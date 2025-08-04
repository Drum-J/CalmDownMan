package chimhaha.chimcard.user.service;

import chimhaha.chimcard.common.AwsProperties;
import chimhaha.chimcard.common.FileValidator;
import chimhaha.chimcard.entity.Account;
import chimhaha.chimcard.exception.ResourceNotFoundException;
import chimhaha.chimcard.game.dto.GameRecordDto;
import chimhaha.chimcard.game.repository.GameCustomRepository;
import chimhaha.chimcard.user.dto.UserDetailDto;
import chimhaha.chimcard.user.dto.UserUpdateDto;
import chimhaha.chimcard.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;

import static chimhaha.chimcard.common.MessageConstants.ACCOUNT_NOT_FOUND;
import static chimhaha.chimcard.common.MessageConstants.EXIST_NICKNAME;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Client s3Client;
    private final GameCustomRepository gameCustomRepository;
    private final AwsProperties awsProperties;

    public UserDetailDto getMyInfo(Long accountId) {
        Account account = getAccount(accountId);

        return new UserDetailDto(account);
    }

    public boolean checkPassword(Long accountId, String password) {
        Account account = getAccount(accountId);

        return passwordEncoder.matches(password, account.getPassword());
    }

    @Transactional
    public UserDetailDto update(Long accountId, UserUpdateDto dto) {
        Account account = getAccount(accountId);

        if (StringUtils.hasText(dto.nickname()) && !dto.nickname().equals(account.getNickname())) {
            if (accountRepository.existsByNickname(dto.nickname())) {
                throw new IllegalArgumentException(EXIST_NICKNAME);
            }
            account.updateNickname(dto.nickname());
        }

        if (StringUtils.hasText(dto.password())) {
            account.updatePassword(passwordEncoder.encode(dto.password()));
        }

        if (dto.profileImage() != null && !dto.profileImage().isEmpty()) {
            FileValidator.validate(dto.profileImage());
            account.updateProfileImage(uploadImage(accountId, dto.profileImage()));
        }
        return new UserDetailDto(account);
    }

    // 마이페이지 > 게임 전적
    public List<GameRecordDto> gameRecords(Long accountId) {
        return gameCustomRepository.getMyGameRecords(accountId);
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND));
    }

    private String uploadImage(Long accountId, MultipartFile file) {
        String key = "user/profile/" + accountId;
        String contentType = file.getContentType();
        long size = file.getSize();


        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(awsProperties.getS3().bucket()) // bucket 이름 설정
                    .key(key)
                    .contentType(contentType)
                    .contentLength(size)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return awsProperties.getS3().prefix() + key;
        } catch (IOException e) {
            throw new IllegalStateException("프로필 이미지 등록에 실패했습니다.", e);
        }
    }

}
