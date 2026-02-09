package com.devopsbuddy.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {
    private final WebClient webClient;

    public OllamaService(){
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public String askOllama(String prompt){
        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek-r1:1.5b");
        body.put("stream", false);

        String systemPrompt =
                "SYSTEM RULES:\n" +
                        "1. If user asks for a command → respond ONLY with the exact terminal command.\n" +
                        "2. Do NOT add explanation, steps, or text.\n" +
                        "3. Output must be plain command only.\n\n" +
                        "USER QUESTION: " + prompt;


        body.put("prompt", systemPrompt);

        String result = webClient.post()
                .uri("/api/generate")
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper()
                            .readTree(result);

            String answer = node.get("response").asText();

            answer = answer.replace("`", "").trim();

            String[] lines = answer.split("\\n");

            for (String line: lines){
                line = line.trim().toLowerCase();

                if (line.startsWith("docker") ||
                        line.startsWith("kubectl") ||
                        line.startsWith("aws") ||
                        line.startsWith("helm")){
                    return line;
                }
            }

            if (prompt.toLowerCase().contains("list") &&
            prompt.toLowerCase().contains("container")){
                return "docker ps";
            }

            return lines[lines.length-1].trim();

        } catch (Exception e) {
            return "ERROR: "+e.getMessage();
        }
    }
}
