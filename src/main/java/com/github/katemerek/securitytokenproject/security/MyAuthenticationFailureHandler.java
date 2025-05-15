package com.github.katemerek.securitytokenproject.security;

import com.github.katemerek.securitytokenproject.repository.MyUserRepository;
import com.github.katemerek.securitytokenproject.service.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private LoginAttemptService loginAttemptService;

    private MyUserRepository userRepository;

    public MyAuthenticationFailureHandler(LoginAttemptService loginAttemptService, MyUserRepository userRepository) {
        this.loginAttemptService = loginAttemptService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception)
            throws IOException, ServletException {
        String username = request.getParameter("username");
        userRepository.findByUsername(username).ifPresent(user -> {
            loginAttemptService.increaseFailedAttempts(user);

            int remainingAttempts = LoginAttemptService.MAX_FAILED_ATTEMPTS - user.getFailedAttempts();
            request.getSession().setAttribute("remainingAttempts", remainingAttempts);
        });

        response.sendRedirect("/login?error=true");
    }
}
