package com.devopsbuddy.service;

import com.devopsbuddy.model.CommandExecutionResult;
import com.devopsbuddy.model.CommandOperationView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CommandExecutor {
    private static final int MAX_OUTPUT_BYTES = 16 * 1024;
    private final boolean executionEnabled;
    private final Duration timeout;

    public CommandExecutor(
            @Value("${devopsbuddy.execution.enabled:false}") boolean executionEnabled,
            @Value("${devopsbuddy.execution.timeout:10s}") Duration timeout
    ) {
        this.executionEnabled = executionEnabled;
        this.timeout = timeout;
    }

    public List<CommandOperationView> availableOperations() {
        return Arrays.stream(CommandOperation.values())
                .map(operation -> new CommandOperationView(
                        operation.id(),
                        operation.description(),
                        operation.displayCommand()
                ))
                .toList();
    }

    public CommandExecutionResult executeApproved(String operationId) {
        if (!executionEnabled) {
            throw new ExecutionDisabledException();
        }

        CommandOperation operation = CommandOperation.fromId(operationId)
                .orElseThrow(UnknownOperationException::new);
        Process process;
        try {
            process = new ProcessBuilder(operation.arguments())
                    .redirectErrorStream(true)
                    .start();
        } catch (IOException e) {
            throw new CommandExecutionException();
        }

        AtomicBoolean outputTruncated = new AtomicBoolean(false);
        FutureTask<String> outputTask = new FutureTask<>(
                () -> readBoundedOutput(process.getInputStream(), outputTruncated)
        );
        Thread outputReader = new Thread(outputTask, "devopsbuddy-command-output");
        outputReader.setDaemon(true);
        outputReader.start();

        try {
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                process.waitFor();
                outputReader.join(1000);
                throw new CommandTimeoutException();
            }

            String output = outputTask.get(1, TimeUnit.SECONDS);
            return new CommandExecutionResult(
                    UUID.randomUUID().toString(),
                    operation.id(),
                    operation.displayCommand(),
                    Instant.now(),
                    process.exitValue(),
                    output,
                    outputTruncated.get()
            );
        } catch (CommandTimeoutException e) {
            throw e;
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new CommandExecutionException();
        } catch (Exception e) {
            process.destroyForcibly();
            throw new CommandExecutionException();
        }
    }

    private String readBoundedOutput(InputStream input, AtomicBoolean truncated) throws IOException {
        try (input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int count;
            while ((count = input.read(buffer)) != -1) {
                int remaining = MAX_OUTPUT_BYTES - output.size();
                int bytesToKeep = Math.min(count, Math.max(remaining, 0));
                if (bytesToKeep > 0) {
                    output.write(buffer, 0, bytesToKeep);
                }
                if (bytesToKeep < count) {
                    truncated.set(true);
                }
            }
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    public static class ExecutionDisabledException extends RuntimeException {
    }

    public static class UnknownOperationException extends RuntimeException {
    }

    public static class ApprovalRequiredException extends RuntimeException {
    }

    public static class CommandTimeoutException extends RuntimeException {
    }

    public static class CommandExecutionException extends RuntimeException {
    }
}
