package chimhaha.chimcard.user.controller;

import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.game.dto.GameRecordDto;
import chimhaha.chimcard.user.dto.PasswordCheckDto;
import chimhaha.chimcard.user.dto.UserDetailDto;
import chimhaha.chimcard.user.dto.UserUpdateDto;
import chimhaha.chimcard.user.service.AccountService;
import chimhaha.chimcard.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final AccountService accountService;

    @GetMapping("/myInfo")
    public ApiResponse<UserDetailDto> getMyInfo() {
        Long accountId = AccountUtils.getAccountId();

        return ApiResponse.success(accountService.getMyInfo(accountId));
    }

    @PostMapping("/checkPassword")
    public ApiResponse<Boolean> checkPassword(@RequestBody PasswordCheckDto dto) {
        log.info("password: {}", dto.password());
        Long accountId = AccountUtils.getAccountId();

        return ApiResponse.success(accountService.checkPassword(accountId, dto.password()));
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserDetailDto> update(UserUpdateDto dto) {
        Long accountId = AccountUtils.getAccountId();

        return ApiResponse.success(accountService.update(accountId, dto));
    }

    @GetMapping("/gameRecords")
    public ApiResponse<List<GameRecordDto>> gameRecords() {
        Long accountId = AccountUtils.getAccountId();

        return ApiResponse.success(accountService.gameRecords(accountId));
    }
}
