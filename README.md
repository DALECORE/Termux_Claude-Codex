# Termux Claude Codex — GitHub App Login

This repository includes a small Ruby/Sinatra GitHub App web-application OAuth flow.

## Setup

1. Register a GitHub App and set its **User authorization callback URL** to:

   `http://localhost:4567/github/callback`

2. Install dependencies:

   ```sh
   bundle install
   ```

3. Create local secrets:

   ```sh
   cp .env.example .env
   ```

   Fill in `CLIENT_ID` and `CLIENT_SECRET`. The `.env` file is ignored by Git.

4. Start the server:

   ```sh
   bundle exec ruby app.rb
   ```

5. Open `http://localhost:4567` and select **Login with GitHub**.

## Configuration

- `CLIENT_ID`: GitHub App client ID.
- `CLIENT_SECRET`: GitHub App client secret.
- `CALLBACK_URL`: registered OAuth callback URL.
- `SESSION_SECRET`: optional stable session secret for deployment.

## Security

- Client secrets are never committed; `.env` is ignored.
- Each login uses a random OAuth state value and validates it on callback.
- The GitHub access token is kept server-side and is never rendered or logged.
- Production deployments should use HTTPS and a real secret manager.
- For a mobile/CLI-only application, GitHub's device flow may be more suitable than a localhost web flow.
