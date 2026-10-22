# LangGraph4j + Spring AI OpenAI Example

A minimal Spring Boot application where LangGraph4j routes a user message
through a graph node that calls an OpenAI chat model using Spring AI.

## Requirements

- Java 17 or later
- Maven 3.6 or later
- An OpenAI API key

## Configure and run

Set your key in the current PowerShell session (do not commit it):

```powershell
$env:OPENAI_API_KEY = "your-openai-api-key"
```

Start the application:

```powershell
mvn spring-boot:run
```

The default model is `gpt-4o-mini`. Send a message to the graph:

```powershell
curl.exe -X POST http://localhost:8080/chat `
  -H "Content-Type: application/json" `
  -d '{ "message": "Explain LangGraph in one sentence." }'
```

The response contains the model-generated answer:

```json
{"response":"..."}
```

The `/chat` controller passes the input to the compiled LangGraph4j graph. Its
`chat` node uses Spring AI's `ChatClient` to call OpenAI and returns the
response from the graph's state.

## Test

```powershell
mvn test
```

The test mocks `ChatClient`, so it does not make a paid OpenAI API request.
