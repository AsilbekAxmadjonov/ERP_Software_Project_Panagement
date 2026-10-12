package org.example.erp.modules.identity.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.erp.modules.identity.application.actions.CreateUser;
import org.example.erp.modules.identity.application.actions.ListUsers;
import org.example.erp.modules.identity.application.actions.UpdateUser;
import org.example.erp.modules.identity.application.commands.CreateUserCommand;
import org.example.erp.modules.identity.application.commands.UpdateUserCommand;
import org.example.erp.modules.identity.application.dto.UserDTO;
import org.example.erp.modules.identity.application.enums.UserRole;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
class UserController {
    private final ListUsers listUsers;
    private final CreateUser createUser;
    private final UpdateUser updateUser;

    UserController(ListUsers listUsers, CreateUser createUser, UpdateUser updateUser) {
        this.listUsers = listUsers;
        this.createUser = createUser;
        this.updateUser = updateUser;
    }

    record CreateRequest(@NotBlank String username, @NotBlank @Size(min = 8) String password,
                         String fullName, @NotNull UserRole role) {}

    record UpdateRequest(String fullName, UserRole role, Boolean active, @Size(min = 8) String password) {}

    @GetMapping
    List<UserDTO> list() {
        return listUsers.handle();
    }

    @PostMapping
    UserDTO create(@RequestBody @Valid CreateRequest r) {
        return createUser.handle(new CreateUserCommand(r.username(), r.password(), r.fullName(), r.role()));
    }

    @PutMapping("/{id}")
    UserDTO update(@PathVariable long id, @RequestBody @Valid UpdateRequest r) {
        return updateUser.handle(new UpdateUserCommand(id, r.fullName(), r.role(), r.active(), r.password()));
    }
}
