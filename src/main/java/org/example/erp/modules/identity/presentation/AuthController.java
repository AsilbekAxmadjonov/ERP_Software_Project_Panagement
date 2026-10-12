package org.example.erp.modules.identity.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.example.erp.modules.identity.application.actions.GetUserByUsername;
import org.example.erp.modules.identity.application.actions.LoginUser;
import org.example.erp.modules.identity.application.dto.LoginResultDTO;
import org.example.erp.modules.identity.application.dto.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
class AuthController {
    private final LoginUser loginUser;
    private final GetUserByUsername getUser;

    AuthController(LoginUser loginUser, GetUserByUsername getUser) {
        this.loginUser = loginUser;
        this.getUser = getUser;
    }

    record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    @PostMapping("/login")
    LoginResultDTO login(@RequestBody @Valid LoginRequest r) {
        return loginUser.handle(r.username(), r.password());
    }

    @GetMapping("/me")
    UserDTO me(Authentication auth) {
        return getUser.handle(auth.getName());
    }
}
