package com.devopsbuddy.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandExecutorTest {
    @Test
    void executionIsDisabledByDefault() {
        CommandExecutor executor = new CommandExecutor(false, Duration.ofSeconds(1));

        assertThrows(
                CommandExecutor.ExecutionDisabledException.class,
                () -> executor.executeApproved("docker-containers")
        );
    }

    @Test
    void unknownOperationsAreRejectedBeforeAProcessStarts() {
        CommandExecutor executor = new CommandExecutor(true, Duration.ofSeconds(1));

        assertThrows(
                CommandExecutor.UnknownOperationException.class,
                () -> executor.executeApproved("docker ps; whoami")
        );
    }

    @Test
    void catalogContainsOnlyFixedReadOnlyOperations() {
        var operations = new CommandExecutor(false, Duration.ofSeconds(1)).availableOperations();

        assertEquals(4, operations.size());
        assertEquals("docker ps -a", operations.get(0).command());
        assertEquals("aws sts get-caller-identity", operations.get(operations.size() - 1).command());
    }
}
