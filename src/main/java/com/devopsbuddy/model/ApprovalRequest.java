package com.devopsbuddy.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ApprovalRequest(
        @NotNull
        @AssertTrue(message = "approval must be explicitly set to true")
        Boolean approved
) {
}
