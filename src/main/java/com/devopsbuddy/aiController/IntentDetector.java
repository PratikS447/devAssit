package com.devopsbuddy.aiController;

import org.springframework.stereotype.Component;

@Component
public class IntentDetector {
    public IntentType detect(String input){
        input = input.toLowerCase();

        if(input.contains("deploy") ||
        input.contains("docker") ||
        input.contains("kubectl") ||
        input.contains("aws") ||
        input.contains("logs")){
            return IntentType.COMMAND;
        }

        if(input.contains("how") ||
        input.contains("what") ||
        input.contains("why")){
            return IntentType.QUESTION;
        }

        return IntentType.UNKNOWN;
    }
}
