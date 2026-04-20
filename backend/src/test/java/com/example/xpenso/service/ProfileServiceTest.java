package com.example.xpenso.service;

import com.example.xpenso.dto.AuthDTO;
import com.example.xpenso.dto.ProfileDTO;
import com.example.xpenso.entity.ProfileEntity;
import com.example.xpenso.repository.ProfileRepository;
import com.example.xpenso.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AppUserDetailsService appUserDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ProfileService profileService;

    private ProfileEntity mockProfile;

    @BeforeEach
    void setUp() {
        mockProfile = ProfileEntity.builder()
                .id(1L)
                .fullName("Vedanti Test")
                .email("vedanti@test.com")
                .password("encoded_password")
                .isActive(false)
                .activationToken("test-token-123")
                .build();
    }

    // ─── registerProfile ───────────────────────────────────────────────────────

    @Test
    void registerProfile_shouldSaveAndReturnResponse() {
        ProfileDTO dto = ProfileDTO.builder()
                .fullName("Vedanti Test")
                .email("vedanti@test.com")
                .password("plain_password")
                .build();

        when(passwordEncoder.encode("plain_password")).thenReturn("encoded_password");
        when(profileRepository.save(any(ProfileEntity.class))).thenReturn(mockProfile);
        when(emailService.sendEmailAsync(anyString(), anyString(), anyString()))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));

        // inject @Value field manually
        org.springframework.test.util.ReflectionTestUtils.setField(profileService, "activationURL", "http://localhost:8080");
        org.springframework.test.util.ReflectionTestUtils.setField(profileService, "returnActivationLink", true);

        Map<String, Object> response = profileService.registerProfile(dto);

        assertThat(response).containsKey("profile");
        assertThat(response).containsKey("activationLink");
        verify(profileRepository, times(1)).save(any(ProfileEntity.class));
    }

    // ─── activateProfile ───────────────────────────────────────────────────────

    @Test
    void activateProfile_shouldReturnTrue_whenTokenValid() {
        when(profileRepository.findByActivationToken("test-token-123"))
                .thenReturn(Optional.of(mockProfile));
        when(profileRepository.save(any(ProfileEntity.class))).thenReturn(mockProfile);

        boolean result = profileService.activateProfile("test-token-123");

        assertThat(result).isTrue();
        assertThat(mockProfile.getIsActive()).isTrue();
    }

    @Test
    void activateProfile_shouldReturnFalse_whenTokenInvalid() {
        when(profileRepository.findByActivationToken("bad-token")).thenReturn(Optional.empty());

        boolean result = profileService.activateProfile("bad-token");

        assertThat(result).isFalse();
    }

    // ─── isAccountActive ───────────────────────────────────────────────────────

    @Test
    void isAccountActive_shouldReturnFalse_whenNotActivated() {
        when(profileRepository.findByEmail("vedanti@test.com")).thenReturn(Optional.of(mockProfile));

        boolean result = profileService.isAccountActive("vedanti@test.com");

        assertThat(result).isFalse();
    }

    @Test
    void isAccountActive_shouldReturnTrue_whenActivated() {
        mockProfile.setIsActive(true);
        when(profileRepository.findByEmail("vedanti@test.com")).thenReturn(Optional.of(mockProfile));

        boolean result = profileService.isAccountActive("vedanti@test.com");

        assertThat(result).isTrue();
    }

    // ─── resendActivationEmail ─────────────────────────────────────────────────

    @Test
    void resendActivationEmail_shouldReturnTrue_whenEmailFoundAndNotActive() {
        when(profileRepository.findByEmail("vedanti@test.com")).thenReturn(Optional.of(mockProfile));
        when(emailService.sendEmailAsync(anyString(), anyString(), anyString()))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));

        org.springframework.test.util.ReflectionTestUtils.setField(profileService, "activationURL", "http://localhost:8080");

        boolean result = profileService.resendActivationEmail("vedanti@test.com");

        assertThat(result).isTrue();
    }

    @Test
    void resendActivationEmail_shouldReturnFalse_whenAlreadyActive() {
        mockProfile.setIsActive(true);
        when(profileRepository.findByEmail("vedanti@test.com")).thenReturn(Optional.of(mockProfile));

        boolean result = profileService.resendActivationEmail("vedanti@test.com");

        assertThat(result).isFalse();
    }

    @Test
    void resendActivationEmail_shouldReturnFalse_whenEmailNotFound() {
        when(profileRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        boolean result = profileService.resendActivationEmail("ghost@test.com");

        assertThat(result).isFalse();
    }

    // ─── authenticateAndGenerateToken ─────────────────────────────────────────

    @Test
    void authenticateAndGenerateToken_shouldReturnTokenAndProfile() {
        AuthDTO authDTO = AuthDTO.builder()
                .email("vedanti@test.com")
                .password("plain_password")
                .build();

        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(appUserDetailsService.loadUserByUsername("vedanti@test.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("mock.jwt.token");
        when(profileRepository.findByEmail("vedanti@test.com")).thenReturn(Optional.of(mockProfile));

        Map<String, Object> result = profileService.authenticateAndGenerateToken(authDTO);

        assertThat(result).containsKey("token");
        assertThat(result.get("token")).isEqualTo("mock.jwt.token");
    }

    @Test
    void authenticateAndGenerateToken_shouldThrow_onBadCredentials() {
        AuthDTO authDTO = AuthDTO.builder()
                .email("vedanti@test.com")
                .password("wrong_password")
                .build();

        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThatThrownBy(() -> profileService.authenticateAndGenerateToken(authDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }
}