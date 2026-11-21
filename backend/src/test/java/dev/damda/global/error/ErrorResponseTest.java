package dev.damda.global.error;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.damda.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class ErrorResponseTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("로그인하지 않고 내 정보를 부르면 401 UNAUTHENTICATED를 준다")
    void anonymousRequest_returnsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("CSRF 토큰 없이 POST하면 403 CSRF_TOKEN_INVALID를 준다")
    void postWithoutCsrfToken_returnsCsrfTokenInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/signup/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CSRF_TOKEN_INVALID"));
    }

    @Test
    @WithMockUser
    @DisplayName("일반 회원이 운영 API를 부르면 403 ACCESS_DENIED를 준다")
    void memberRequestsAdminApi_returnsAccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/admin/anything"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @WithMockUser
    @DisplayName("로그인한 회원이 없는 주소를 부르면 404 NOT_FOUND를 준다")
    void unknownPath_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("검증에 실패한 요청은 400 VALIDATION_FAILED와 칸별 errors를 준다")
    void invalidRequestBody_returnsValidationFailedWithErrors() throws Exception {
        mockMvc.perform(post("/api/v1/signup/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-email\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }
}
