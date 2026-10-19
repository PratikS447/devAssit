package com.devopsbuddy.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskRequest(
        @NotBlank(message = "prompt must not be blank")
        @Size(max = 2000, message = "prompt must be at most 2000 characters")
        String prompt
) {
}
