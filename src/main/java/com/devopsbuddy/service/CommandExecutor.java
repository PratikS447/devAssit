package com.devopsbuddy.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class CommandExecutor {
    private final List<String> allowed = List.of(
            "docker",
            "kubectl",
            "aws"
    );

    private String normalize(String cmd){
        cmd = cmd.trim();

        if(cmd.equals("dockers ls")){
            return "docker ps";
        }

        if(cmd.equals("docker list")){
            return "docker ps";
        }

        return cmd;
    }

    public String execute(String command){
        command = normalize(command);
        if(!isAllowed(command)){
            return "Command not allowed for safety.";
        }

        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("bash", "-c", command);

            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null){
                output.append(line).append("\n");
            }

            while ((line = err.readLine()) != null){
                output.append(line).append("\n");
            }

            process.waitFor();

            return output.toString();
        }catch (Exception e){
            return "Execution error : "+e.getMessage();
        }
    }

    private boolean isAllowed(String cmd){
        cmd = cmd.trim().toLowerCase();

        return cmd.startsWith("docker ") ||
                cmd.equals("docker") ||
                cmd.startsWith("kubectl ") ||
                cmd.startsWith("aws ");
    }
}
