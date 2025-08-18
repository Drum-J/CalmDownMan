package chimhaha.chimcard.card.service;

import chimhaha.chimcard.card.dto.*;
import chimhaha.chimcard.card.repository.AccountCardRepository;
import chimhaha.chimcard.card.repository.CardCustomRepository;
import chimhaha.chimcard.card.repository.CardRepository;
import chimhaha.chimcard.card.repository.CardSeasonRepository;
import chimhaha.chimcard.common.AwsProperties;
import chimhaha.chimcard.common.FileValidator;
import chimhaha.chimcard.entity.*;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.user.repository.AccountRepository;
import chimhaha.chimcard.utils.CardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static chimhaha.chimcard.common.MessageConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCardService {

    private final CardRepository cardRepository;
    private final CardSeasonRepository cardSeasonRepository;
    private final S3Client s3Client;
    private final AwsProperties awsProperties;
    private final CardCustomRepository cardCustomRepository;
    private final AccountRepository accountRepository;
    private final AccountCardRepository accountCardRepository;

    @Transactional
    public void saveCard(CardCreateDto dto) {
        CardSeason cardSeason = cardSeasonRepository.findById(dto.cardSeasonId())
                .orElseThrow(() -> new ResourceNotFoundException(CARD_SEASON_NOT_FOUND));

        FileValidator.validate(dto.cardImage());
        String imageUrl = uploadCardImage(dto.cardImage(), dto.cardSeasonId(), dto.grade());

        Card card = Card.builder()
                .title(dto.title())
                .imageUrl(imageUrl)
                .attackType(dto.attackType())
                .grade(dto.grade())
                .power(dto.power())
                .cardSeason(cardSeason)
                .build();

        cardRepository.save(card);
    }

    @Transactional
    public void updateCard(CardUpdateDto dto) {
        Card card = cardRepository.findById(dto.id())
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));

        // 이미지 파일이 있을 경우에만 이미지 업데이트
        MultipartFile newImageFile = dto.cardImage();
        if (newImageFile != null && !newImageFile.isEmpty()) {
            FileValidator.validate(newImageFile);

            // 1. 기존 이미지 S3에서 삭제
            if (StringUtils.hasText(card.getImageUrl())) {
                deleteCardImage(card.getImageUrl());
            }

            // 2. 새 이미지 S3에 업로드
            String newImageUrl = uploadCardImage(newImageFile, card.getCardSeason().getId(), dto.grade());

            // 3. 카드 정보 및 새 이미지 URL 업데이트
            card.update(dto.title(), dto.power(), dto.attackType(), dto.grade(), newImageUrl);
        } else {
            // 이미지를 제외한 나머지 정보만 업데이트
            card.update(dto.title(), dto.power(), dto.attackType(), dto.grade());
        }
    }

    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));

        if (card.getImageUrl() != null) {
            String imageUrl = card.getImageUrl();
            deleteCardImage(imageUrl);
        }

        cardRepository.delete(card);
    }

    @Transactional
    public void saveSeason(SeasonCreateDto dto) {
        FileValidator.validate(dto.seasonImage());
        String imageUrl = uploadSeasonImage(dto.seasonImage());
        cardSeasonRepository.save(new CardSeason(dto.seasonName(), imageUrl));
    }

    private String uploadSeasonImage(MultipartFile file) {
        String key = String.format("season/%s", file.getOriginalFilename());

        return uploadImage(file, key);
    }

    private String uploadCardImage(MultipartFile file, Long cardSeasonId, Grade grade) {
        String key = String.format("card/%s/%s/%s", cardSeasonId, grade, file.getOriginalFilename());

        return uploadImage(file, key);
    }

    private String uploadImage(MultipartFile file, String key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(awsProperties.getS3().bucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return awsProperties.getS3().prefix() + key;
        } catch (IOException e) {
            throw new IllegalStateException("카드 이미지 업로드에 실패했습니다.", e);
        }
    }

    private void deleteCardImage(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return;
        }

        String key = imageUrl.substring(awsProperties.getS3().prefix().length());

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(awsProperties.getS3().bucket())
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    @Transactional
    public void supplyCards(SupplyCardRequestDto dto) {
        Long accountId = dto.accountId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND));

        List<SupplyCardDto> cards = dto.cards();
        Set<Long> cardIds = cards.stream().map(SupplyCardDto::cardId).collect(Collectors.toSet());
        List<AccountCard> accountCards = cardCustomRepository.getMyCardByCardIds(accountId, cardIds);

        Map<Long, AccountCard> accountCardMap = CardUtils.accountCardMapLong(accountCards);
        List<AccountCard> upsertCards = new ArrayList<>();
        for (SupplyCardDto card : cards) {
            AccountCard accountCard = accountCardMap.get(card.cardId());

            if (accountCard != null) {
                accountCard.increaseCount(card.count());
            } else {
                accountCard = new AccountCard(account, getCardById(card.cardId()), card.count());
            }
            upsertCards.add(accountCard);
        }
        accountCardRepository.saveAll(upsertCards);
    }

    private Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND));
    }
}
