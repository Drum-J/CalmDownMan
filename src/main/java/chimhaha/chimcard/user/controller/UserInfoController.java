package chimhaha.chimcard.user.controller;

import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.user.dto.UserDetailDto;
import chimhaha.chimcard.user.service.UserService;
import chimhaha.chimcard.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserInfoController {

    private final UserService userService;

    @GetMapping("/myInfo")
    public ApiResponse<UserDetailDto> getMyInfo() {
        Long accountId = AccountUtils.getAccountId();

        return ApiResponse.success(userService.getMyInfo(accountId));
    }
}
