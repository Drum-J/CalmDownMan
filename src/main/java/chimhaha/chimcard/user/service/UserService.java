package chimhaha.chimcard.user.service;

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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;

import static chimhaha.chimcard.common.MessageConstants.ACCOUNT_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Client s3Client;
    private final GameCustomRepository gameCustomRepository;

    public UserDetailDto getMyInfo(Long accountId) {
        Account account = getAccount(accountId);

        return new UserDetailDto(account);
    }

    public boolean checkPassword(Long accountId, String password) {
        Account account = getAccount(accountId);

        return passwordEncoder.matches(password, account.getPassword());
    }

    /*@Transactional*/
    public void update(Long accountId, UserUpdateDto dto) {
        try {
            Account account = getAccount(accountId);

            if (StringUtils.hasText(dto.nickname()) && !dto.nickname().equals(account.getNickname())) {
                log.info("변경 닉네임: {} -> {}", account.getNickname(), dto.nickname());
                /*account.updateNickname(dto.nickname());*/
            }

            if (StringUtils.hasText(dto.password())) {
                log.info("변경 비밀번호: {}", dto.password());
                /*account.updatePassword(passwordEncoder.encode(dto.password()));*/
            }

            // TODO 프로필 이미지는 S3 등록 필요
            if (dto.profileImage() != null && !dto.profileImage().isEmpty()) {
                String oldProfile = account.getProfileImage();
                String imageUrl = uploadImage(accountId, dto.profileImage());
                log.info("변경 프로필 이미지: {} -> {}", oldProfile, imageUrl);
                /*account.updateProfileImage(imageUrl);*/

                if (StringUtils.hasText(oldProfile)) {
                    deleteImage(accountId, oldProfile);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    // 마이페이지 > 게임 전적
    public List<GameRecordDto> gameRecords(Long accountId) {
        return gameCustomRepository.getMyGameRecords(accountId);
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND));
    }

    private String uploadImage(Long accountId, MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();


        PutObjectRequest request = PutObjectRequest.builder()
                .bucket("profile-image") // bucket 이름 설정
                .key(accountId + "/" + fileName) // 아마 파일이름
                .contentType(contentType)
                .contentLength(size)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        return request.key();
    }

    private void deleteImage(Long accountId, String imageUrl) {
        // S3에서 이미지 삭제 로직 구현
        log.info("S3에서 기존 이미지 삭제를 시도합니다: {}", imageUrl);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket("profile-image")
                .key(imageUrl)
                .build();

        s3Client.deleteObject(request);
    }

}
