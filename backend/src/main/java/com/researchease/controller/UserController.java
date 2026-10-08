package com.researchease.controller;

import com.researchease.common.Result;
import com.researchease.dto.UserCreateRequest;
import com.researchease.dto.UserResponse;
import com.researchease.dto.UpdateEnabledRequest;
import com.researchease.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<List<UserResponse>> list() {
        return Result.ok(userService.list());
    }

    @PostMapping
    public Result<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.ok(userService.create(request));
    }

    @PutMapping("/{id}/enabled")
    public Result<Void> setEnabled(@PathVariable Long id, @Valid @RequestBody UpdateEnabledRequest request) {
        userService.setEnabled(id, request.getEnabled());
        return Result.ok();
    }
}
