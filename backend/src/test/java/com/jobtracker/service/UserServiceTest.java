package com.jobtracker.service;

import com.jobtracker.dto.LoginRequest;
import com.jobtracker.dto.ProfileUpdateRequest;
import com.jobtracker.dto.RegisterRequest;
import com.jobtracker.entity.User;
import com.jobtracker.exception.EmailAlreadyExistsException;
import com.jobtracker.exception.InvalidCredentialsException;
import com.jobtracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserService userService;

    @Test
    void registrationNormalizesEmailHashesPasswordAndReturnsNoPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setName("  Alex Example  ");
        request.setEmail(" Alex@Example.com ");
        request.setPassword("secure-pass-123");
        when(userRepository.existsByEmailIgnoreCase("alex@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secure-pass-123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(12L);
            return user;
        });

        var response = userService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("Alex Example", captor.getValue().getName());
        assertEquals("alex@example.com", captor.getValue().getEmail());
        assertEquals("bcrypt-hash", captor.getValue().getPassword());
        assertEquals(12L, response.getId());
        assertFalse(response.toString().contains("bcrypt-hash"));
    }

    @Test
    void registrationRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alex");
        request.setEmail("alex@example.com");
        request.setPassword("secure-pass-123");
        when(userRepository.existsByEmailIgnoreCase("alex@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsJwtForMatchingPassword() {
        User user = user(12L, "alex@example.com", "bcrypt-hash");
        LoginRequest request = new LoginRequest();
        request.setEmail("ALEX@example.com");
        request.setPassword("secure-pass-123");
        when(userRepository.findByEmailIgnoreCase("alex@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secure-pass-123", "bcrypt-hash")).thenReturn(true);
        when(jwtService.generateToken("alex@example.com")).thenReturn("signed.jwt");

        var response = userService.login(request);

        assertEquals("signed.jwt", response.getToken());
        assertEquals(12L, response.getId());
        verify(passwordEncoder).matches("secure-pass-123", "bcrypt-hash");
    }

    @Test
    void loginRejectsWrongPasswordWithGenericError() {
        User user = user(12L, "alex@example.com", "bcrypt-hash");
        LoginRequest request = new LoginRequest();
        request.setEmail("alex@example.com");
        request.setPassword("wrong-password");
        when(userRepository.findByEmailIgnoreCase("alex@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "bcrypt-hash")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(request));
        assertEquals("Invalid email or password", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void profileUpdateChangesOnlyName() {
        User user = user(12L, "alex@example.com", "bcrypt-hash");
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("  Alex Newname ");
        when(userRepository.findByEmailIgnoreCase("alex@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        var response = userService.updateCurrentUser("alex@example.com", request);

        assertEquals("Alex Newname", response.getName());
        assertEquals("alex@example.com", response.getEmail());
        assertEquals("bcrypt-hash", user.getPassword());
    }

    private User user(Long id, String email, String password) {
        User user = new User();
        user.setId(id);
        user.setName("Alex Example");
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
