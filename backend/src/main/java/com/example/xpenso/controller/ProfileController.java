package com.example.xpenso.controller;

import com.example.xpenso.dto.AuthDTO;
import com.example.xpenso.dto.ProfileDTO;
import com.example.xpenso.entity.ProfileEntity;
import com.example.xpenso.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerProfile(@RequestBody ProfileDTO profileDTO) {
        Map<String, Object> response = profileService.registerProfile(profileDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<Map<String, Object>> resendActivationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email is required."));
        }
        
        boolean sent = profileService.resendActivationEmail(email);
        if (sent) {
            return ResponseEntity.ok(Map.of("message", "Activation email has been sent. Please check your inbox."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Account not found or already activated."));
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<Void> activateProfile(@RequestParam String token) {
        boolean isActivated = profileService.activateProfile(token);

        if (isActivated) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "https://xpenso-app.netlify.app/login")
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthDTO authDTO) {
        try {
            if (!profileService.isAccountActive(authDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Account is not active. Please activate your account first."));
            }
            Map<String, Object> response = profileService.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getPublicProfile() {
        ProfileDTO profileDTO = profileService.getPublicProfile(null);
        return ResponseEntity.ok(profileDTO);
    }
}
