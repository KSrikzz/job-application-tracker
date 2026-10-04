package com.jobtracker.controller;

import com.jobtracker.dto.ProfileUpdateRequest;
import com.jobtracker.dto.UserResponse;
import com.jobtracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        return userService.getCurrentUser(authentication.getName());
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {
        return userService.updateCurrentUser(authentication.getName(), request);
    }
}
