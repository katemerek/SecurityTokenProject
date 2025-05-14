package com.github.katemerek.securitytokenproject.controller;

import com.github.katemerek.securitytokenproject.dto.LoginResponse;
import com.github.katemerek.securitytokenproject.dto.MyUserDto;
import com.github.katemerek.securitytokenproject.dto.MyUserDtoForGet;
import com.github.katemerek.securitytokenproject.dto.MyUserResponse;
import com.github.katemerek.securitytokenproject.exception.UserNotFoundException;
import com.github.katemerek.securitytokenproject.model.MyUser;
import com.github.katemerek.securitytokenproject.service.MyUserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MyUserController {

    private final MyUserManagementService myUserManagementService;

    public MyUserController(MyUserManagementService myUserManagementService) {
        this.myUserManagementService = myUserManagementService;
    }

    @PutMapping("/moderator/update/{id}")
    public ResponseEntity<MyUserResponse> updateUser(@PathVariable Long id, @RequestBody @Valid MyUser updatedUser) {
        MyUserResponse myUserResponse = myUserManagementService.updateUser(id, updatedUser);
        return ResponseEntity.ok(myUserResponse);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<MyUserResponse> register(@RequestBody @Valid MyUserDto myUserDto) {
        MyUserResponse myUserResponse = myUserManagementService.register(myUserDto);
        return ResponseEntity.ok(myUserResponse);
    }

    @PostMapping("/auth/login")
    //The controller sends the login details to the service layer
    // ➡️ A JWT token and refresh token are generated
    // ➡️ The tokens are returned to the client for secure communication.
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid MyUserDto myUserDto) {
        return ResponseEntity.ok(myUserManagementService.login(myUserDto));
    }

    @PostMapping("/auth/refresh")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody LoginResponse refreshTokenRequest) throws UserNotFoundException {
        return ResponseEntity.ok(myUserManagementService.refreshToken(refreshTokenRequest));
    }

    @GetMapping("/{id}")
    public MyUserDtoForGet getUserById(@PathVariable Long id) {
        return myUserManagementService.getUserById(id);
    }

    @GetMapping("/admin/get-all-users")
    public List<MyUserDtoForGet> getAllUsers() {
        return myUserManagementService.getAllUsers();
    }

    @DeleteMapping("/admin/delete/{id}")
    public void deleteUserById(@PathVariable Long id) {
        myUserManagementService.deleteUserById(id);
    }
}

