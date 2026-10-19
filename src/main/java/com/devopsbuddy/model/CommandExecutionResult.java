package com.devopsbuddy.model;

import java.time.Instant;

public record CommandExecutionResult(
        String executionId,
        String operationId,
        String command,
        Instant completedAt,
        int exitCode,
        String output,
        boolean outputTruncated
) {
}
