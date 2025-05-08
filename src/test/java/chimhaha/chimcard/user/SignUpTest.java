package chimhaha.chimcard.user;

import chimhaha.chimcard.user.dto.SignUpDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
public class SignUpTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 정상")
    @Transactional
    void signUp() throws Exception {
        //given
        SignUpDto dto = SignUpDto.builder()
                .username("testUsername")
                .nickname("testNickname")
                .password("test123!")
                .build();

        //when
        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
        //then
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(OK.value()))
                .andExpect(jsonPath("message").value(OK.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //T data
                .andExpect(jsonPath("data").value("회원 가입 완료!"));
    }

    @Test
    @DisplayName("username 중복")
    void username_false() throws Exception {
        //given
        String username = "seungho";

        //when
        mockMvc.perform(get("/api/signup/checkUsername")
                        .param("username", username)
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
        //then
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("message").value(BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //T data
                .andExpect(jsonPath("data").value("해당 ID가 이미 존재합니다."));
    }

    @Test
    @DisplayName("username Unique")
    void username_true() throws Exception {
        //given
        String username = "testUsername";

        //when
        mockMvc.perform(get("/api/signup/checkUsername")
                        .param("username", username)
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
        //then
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(OK.value()))
                .andExpect(jsonPath("message").value(OK.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //T data
                .andExpect(jsonPath("data").value("사용 가능한 ID 입니다!"));
    }

    @Test
    @DisplayName("nickname 중복")
    void nickname_false() throws Exception {
        //given
        SignUpDto dto = SignUpDto.builder()
                .username("testUsername")
                .nickname("drumj")
                .password("test123!")
                .build();

        //when
        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
        //then
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("message").value(BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //T data
                .andExpect(jsonPath("data").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    @DisplayName("password pattern 에러")
    void password_error() throws Exception {
        //given
        SignUpDto dto = SignUpDto.builder()
                .username("testUsername")
                .nickname("testNickname")
                .password("pass1234")
                .build();

        //when
        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
        //then
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("message").value(BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //T data
                .andExpect(jsonPath("data")
                        .value("비밀번호는 영문, 숫자, 특수문자 포함 8~20글자 이하로 입력해 주세요."));
    }

    @Test
    @DisplayName("@Size, @Pattern 에러")
    void validation_Error() throws Exception {
        //given
        SignUpDto dto = SignUpDto.builder()
                .username("123")
                .nickname("1")
                .password("123")
                .build();

        //when
        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
        //then
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("message").value(BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //T data
                .andExpect(jsonPath("data.size()").value(3));
    }

    @Test
    @DisplayName("모두 빈 값이 넘어온 경우")
    void allBlank() throws Exception {
        //given
        SignUpDto dto = SignUpDto.builder()
                .username("")
                .nickname("")
                .password("")
                .build();

        //when
        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
        //then
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("message").value(BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //T data
                .andExpect(jsonPath("data.size()").value(6));
    }

    @Test
    @DisplayName("모두 Null 값이 넘어온 경우")
    void allNull() throws Exception {
        //given
        SignUpDto dto = SignUpDto.builder()
                .username(null)
                .nickname(null)
                .password(null)
                .build();

        //when
        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
        //then
                //ApiResponse 공통
                .andExpect(jsonPath("status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("message").value(BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("time").exists())
                //T data
                .andExpect(jsonPath("data.size()").value(3));
    }
}
