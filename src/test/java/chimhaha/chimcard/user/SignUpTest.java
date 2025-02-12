package chimhaha.chimcard.user;

import chimhaha.chimcard.user.dto.SignUpDto;
import chimhaha.chimcard.user.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
public class SignUpTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;

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
        MvcResult mvcResult = mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        //then
        assertEquals(content, "회원 가입 완료!");
    }

    @Test
    @DisplayName("username 중복")
    void username_false() throws Exception {
        //given
        String username = "seungho";

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/signup/checkUsername")
                        .param("username", username)
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        //then
        assertEquals(content, "해당 ID가 이미 존재합니다.");
    }

    @Test
    @DisplayName("username Unique")
    void username_true() throws Exception {
        //given
        String username = "testUsername";

        //when
        MvcResult mvcResult = mockMvc.perform(get("/api/signup/checkUsername")
                        .param("username", username)
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        //then
        assertEquals(content, "사용 가능한 ID 입니다!");

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
        MvcResult mvcResult = mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        //then
        assertEquals(content,"이미 사용 중인 닉네임입니다.");
    }

    @Test
    @DisplayName("passwordCheck 에러")
    void password_error() throws Exception {
        //given
        SignUpDto dto = SignUpDto.builder()
                .username("testUsername")
                .nickname("testNickname")
                .password("pass1234")
                .build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        //then
        assertEquals(content, "비밀번호는 영문, 숫자, 특수문자 포함 8~20글자 이하로 입력해 주세요.");
    }

    @Test
    @DisplayName("nickname 중복, passwordCheck 에러")
    void nickname_password_error() throws Exception {
        //given
        SignUpDto dto = SignUpDto.builder()
                .username("testUsername")
                .nickname("drumj")
                .password("pa12!")
                .build();

        //when
        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        //then
    }
}
