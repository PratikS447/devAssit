package com.devopsbuddy.aiController;

import com.devopsbuddy.service.CommandExecutor;
import com.devopsbuddy.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AIController {
    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private IntentDetector detector;

    @Autowired
    private CommandExecutor executor;

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> req){
        try {
            String prompt = req.get("prompt");

            if (prompt == null || prompt.isBlank()){
                return Map.of(
                        "response", "prompt required"
                );
            }

            IntentType type = detector.detect(prompt);

            System.out.println("Prompt : "+prompt);
            System.out.println("Detector intent : "+type);

            String aiResponse = ollamaService.askOllama(prompt);

            if(type == IntentType.COMMAND){
                String output = executor.execute(aiResponse);

                return Map.of(
                        "intent", "COMMAND",
                        "command", aiResponse,
                        "output", output
                );
            }

            return Map.of(
                    "intent", type.name(),
                    "response", aiResponse
            );
        }catch (Exception e){
            e.printStackTrace();

            return Map.of(
                    "intent","ERROR",
                    "response",e.getMessage()
            );
        }
    }
}
