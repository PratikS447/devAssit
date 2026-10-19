package com.devopsbuddy.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The only commands the application is allowed to run. Arguments are fixed in
 * code; request data and model output can never become a process argument.
 */
public enum CommandOperation {
    DOCKER_CONTAINERS(
            "docker-containers",
            "List Docker containers",
            List.of("docker", "ps", "-a")
    ),
    KUBERNETES_PODS(
            "kubernetes-pods",
            "List pods across Kubernetes namespaces",
            List.of("kubectl", "get", "pods", "--all-namespaces")
    ),
    KUBERNETES_DEPLOYMENTS(
            "kubernetes-deployments",
            "List deployments across Kubernetes namespaces",
            List.of("kubectl", "get", "deployments", "--all-namespaces")
    ),
    AWS_IDENTITY(
            "aws-identity",
            "Show the AWS identity configured for this runtime",
            List.of("aws", "sts", "get-caller-identity")
    );

    private final String id;
    private final String description;
    private final List<String> arguments;

    CommandOperation(String id, String description, List<String> arguments) {
        this.id = id;
        this.description = description;
        this.arguments = List.copyOf(arguments);
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    }

    public List<String> arguments() {
        return arguments;
    }

    public String displayCommand() {
        return String.join(" ", arguments);
    }

    public static Optional<CommandOperation> fromId(String id) {
        return Arrays.stream(values()).filter(operation -> operation.id.equals(id)).findFirst();
    }
}
