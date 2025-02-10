package chimhaha.chimcard.user.controller;

import chimhaha.chimcard.user.dto.SignUpDto;
import chimhaha.chimcard.user.service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/signup")
public class SignUpApiController {

    private final SignUpService signUpService;

    @PostMapping
    public void singUp(@RequestBody @Valid SignUpDto dto) {
        signUpService.signUp(dto);
    }

    @GetMapping("/checkUsername")
    public ResponseEntity<String> checkUsername(@RequestParam("username") String username) {
        boolean checked = signUpService.checkUsername(username);

        if (!checked) {
            return ResponseEntity.ok("사용 가능한 ID 입니다!");
        } else {
            return ResponseEntity.badRequest().body("해당 ID가 이미 존재합니다.");
        }
    }
}
