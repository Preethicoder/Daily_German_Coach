Local .env usage (safe instructions)

Do NOT commit real secrets into source control. The repository contains `.env.example` and `.gitignore` already.

1) Create a local `.env` file (in project root) by copying the example:

   cp .env.example .env

2) Edit `.env` and add your real API key (replace the placeholder):

   # open with your editor and set the key
   # AI_PROVIDER_KEY=AIzaSy...

   # do NOT paste the key into an issue, PR, or public chat

3) Load the `.env` into your current shell session (zsh):

   # Option A: source with exported variables for the current shell
   set -a
   source .env
   set +a

   # Option B: export directly (useful in CI or single command)
   # export AI_PROVIDER_KEY=$(grep -E '^AI_PROVIDER_KEY=' .env | cut -d'=' -f2-)

4) Run the app so Spring sees the env var (Spring maps AI_PROVIDER_KEY -> ai.provider.key):

   mvn spring-boot:run

Alternative: pass property on the mvn command line (without storing in files):

   mvn spring-boot:run -Dai.provider.key="$AI_PROVIDER_KEY"

Provider-specific notes for Gemini (Google Generative Models)
---------------------------------------------------------
- Set `ai.provider.type=gemini` (default) in `src/main/resources/application.properties` or via env var.
- Most Google API keys are passed as a query parameter (`?key=YOUR_KEY`) rather than a Bearer token.
   The project uses `ai.provider.useKeyQuery=true` by default so it will attach `?key=...` to requests.
- Example `.env` entries for Gemini:

   AI_PROVIDER_KEY="your-google-api-key"
   AI_PROVIDER_URL="https://generativelanguage.googleapis.com/v1beta2/models/gemini-1.0:generateText"

   # or if your provider uses a different endpoint, set AI_PROVIDER_URL accordingly

Important: If you are using Google Cloud Generative APIs in production, prefer service account
credentials and the official client libraries (which use OAuth2) instead of API keys.

If you want, I can also add a small helper script (`run.sh`) that loads `.env` and runs the app without exporting permanently.
