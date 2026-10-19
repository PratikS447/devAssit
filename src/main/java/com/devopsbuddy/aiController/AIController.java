package com.devopsbuddy.aiController;

import com.devopsbuddy.service.CommandExecutor;
import com.devopsbuddy.service.OllamaService;
import com.devopsbuddy.model.ApprovalRequest;
import com.devopsbuddy.model.AskRequest;
import com.devopsbuddy.model.AskResponse;
import com.devopsbuddy.model.CommandExecutionResult;
import com.devopsbuddy.model.CommandOperationView;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AIController {
    private final OllamaService ollamaService;
    private final IntentDetector detector;
    private final CommandExecutor executor;

    public AIController(OllamaService ollamaService, IntentDetector detector, CommandExecutor executor) {
        this.ollamaService = ollamaService;
        this.detector = detector;
        this.executor = executor;
    }

    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        String prompt = request.prompt().trim();
        IntentType type = detector.detect(prompt);
        String answer = ollamaService.askOllama(prompt, type == IntentType.COMMAND);

        return new AskResponse(
                type.name(),
                answer,
                type == IntentType.COMMAND ? "NOT_EXECUTED" : "NOT_APPLICABLE"
        );
    }

    @GetMapping("/commands")
    public List<CommandOperationView> availableCommands() {
        return executor.availableOperations();
    }

    @PostMapping("/commands/{operationId}/approve")
    public CommandExecutionResult approveAndExecute(
            @PathVariable String operationId,
            @Valid @RequestBody ApprovalRequest approval) {
        if (!Boolean.TRUE.equals(approval.approved())) {
            throw new CommandExecutor.ApprovalRequiredException();
        }
        return executor.executeApproved(operationId);
    }
}
