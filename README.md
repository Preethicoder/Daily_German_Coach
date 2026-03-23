# Daily German Coach API 🇩🇪

A Spring Boot REST API that helps users learn German vocabulary using Google Gemini AI for intelligent translations, example sentences, and grammar validation.

## 🌟 Features

- **AI-Powered Translation**: Translate German words to English with context
- **Smart Example Sentences**: Get real-world German sentence examples for each word
- **Grammar Validation**: Check if your German sentences are grammatically correct
- **Vocabulary Tracking**: Save and retrieve learned words
- **Random Practice**: Get random words for review sessions

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Google Gemini API key ([Get one here](https://ai.google.dev/))

### Installation

1. **Clone the repository**
```bash
git clone <your-repo-url>
cd "Daily German Coach API"
```

2. **Set up environment variables**
```bash
# Copy the example .env file
cp .env.example .env

# Edit .env and add your Gemini API key
export AI_PROVIDER_KEY="your-api-key-here"
export AI_PROVIDER_URL="https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
export AI_PROVIDER_USE_KEY_QUERY=true
export GCP_PROJECT_ID="your-project-id"
```

3. **Load environment variables and run**
```bash
source .env
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

## 📚 API Endpoints

### 1. Enrich and Save Word
Translate a German word and save it with an example sentence.

```bash
curl -X POST http://localhost:8080/api/german/enrich \
  -H "Content-Type: application/json" \
  -d '{"german": "Baum"}'
```

**Response:**
```json
{
  "id": 1,
  "german": "Baum",
  "english": "tree",
  "exampleSentence": "Der Baum im Garten ist sehr groß.",
  "createdAt": "2026-03-23T10:30:00"
}
```

### 2. Get All Words
Retrieve all saved vocabulary words.

```bash
curl http://localhost:8080/api/german/words
```

**Response:**
```json
[
  {
    "id": 1,
    "german": "Baum",
    "english": "tree",
    "exampleSentence": "Der Baum im Garten ist sehr groß.",
    "createdAt": "2026-03-23T10:30:00"
  },
  {
    "id": 2,
    "german": "Haus",
    "english": "house",
    "exampleSentence": "Das Haus ist sehr schön.",
    "createdAt": "2026-03-23T10:31:00"
  }
]
```

### 3. Get Random Word
Get a random word for practice.

```bash
curl http://localhost:8080/api/german/random
```

**Response:**
```json
{
  "id": 1,
  "german": "Baum",
  "english": "tree",
  "exampleSentence": "Der Baum im Garten ist sehr groß.",
  "createdAt": "2026-03-23T10:30:00"
}
```

### 4. Check Sentence
Validate your German sentence with AI feedback.

```bash
curl -X POST http://localhost:8080/api/german/check-sentence \
  -H "Content-Type: application/json" \
  -d '{
    "german": "Baum",
    "userSentence": "Der Baum ist groß"
  }'
```

**Response:**
```json
{
  "correct": true,
  "feedback": "The sentence is grammatically correct and properly uses the word 'Baum'.",
  "suggestion": ""
}
```

**Example with correction:**
```json
{
  "correct": false,
  "feedback": "The verb conjugation is incorrect. 'sein' should be conjugated as 'ist' not 'sind' for singular subject.",
  "suggestion": "Der Baum ist groß."
}
```

## 🏗️ Architecture

### Tech Stack

- **Framework**: Spring Boot 3.1.3
- **Language**: Java 17+
- **Database**: H2 (in-memory) - easily switchable to PostgreSQL
- **AI Provider**: Google Gemini 2.5 Flash
- **HTTP Client**: Spring WebClient (reactive)
- **Build Tool**: Maven
- **ORM**: Spring Data JPA with Hibernate

### Project Structure

```
src/main/java/com/dailygermancoach/
├── controller/
│   └── GermanController.java       # REST endpoints
├── service/
│   ├── GermanService.java          # Business logic
│   ├── AiService.java              # AI integration (WebClient)
│   └── GenAiSdkService.java        # Optional SDK implementation
├── model/
│   ├── Word.java                   # JPA entity
│   ├── WordRequest.java            # DTOs
│   ├── SentenceRequest.java
│   └── SentenceResponse.java
└── repository/
    └── WordRepository.java         # JPA repository
```

### Key Components

#### AiService
Handles all AI interactions using Spring WebClient to call Gemini API.

**Features:**
- Configurable authentication (API key or OAuth)
- JSON response parsing with markdown cleanup
- Fallback dictionary for common words
- Error handling and logging

#### GermanService
Business logic layer that coordinates between controllers, repositories, and AI service.

#### GermanController
REST API endpoints with proper HTTP methods and status codes.

## 🔧 Configuration

### Application Properties

```properties
# AI Provider (Gemini via REST API)
ai.provider.url=${AI_PROVIDER_URL}
ai.provider.key=${AI_PROVIDER_KEY}
ai.provider.type=gemini
ai.provider.useKeyQuery=true
ai.provider.useSdk=false

# Database (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop

# Logging
logging.level.com.dailygermancoach=INFO
```

### Authentication Modes

#### WebClient Mode (Current - Recommended)
Uses API key for simple, direct Gemini API access.

```bash
export AI_PROVIDER_KEY="your-api-key"
export AI_PROVIDER_USE_KEY_QUERY=true
export AI_PROVIDER_USE_SDK=false
```

#### SDK Mode (Optional)
Uses Vertex AI SDK with Application Default Credentials (ADC).

```bash
export AI_PROVIDER_USE_SDK=true
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
export GCP_PROJECT_ID="your-project-id"
```

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Without Tests
```bash
mvn -DskipTests=false package
```

### Test Coverage
- Unit tests for service layer
- Integration tests for API endpoints
- Fallback dictionary tests (no API key needed)

## 📖 Learning Path Example

Here's a typical learning workflow:

```bash
# 1. Learn a new word
curl -X POST http://localhost:8080/api/german/enrich \
  -H "Content-Type: application/json" \
  -d '{"german": "Schule"}'

# 2. Try writing a sentence
curl -X POST http://localhost:8080/api/german/check-sentence \
  -H "Content-Type: application/json" \
  -d '{"german": "Schule", "userSentence": "Ich gehe zur Schule"}'

# 3. Review all learned words
curl http://localhost:8080/api/german/words

# 4. Practice with random word
curl http://localhost:8080/api/german/random
```

## 🚀 Deployment

### Local Development
```bash
source .env
mvn spring-boot:run
```

### Build JAR
```bash
mvn clean package
java -jar target/dailygermancoach-0.0.1-SNAPSHOT.jar
```

### Docker (Optional)
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Switch to PostgreSQL

Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/germancoach
spring.datasource.username=youruser
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

Add dependency to `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

## 🐛 Troubleshooting

### API Key Issues
```bash
# Verify environment variable is set
echo $AI_PROVIDER_KEY

# Check logs for authentication errors
tail -f logs/application.log
```

### Empty Database Fields
The AI response parsing has been fixed to properly extract nested JSON and remove markdown code blocks. If you still see empty fields, check logs for parsing errors.

### Port Already in Use
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Or change port in application.properties
server.port=8081
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 🙏 Acknowledgments

- Google Gemini AI for translation and grammar checking
- Spring Boot team for the amazing framework
- German language community for inspiration

## 📧 Contact

For questions or feedback, please open an issue on GitHub.

---

**Built with ❤️ using Java, Spring Boot, and AI**
