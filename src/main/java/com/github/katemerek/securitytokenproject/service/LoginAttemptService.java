package com.github.katemerek.securitytokenproject.service;

import com.github.katemerek.securitytokenproject.dto.MyUserDtoForGet;
import com.github.katemerek.securitytokenproject.dto.MyUserResponse;
import com.github.katemerek.securitytokenproject.exception.UserNotFoundException;
import com.github.katemerek.securitytokenproject.mapper.MyUserMapperForGet;
import com.github.katemerek.securitytokenproject.model.MyUser;
import com.github.katemerek.securitytokenproject.repository.MyUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class LoginAttemptService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000; // 24 часа

    private final MyUserRepository myUserRepository;
    private final MyUserMapperForGet myUserMapperForGet;

    public LoginAttemptService(MyUserRepository myUserRepository, MyUserMapperForGet myUserMapperForGet) {
        this.myUserRepository = myUserRepository;
        this.myUserMapperForGet = myUserMapperForGet;
    }

    public void increaseFailedAttempts(MyUser user) {
        int newFailedAttempts = user.getFailedAttempts() + 1;
        myUserRepository.updateFailedAttempts(newFailedAttempts, user.getUsername());

        if (newFailedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockUser(user);
        }
    }

    public void lockUser(MyUser user) {
        user.setIsAccountNonLocked(false);
        user.setLockTime(Instant.from(LocalDateTime.now()));
        myUserRepository.save(user);
    }

    public MyUserResponse unlockUser(Long id) {
        MyUser user = myUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setIsAccountNonLocked(true);
        user.setFailedAttempts(0);
        user.setLockTime(null);
        myUserRepository.save(user);
        return new MyUserResponse(user.getId(), "User unlocked succesfully");
    }

    public boolean isAccountLocked(MyUser user) {
        if (user.getLockTime() != null) {
            long lockTime = user.getLockTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return lockTime + LOCK_TIME_DURATION > System.currentTimeMillis();
        }
        return !user.getIsAccountNonLocked();
    }

    public List<MyUserDtoForGet> getLockedUsers() {
        return myUserRepository.findByIsAccountNonLockedFalse()
                .stream()
                .map(myUserMapperForGet::toMyUserDtoForGet)
                .toList();
    }
}