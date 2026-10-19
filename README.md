# DevOpsBuddy

DevOpsBuddy is a Java 21 / Spring Boot API for DevOps questions, command suggestions, and explicitly approved, read-only inspections.

## Safety model

- `POST /api/ask` asks the configured Ollama service for a response. A command request is returned as a **suggestion** and is never executed by this endpoint.
- `GET /api/commands` lists the small, fixed set of operations the server supports.
- An operator can explicitly approve one listed operation with `POST /api/commands/{operationId}/approve` and `{"approved":true}`. The request cannot supply a shell command or arguments.
- The executor uses `ProcessBuilder` with fixed argument lists—never a shell—and limits execution time and captured output.
- Execution is disabled unless `COMMAND_EXECUTION_ENABLED=true`. The API also requires an `X-API-Key` of at least 32 characters. Keep this key in a secrets manager and send requests only over HTTPS.
- The supported operations are read-only: list Docker containers, Kubernetes pods or deployments, and show the AWS identity configured for the runtime.

**Execution runs on the same host/container as this API.** Keep it disabled on public or shared deployments until it is connected to an isolated runner with least-privilege credentials. Approval and result records are not persisted, and the API currently uses one shared API key rather than individual user identities; those are gaps to close before a multi-user or higher-risk production rollout.

## Run locally

Requirements: Java 21, Docker only if using the Docker operation, `kubectl` only if using Kubernetes operations, and AWS CLI only if using the AWS identity operation.

```bash
export DEVOPSBUDDY_API_KEY="$(openssl rand -base64 32)"
./mvnw spring-boot:run
```

The API uses Ollama at `http://localhost:11434` by default. Configure the model service and execution gate with environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `DEVOPSBUDDY_API_KEY` | unset | Required for `/api/**`; at least 32 characters |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama server URL |
| `OLLAMA_MODEL` | `deepseek-r1:1.5b` | Model name |
| `OLLAMA_TIMEOUT` | `30s` | Maximum wait for a model response |
| `COMMAND_EXECUTION_ENABLED` | `false` | Explicitly enables approved fixed operations |
| `COMMAND_EXECUTION_TIMEOUT` | `10s` | Maximum time for each operation |

Health and readiness probes are available without an API key at `/actuator/health`, `/actuator/health/liveness`, and `/actuator/health/readiness`. No other actuator endpoints are exposed.

## API

All `/api/**` routes require the `X-API-Key` header.

Ask a question or request a command suggestion:

```http
POST /api/ask
Content-Type: application/json
X-API-Key: <your key>

{"prompt":"How do I list the pods in my cluster?"}
```

List available operations before asking for approval:

```http
GET /api/commands
X-API-Key: <your key>
```

Approve one of the returned, fixed operations:

```http
POST /api/commands/kubernetes-pods/approve
Content-Type: application/json
X-API-Key: <your key>

{"approved":true}
```

An unset API key or disabled execution gate fails closed. Errors use stable error codes and request IDs; internal exception messages and prompt contents are not returned to clients or logged.

## Tests and CI

Run `./mvnw --batch-mode verify`. GitHub Actions runs the same verification on pushes and pull requests using Java 21.
