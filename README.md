# PIM (Proxy Instagram Manager)

> **"My digital twin. An AI system that handles my DMs so I don't have to."**

## ⚠️ Disclaimer
**This is a personal utility project.**
I am "vibe coding" this. There are no unit tests. There is no CI/CD pipeline. There is only me, caffeine, and a desire to automate my social life. If you clone this and it breaks your phone or gets you ghosted by your crush, that's on you.

## 🧐 What is this?
PIM is an Android-based AI proxy. It lives on my phone, listens for incoming Instagram notifications, and uses an LLM (Large Language Model) to generate context-aware replies based on my personality.

It's basically an automated "me" that runs 24/7.

## 📁 Project Structure
```
PIM/
├── android/                 # The "Body" - Android App
│   └── app/
│       └── src/main/
│           ├── java/com/pim/
│           │   ├── MainActivity.java
│           │   ├── service/
│           │   │   └── PimNotificationService.java  # The Spy
│           │   ├── api/
│           │   │   ├── PimApiService.java           # Retrofit interface
│           │   │   └── PimApiClient.java            # HTTP client
│           │   └── model/
│           │       ├── ChatRequest.java
│           │       └── ChatResponse.java
│           ├── res/
│           └── AndroidManifest.xml
│
└── backend/                 # The "Brain" - Bun Server
    └── src/
        ├── index.ts         # Elysia server + routes
        ├── gemini.ts        # AI integration
        └── db/
            ├── index.ts     # Drizzle client
            └── schema.ts    # Database schema
```

## ⚙️ How it Works (The "Architecture")
The system is split into two parts: The **Body** (Android) and the **Brain** (Server).

1.  **The Interceptor (Android):** A background service uses `NotificationListenerService` to catch notifications from specific packages (e.g., `com.instagram.android`).
2.  **The Transport:** The app extracts the sender and message text and POSTs it to my private backend.
3.  **The Memory (PostgreSQL):** The server retrieves the last $N$ messages from that sender to understand the context.
4.  **The Intelligence (LLM):** The conversation history + a system prompt ("You are Aditya, a CS student, sarcastic but helpful...") is sent to Gemini/OpenAI.
5.  **The Action:** The generated reply is sent back to the phone, which uses the Android `RemoteInput` API to reply directly from the notification bar.

## 🛠️ Tech Stack
* **Mobile:** Android Native (Java/Kotlin)
    * *Why?* Because `NotificationListenerService` requires native permissions.
* **Backend:** Bun (ElysiaJS/Hono)
    * *Why?* It's fast, and I like it.
* **Database:** PostgreSQL (hosted on Railway)
    * *Why?* Need structured relations for chat logs.
* **AI:** Gemini 1.5 Flash / GPT-4o
    * *Why?* Cheap, fast, and smart enough to mimic me.

## 🚀 Roadmap / To-Do
- [x] **Phase 1: The Ears**
    - [x] Android Service to intercept notifications.
    - [x] Log output to verify data capture.
- [x] **Phase 2: The Brain**
    - [x] Set up Bun server with Elysia.
    - [x] Connect PostgreSQL database (Drizzle ORM).
    - [x] Integrate Gemini API.
- [x] **Phase 3: The Mouth**
    - [x] Android `RemoteInput` implementation to auto-reply.
    - [ ] Handle "Do Not Disturb" logic (so I don't reply at 4 AM).
- [ ] **Phase 4: Personality Tuning**
    - [ ] "Roast Mode" implementation.
    - [ ] Specific filters for "VIP" contacts.

## 🔧 Setup

### Backend
```bash
cd backend
cp .env.example .env
# Edit .env with your GEMINI_API_KEY and DATABASE_URL
bun install
bun run db:push
bun run dev
```

### Android
1. Open `android/` folder in Android Studio (NOT the root folder)
2. Update `BASE_URL` in `PimApiClient.java`:
   - Emulator: `http://10.0.2.2:3000/` (default)
   - Physical device: Use your PC's IP (run `ipconfig`)
3. Build and install APK
4. Grant Notification Access permission

### Quick Test
```bash
# Test Gemini (no DB required)
curl -X POST http://localhost:3000/test \
  -H "Content-Type: application/json" \
  -d '{"message": "hey whats up"}'
```

## 📄 License
MIT. Do whatever you want with it, just don't blame me, Chears☕.
