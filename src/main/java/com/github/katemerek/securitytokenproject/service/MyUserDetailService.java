package com.github.katemerek.securitytokenproject.service;

import com.github.katemerek.securitytokenproject.model.MyUser;
import com.github.katemerek.securitytokenproject.repository.MyUserRepository;
import com.github.katemerek.securitytokenproject.security.MyUserDetails;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailService implements UserDetailsService {
    private final MyUserRepository myUserRepository;
    private final LoginAttemptService loginAttemptService;

    public MyUserDetailService(MyUserRepository myUserRepository, LoginAttemptService loginAttemptService) {
        this.myUserRepository = myUserRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public MyUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MyUser myUser = myUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

        if (loginAttemptService.isAccountLocked(myUser)) {
            throw new LockedException("Account is locked");
        }

        return new MyUserDetails(myUser);
    }
}
