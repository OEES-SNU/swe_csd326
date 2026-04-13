package com.oees.oees.service;

import com.oees.oees.dto.request.LoginRequest;
import com.oees.oees.dto.request.RegisterRequest;
import com.oees.oees.dto.response.AuthResponse;
import com.oees.oees.entity.User;
import com.oees.oees.enums.Role;
import com.oees.oees.repository.userRepository;
import com.oees.oees.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private userRepository userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authManager;

    @InjectMocks private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setName("John Doe");
        validRegisterRequest.setEmail("john@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setRole(Role.STUDENT);

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("john@example.com");
        validLoginRequest.setPassword("password123");

        savedUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.STUDENT)
                .active(true)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-AUTH-01: Successful registration
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-01: Register with valid data returns JWT token")
    void register_validRequest_returnsAuthResponse() {
        when(userRepo.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(validRegisterRequest);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getRole()).isEqualTo("STUDENT");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getName()).isEqualTo("John Doe");
        verify(userRepo).save(any(User.class));
    }

    // ─────────────────────────────────────────────────────────────
    // TC-AUTH-02: Registration with duplicate email
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-02: Register with duplicate email throws RuntimeException")
    void register_duplicateEmail_throwsException() {
        when(userRepo.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRegisterRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already registered");

        verify(userRepo, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // TC-AUTH-03: Registration with INSTRUCTOR role
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-03: Register as INSTRUCTOR sets correct role in response")
    void register_instructorRole_setsRoleCorrectly() {
        validRegisterRequest.setRole(Role.INSTRUCTOR);
        User instructor = User.builder().id(2L).name("John Doe").email("john@example.com")
                .password("enc").role(Role.INSTRUCTOR).active(true).build();
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("enc");
        when(userRepo.save(any())).thenReturn(instructor);
        when(jwtUtil.generateToken(any())).thenReturn("token");

        AuthResponse response = authService.register(validRegisterRequest);

        assertThat(response.getRole()).isEqualTo("INSTRUCTOR");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-AUTH-04: Successful login
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-04: Login with correct credentials returns JWT token")
    void login_validCredentials_returnsAuthResponse() {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(savedUser));
        when(jwtUtil.generateToken(savedUser)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(validLoginRequest);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo("STUDENT");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-AUTH-05: Login with wrong password
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-05: Login with wrong password throws BadCredentialsException")
    void login_wrongPassword_throwsException() {
        when(authManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ─────────────────────────────────────────────────────────────
    // TC-AUTH-06: Login with non-existent email
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-06: Login with unknown email throws RuntimeException after auth")
    void login_unknownEmail_throwsException() {
        when(authManager.authenticate(any())).thenReturn(null);
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-AUTH-07: Password is encoded before saving
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AUTH-07: Password is BCrypt-encoded before persistence")
    void register_passwordIsEncoded() {
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hashed");
        when(userRepo.save(argThat(u -> u.getPassword().equals("$2a$hashed")))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any())).thenReturn("token");

        authService.register(validRegisterRequest);

        verify(passwordEncoder).encode("password123");
    }
}
