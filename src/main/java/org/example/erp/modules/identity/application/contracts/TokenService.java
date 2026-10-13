package org.example.erp.modules.identity.application.contracts;

import java.util.Optional;

public interface TokenService {
    String issue(String username);

    Optional<String> usernameFrom(String token);
}
