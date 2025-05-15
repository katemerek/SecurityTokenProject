package com.github.katemerek.securitytokenproject.repository;

import com.github.katemerek.securitytokenproject.model.MyUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyUserRepository extends JpaRepository<MyUser, Long> {
Optional<MyUser> findByUsername(String username);

    boolean existsByUsername(@Size(max = 60) @NotBlank String username);

    List<MyUser> findByIsAccountNonLockedFalse();

    @Modifying
    @Query("UPDATE MyUser u SET u.failedAttempts = :failedAttempts WHERE u.username = :username")
    void updateFailedAttempts(@Param("failedAttempts") int failedAttempts,
                              @Param("username") String username);
}