package com.ocms.backend.dto;

import com.ocms.backend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    private String name;
    @Email
    private String email;
    @NotBlank
    private String password;
    @NotNull
    private Role role;
}
