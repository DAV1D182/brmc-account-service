package com.brmc.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API administrativa para gestionar usuarios de acceso.
 */
@RestController
@RequestMapping("/api/users")
class UserAdminRestController {

    private final UserAdminService userAdminService;

    UserAdminRestController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    List<AppUserResponse> getUsers() {
        return userAdminService.getUsers().stream()
                .map(AppUserResponse::from)
                .toList();
    }

    @PostMapping
    AppUserResponse createUser(@Valid @RequestBody UserRequest request) {
        return AppUserResponse.from(userAdminService.createUser(
                request.username(),
                request.password(),
                request.fullName(),
                request.email(),
                request.role(),
                request.status()
        ));
    }

    @PutMapping("/{username}")
    AppUserResponse updateUser(@PathVariable String username, @Valid @RequestBody UserRequest request) {
        return AppUserResponse.from(userAdminService.updateUser(
                username,
                request.password(),
                request.fullName(),
                request.email(),
                request.role(),
                request.status()
        ));
    }

    @PostMapping("/{username}/activate")
    AppUserResponse activateUser(@PathVariable String username) {
        return AppUserResponse.from(userAdminService.activate(username));
    }

    @PostMapping("/{username}/deactivate")
    AppUserResponse deactivateUser(@PathVariable String username) {
        return AppUserResponse.from(userAdminService.deactivate(username));
    }

    record UserRequest(
            @NotBlank(message = "username es obligatorio")
            @Size(max = 60, message = "username no puede superar 60 caracteres")
            String username,

            @Size(max = 120, message = "password no puede superar 120 caracteres")
            String password,

            @NotBlank(message = "fullName es obligatorio")
            @Size(max = 120, message = "fullName no puede superar 120 caracteres")
            String fullName,

            @Size(max = 160, message = "email no puede superar 160 caracteres")
            String email,

            AppRole role,

            AppUserStatus status
    ) {
    }
}
