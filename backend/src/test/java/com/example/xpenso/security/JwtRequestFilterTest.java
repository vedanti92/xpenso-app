package com.example.xpenso.security;

import com.example.xpenso.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtRequestFilterTest {

        @Mock
        private UserDetailsService userDetailsService;

        @Mock
        private JwtUtil jwtUtil;

        @Mock
        private FilterChain filterChain;

        @InjectMocks
        private JwtRequestFilter jwtRequestFilter;

        @AfterEach
        void clearSecurityContext() {
                SecurityContextHolder.clearContext();
        }

        // ─── No bearer token ─────

        @Test
        void shouldContinueChainWhenTokenIsMissing()
                        throws ServletException, IOException {

                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();

                jwtRequestFilter.doFilter(request, response, filterChain);

                verify(filterChain).doFilter(request, response);
                assertEquals(200, response.getStatus());
                verifyNoInteractions(jwtUtil, userDetailsService);
        }

        // ─── Invalid JWT ─────

        @Test
        void shouldReturn401WhenTokenIsMalformed()
                        throws ServletException, IOException {

                MockHttpServletRequest request = new MockHttpServletRequest();
                request.addHeader("Authorization", "Bearer malformed-token");

                MockHttpServletResponse response = new MockHttpServletResponse();

                when(jwtUtil.extractUsername("malformed-token"))
                                .thenThrow(new JwtException("Malformed token"));

                jwtRequestFilter.doFilter(request, response, filterChain);

                assertEquals(401, response.getStatus());
                verify(filterChain, never()).doFilter(request, response);
        }

        // ─── Unknown user ─────

        @Test
        void shouldReturn401WhenUserDoesNotExist()
                        throws ServletException, IOException {

                MockHttpServletRequest request = new MockHttpServletRequest();
                request.addHeader("Authorization", "Bearer valid-format-token");

                MockHttpServletResponse response = new MockHttpServletResponse();

                when(jwtUtil.extractUsername("valid-format-token"))
                                .thenReturn("unknown@example.com");

                when(userDetailsService.loadUserByUsername("unknown@example.com"))
                                .thenThrow(new UsernameNotFoundException("User not found"));

                jwtRequestFilter.doFilter(request, response, filterChain);

                assertEquals(401, response.getStatus());
                verify(filterChain, never()).doFilter(request, response);
        }

        // ─── Valid JWT ─────

        @Test
        void shouldAuthenticateUserWhenTokenIsValid()
                        throws ServletException, IOException {

                MockHttpServletRequest request = new MockHttpServletRequest();
                request.addHeader("Authorization", "Bearer valid-token");

                MockHttpServletResponse response = new MockHttpServletResponse();

                UserDetails userDetails = User.withUsername("user@example.com")
                                .password("password")
                                .authorities("USER")
                                .build();

                when(jwtUtil.extractUsername("valid-token"))
                                .thenReturn("user@example.com");

                when(userDetailsService.loadUserByUsername("user@example.com"))
                                .thenReturn(userDetails);

                when(jwtUtil.validateToken("valid-token", userDetails))
                                .thenReturn(true);

                jwtRequestFilter.doFilter(request, response, filterChain);

                assertNotNull(
                                SecurityContextHolder.getContext().getAuthentication());

                assertEquals(
                                "user@example.com",
                                SecurityContextHolder.getContext()
                                                .getAuthentication().getName());

                verify(filterChain).doFilter(request, response);
        }

        // ─── Invalid token validation ─────

        @Test
        void shouldNotAuthenticateUserWhenValidationFails()
                        throws ServletException, IOException {

                MockHttpServletRequest request = new MockHttpServletRequest();
                request.addHeader("Authorization", "Bearer invalid-token");

                MockHttpServletResponse response = new MockHttpServletResponse();

                UserDetails userDetails = User.withUsername("user@example.com")
                                .password("password")
                                .authorities("USER")
                                .build();

                when(jwtUtil.extractUsername("invalid-token"))
                                .thenReturn("user@example.com");

                when(userDetailsService.loadUserByUsername("user@example.com"))
                                .thenReturn(userDetails);

                when(jwtUtil.validateToken("invalid-token", userDetails))
                                .thenReturn(false);

                jwtRequestFilter.doFilter(request, response, filterChain);

                assertNull(SecurityContextHolder.getContext().getAuthentication());
                verify(filterChain).doFilter(request, response);
        }
}
