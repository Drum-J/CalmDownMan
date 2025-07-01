package chimhaha.chimcard.user.controller;

import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.user.dto.PasswordCheckDto;
import chimhaha.chimcard.user.dto.UserDetailDto;
import chimhaha.chimcard.user.dto.UserUpdateDto;
import chimhaha.chimcard.user.service.UserService;
import chimhaha.chimcard.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/myInfo")
    public ApiResponse<UserDetailDto> getMyInfo() {
        Long accountId = AccountUtils.getAccountId();

        return ApiResponse.success(userService.getMyInfo(accountId));
    }

    @PostMapping("/checkPassword")
    public ApiResponse<Boolean> checkPassword(@RequestBody PasswordCheckDto dto) {
        log.info("password: {}", dto.password());
        Long accountId = AccountUtils.getAccountId();

        return ApiResponse.success(userService.checkPassword(accountId, dto.password()));
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> update(UserUpdateDto dto) {
        log.info("dto: {}", dto);
        Long accountId = AccountUtils.getAccountId();
        userService.update(accountId, dto);
        return null;
    }
}
