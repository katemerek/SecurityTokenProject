package com.github.katemerek.securitytokenproject.service;

import com.github.katemerek.securitytokenproject.dto.LoginResponse;
import com.github.katemerek.securitytokenproject.dto.MyUserDto;
import com.github.katemerek.securitytokenproject.dto.MyUserDtoForGet;
import com.github.katemerek.securitytokenproject.dto.MyUserResponse;
import com.github.katemerek.securitytokenproject.exception.UserNotFoundException;
import com.github.katemerek.securitytokenproject.mapper.MyUserMapperForGet;
import com.github.katemerek.securitytokenproject.model.MyUser;
import com.github.katemerek.securitytokenproject.repository.MyUserRepository;
import com.github.katemerek.securitytokenproject.security.JWTUtils;
import com.github.katemerek.securitytokenproject.security.MyUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MyUserManagementService {
    private final MyUserRepository myUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final MyUserMapperForGet myUserMapperForGet;

    public MyUserManagementService(MyUserRepository myUserRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JWTUtils jwtUtils, MyUserMapperForGet myUserMapperForGet) {
        this.myUserRepository = myUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.myUserMapperForGet = myUserMapperForGet;
    }

    @Transactional
    public MyUserResponse register(MyUserDto myUserDto) {
        if (!myUserRepository.existsByUsername(myUserDto.getUsername())) {
            throw new UsernameNotFoundException("User with username" + myUserDto.getUsername() + " not found");
        } else {
            MyUser myUser = new MyUser();
            myUser.setUsername(myUserDto.getUsername());
            myUser.setPassword(passwordEncoder.encode(myUserDto.getPassword()));
            myUser.setRole(myUserDto.getRole());

            myUser = myUserRepository.save(myUser);
            return new MyUserResponse(myUser.getId(), "New User was created successfully");
        }
    }

    public LoginResponse login(MyUserDto myUserDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        myUserDto.getUsername(),
                        myUserDto.getPassword()
                ));

        MyUser user = myUserRepository.findByUsername(myUserDto.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User with username" + myUserDto.getUsername() + " not found"));
        MyUserDetails userDetails = new MyUserDetails(user);
        String jwt = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), userDetails);
        return new LoginResponse(HttpStatus.OK, jwt, refreshToken, user.getRole().name(), "24Hrs", "Successfully logged in");
    }

    public LoginResponse refreshToken(LoginResponse refreshTokenRequest) throws UserNotFoundException {
        String username = jwtUtils.extractUsername(refreshTokenRequest.token());
        MyUser user = myUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User with username" + username + " not found"));
        MyUserDetails userDetails = new MyUserDetails(user);
        if (jwtUtils.isTokenValid(refreshTokenRequest.token(), userDetails)) {
            String newJwt = jwtUtils.generateToken(userDetails);
            return new LoginResponse(HttpStatus.OK, newJwt, refreshTokenRequest.refreshToken(), user.getRole().name(),
                    "24Hrs", "Successfully refreshed the token");
        }
        return new LoginResponse(HttpStatus.UNAUTHORIZED, refreshTokenRequest.token(), refreshTokenRequest.refreshToken(),
                user.getRole().name(), "0", "Invalid Token");
    }

    public List<MyUserDtoForGet> getAllUsers() {
        return myUserRepository.findAll()
                .stream()
                .map(myUserMapperForGet::toMyUserDtoForGet)
                .toList();
    }

    public MyUserDtoForGet getUserById(Long id) {
        return myUserMapperForGet.toMyUserDtoForGet(myUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    public void deleteUserById(Long id) {
        if (!myUserRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        myUserRepository.deleteById(id);
    }

    public MyUserResponse updateUser(Long id, MyUser updatedUser) {
        MyUser existingUser = myUserRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setIsAccountNonLocked(updatedUser.getIsAccountNonLocked());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        myUserRepository.save(existingUser);
        return new MyUserResponse(existingUser.getId(), "User was updated successfully");
    }
}
