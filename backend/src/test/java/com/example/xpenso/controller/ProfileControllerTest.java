package com.example.xpenso.controller;

import com.example.xpenso.dto.AuthDTO;
import com.example.xpenso.dto.ProfileDTO;
import com.example.xpenso.security.JwtRequestFilter;
import com.example.xpenso.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    // ✅ ObjectMapper created manually — not @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ─── POST /register ────────────────────────────────────────────────────────

    @Test
    void register_shouldReturn201() throws Exception {
        ProfileDTO dto = ProfileDTO.builder()
                .fullName("Vedanti Test")
                .email("vedanti@test.com")
                .password("password123")
                .build();

        ProfileDTO savedProfile = ProfileDTO.builder()
                .id(1L)
                .fullName("Vedanti Test")
                .email("vedanti@test.com")
                .build();

        when(profileService.registerProfile(any(ProfileDTO.class)))
                .thenReturn(Map.of("profile", savedProfile, "message", "Registration successful."));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful."));
    }

    // ─── POST /login ───────────────────────────────────────────────────────────

    @Test
    void login_shouldReturn200_withToken() throws Exception {
        AuthDTO authDTO = AuthDTO.builder()
                .email("vedanti@test.com")
                .password("password123")
                .build();

        when(profileService.isAccountActive("vedanti@test.com")).thenReturn(true);
        when(profileService.authenticateAndGenerateToken(any(AuthDTO.class)))
                .thenReturn(Map.of("token", "mock.jwt.token"));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"));
    }

    @Test
    void login_shouldReturn403_whenAccountNotActive() throws Exception {
        AuthDTO authDTO = AuthDTO.builder()
                .email("vedanti@test.com")
                .password("password123")
                .build();

        when(profileService.isAccountActive("vedanti@test.com")).thenReturn(false);

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account is not active. Please activate your account first."));
    }

    // ─── GET /activate ─────────────────────────────────────────────────────────

    @Test
    void activate_shouldReturn302_whenTokenValid() throws Exception {
        when(profileService.activateProfile("test-token-123")).thenReturn(true);

        mockMvc.perform(get("/activate").param("token", "test-token-123"))
                .andExpect(status().isFound());
    }

    @Test
    void activate_shouldReturn404_whenTokenInvalid() throws Exception {
        when(profileService.activateProfile("bad-token")).thenReturn(false);

        mockMvc.perform(get("/activate").param("token", "bad-token"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /resend-activation ───────────────────────────────────────────────

    @Test
    void resendActivation_shouldReturn200_whenEmailSent() throws Exception {
        when(profileService.resendActivationEmail("vedanti@test.com")).thenReturn(true);

        mockMvc.perform(post("/resend-activation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vedanti@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Activation email has been sent. Please check your inbox."));
    }

    @Test
    void resendActivation_shouldReturn404_whenNotFound() throws Exception {
        when(profileService.resendActivationEmail("ghost@test.com")).thenReturn(false);

        mockMvc.perform(post("/resend-activation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ghost@test.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resendActivation_shouldReturn400_whenEmailBlank() throws Exception {
        mockMvc.perform(post("/resend-activation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /profile ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getProfile_shouldReturn200() throws Exception {
        ProfileDTO profile = ProfileDTO.builder()
                .id(1L)
                .fullName("Vedanti Test")
                .email("vedanti@test.com")
                .build();

        when(profileService.getPublicProfile(null)).thenReturn(profile);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Vedanti Test"));
    }
}