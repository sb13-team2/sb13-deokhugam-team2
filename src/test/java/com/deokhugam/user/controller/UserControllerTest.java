package com.deokhugam.user.controller;

import com.deokhugam.dashboard.service.DashboardQueryService;
import com.deokhugam.global.config.SecurityConfig;
import com.deokhugam.user.dto.request.UserRegisterRequest;
import com.deokhugam.user.dto.response.UserDto;
import com.deokhugam.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private DashboardQueryService dashboardQueryService;

    @Test
    @DisplayName("회원가입 API - 성공 (201 Created)")
    void register_success() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest("test@test.com", "testNick", "Password123!");
        UserDto responseDto = new UserDto(UUID.randomUUID(), "test@test.com", "testNick", LocalDateTime.now());

        given(userService.register(any(UserRegisterRequest.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("testNick"));
    }

    @Test
    @DisplayName("회원가입 API - 실패 (유효성 검증 오류 400 Bad Request)")
    void register_fail_validation() throws Exception {
        // given: 잘못된 이메일 형식과 짧은 비밀번호
        UserRegisterRequest request = new UserRegisterRequest("invalid_email", "testNick", "123");

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }
}