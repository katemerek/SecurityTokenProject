package com.github.katemerek.securitytokenproject.service;

import com.github.katemerek.securitytokenproject.model.MyUser;
import com.github.katemerek.securitytokenproject.repository.MyUserRepository;
import com.github.katemerek.securitytokenproject.security.MyUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailService implements UserDetailsService {
    private final MyUserRepository myUserRepository;

    public MyUserDetailService(MyUserRepository myUserRepository) {
        this.myUserRepository = myUserRepository;
    }

    @Override
    public MyUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MyUser myUser = myUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new MyUserDetails(myUser);
    }
}
