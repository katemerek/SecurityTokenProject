package com.github.katemerek.securitytokenproject.dto;

import com.github.katemerek.securitytokenproject.enumiration.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * DTO for {@link com.github.katemerek.securitytokenproject.model.MyUser}
 */
public class MyUserDto {
    @Size(max = 60)
    @NotBlank
    private final String username;
    @Size(max = 200)
    @NotBlank
    private final String password;
    private final Role role;

    public MyUserDto(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyUserDto entity = (MyUserDto) o;
        return Objects.equals(this.username, entity.username) &&
                Objects.equals(this.password, entity.password) &&
                Objects.equals(this.role, entity.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, role);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "username = " + username + ", " +
                "password = " + password + ", " +
                "role = " + role + ")";
    }
}