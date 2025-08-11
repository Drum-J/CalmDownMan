package chimhaha.chimcard.user.controller;

import chimhaha.chimcard.common.ApiResponse;
import chimhaha.chimcard.user.dto.UpdatePointDto;
import chimhaha.chimcard.user.dto.UpdateRoleDto;
import chimhaha.chimcard.user.dto.UserDetailDto;
import chimhaha.chimcard.user.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/user")
public class UserAdminController {

    private final AccountService accountService;

    @GetMapping
    public ApiResponse<List<UserDetailDto>> getAllUser() {
        return ApiResponse.success(accountService.getAllUser());
    }

    @PutMapping("/{accountId}/role")
    public void updateRole(@PathVariable("accountId") Long accountId, @RequestBody UpdateRoleDto dto) {
        accountService.updateRole(accountId, dto.role());
    }

    @PutMapping("/{accountId}/point")
    public void updatePoint(@PathVariable("accountId") Long accountId, @RequestBody UpdatePointDto dto) {
        accountService.updatePoint(accountId, dto.point());
    }
}
