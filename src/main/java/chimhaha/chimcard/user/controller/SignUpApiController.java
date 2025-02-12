package chimhaha.chimcard.user.controller;

import chimhaha.chimcard.user.dto.SignUpDto;
import chimhaha.chimcard.user.service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/signup")
public class SignUpApiController {

    private final SignUpService signUpService;

    @PostMapping
    public ResponseEntity<String> singUp(@RequestBody @Valid SignUpDto dto, Errors errors) {
        if (errors.hasErrors()) {
            List<String> errorMessages = errors.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).toList();

            return ResponseEntity.badRequest().body(String.join(", ",errorMessages));
        }

        signUpService.signUp(dto);
        return ResponseEntity.ok("회원 가입 완료!");
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
