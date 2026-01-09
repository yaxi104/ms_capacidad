package com.reactivo.capacidad.domain.usecase;

import com.reactivo.capacidad.domain.api.JwtServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)
class AuthenticationUseCaseTest {

    @Mock
    private JwtServicePort jwtServicePort;

    @InjectMocks
    private AuthenticationUseCase authenticationUseCase;

    private String email;
    private String role;
    private String token;

    @BeforeEach
    void setUp() {
        email = "user@example.com";
        role = "ADMIN";
        token = "fake-jwt-token";
    }

    @Test
    void validateTokenSuccessTest() {
        when(jwtServicePort.validateToken(token)).thenReturn(true);

        boolean isValid = jwtServicePort.validateToken(token);
        assert isValid;
    }

    @Test
    void validateTokenTokenIsInvalidTest() {
        String invalidToken = "invalid-token";
        when(jwtServicePort.validateToken(invalidToken)).thenReturn(false);

        boolean isValid = jwtServicePort.validateToken(invalidToken);
        assert !isValid;
    }

    @Test
    void getEmailAndRoleFromTokenTest() {
        when(jwtServicePort.getEmailFromToken(token)).thenReturn(email);
        when(jwtServicePort.getRoleFromToken(token)).thenReturn(role);

        String extractedEmail = jwtServicePort.getEmailFromToken(token);
        String extractedRole = jwtServicePort.getRoleFromToken(token);

        assertEquals(email, extractedEmail, "user@example.com");
        assertEquals(role, extractedRole, "ADMIN");
    }
}