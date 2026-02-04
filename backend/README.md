# PIM Backend

The "Brain" - Bun + Elysia + Gemini server that processes Instagram DMs.

## Setup

1. Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

2. Fill in your environment variables:
   - `GEMINI_API_KEY` - Get from [Google AI Studio](https://aistudio.google.com/app/apikey)
   - `DATABASE_URL` - PostgreSQL connection string (Railway provides this)

3. Install dependencies:
```bash
bun install
```

4. Push database schema:
```bash
bun run db:push
```

5. Run the server:
```bash
bun run dev
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Health check |
| POST | `/chat` | Process incoming DM (main endpoint) |
| POST | `/test` | Test Gemini without database |
| GET | `/history/:sender` | Get conversation history |

## Testing

Quick test with curl:
```bash
curl -X POST http://localhost:3000/test \
  -H "Content-Type: application/json" \
  -d '{"message": "hey whats up"}'
```

Full chat test:
```bash
curl -X POST http://localhost:3000/chat \
  -H "Content-Type: application/json" \
  -d '{"sender": "test_user", "message": "hey whats up"}'
```

## Deployment (Railway)

1. Connect your GitHub repo to Railway
2. Add environment variables in Railway dashboard
3. Deploy!

The server will automatically start on the assigned `PORT`.
