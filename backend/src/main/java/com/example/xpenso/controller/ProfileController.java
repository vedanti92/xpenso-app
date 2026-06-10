package com.example.xpenso.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.xpenso.dto.AuthDTO;
import com.example.xpenso.dto.ProfileDTO;
import com.example.xpenso.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerProfile(@RequestBody ProfileDTO profileDTO) {
        Map<String, Object> response = profileService.registerProfile(profileDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
