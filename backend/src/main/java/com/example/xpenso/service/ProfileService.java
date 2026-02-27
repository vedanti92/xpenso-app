package com.example.xpenso.service;

import com.example.xpenso.dto.AuthDTO;
import com.example.xpenso.dto.ProfileDTO;
import com.example.xpenso.entity.ProfileEntity;
import com.example.xpenso.repository.ProfileRepository;
import com.example.xpenso.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationURL;

    @Value("${app.return.activation.link:true}")
    private boolean returnActivationLink;

    public Map<String, Object> registerProfile(ProfileDTO profileDTO) {
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);
        
        // Generate activation link
        String activationLink = activationURL + "/activate?token=" + newProfile.getActivationToken();
        
        // Send activation email asynchronously - don't block registration if email fails
        String subject = "Activate your Xpenso account.";
        String body = "Click on the following link to activate your Xpenso account: " + activationLink;
        emailService.sendEmailAsync(newProfile.getEmail(), subject, body);
        
        // Build response
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("profile", toDTO(newProfile));
        
        // Include activation link in response (useful if email fails or for development)
        if (returnActivationLink) {
            response.put("activationLink", activationLink);
            response.put("message", "Registration successful. Please check your email for activation link. If you don't receive it, use the activation link provided below or request a resend.");
        } else {
            response.put("message", "Registration successful. Please check your email for activation link. If you don't receive it, you can request a resend.");
        }
        
        return response;
    }

    public boolean resendActivationEmail(String email) {
        return profileRepository.findByEmail(email)
                .map(profile -> {
                    if (profile.getIsActive()) {
                        return false; // Already activated
                    }
                    
                    // Generate new activation link
                    String activationLink = activationURL + "/activate?token=" + profile.getActivationToken();
                    String subject = "Activate your Xpenso account.";
                    String body = "Click on the following link to activate your Xpenso account: " + activationLink;
                    
                    // Send email asynchronously
                    emailService.sendEmailAsync(profile.getEmail(), subject, body);
                    return true;
                })
                .orElse(false); // Email not found
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    public boolean activateProfile(String activationToken) {
        return  profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }

    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser = null;
        if (email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(()  -> new UsernameNotFoundException("Profile not found with email: " + email));
        }
        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));
            UserDetails userDetails = appUserDetailsService.loadUserByUsername(authDTO.getEmail());
//            generate JWT token
            String token = jwtUtil.generateToken(userDetails);
            return Map.of("token", token,
                    "user", getPublicProfile(authDTO.getEmail()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password.");
        }
    }
}
