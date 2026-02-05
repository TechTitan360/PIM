# PIM (Personal Intelligence Module)

> **"My digital twin. An AI system that handles my DMs so I don't have to."**

![Status](https://img.shields.io/badge/Status-Active_Dev-success?style=for-the-badge) ![Utility](https://img.shields.io/badge/Utility-Maximum-blue?style=for-the-badge)

## 🧐 The Mission
PIM is an automated proxy designed to bridge the gap between my social obligations and my need for deep work. It lives on my Android phone, intercepts Instagram notifications, and uses an LLM to generate context-aware replies that sound exactly like me.

It’s not a chatbot. It’s a **Personal Intelligence Module**.

## 📜 The Manifesto
This project follows a simple rule: **Utility > Perfection.**

> *"When you build for yourself, 'Done' is better than 'Perfect.' If the code is messy but it saves you 10 minutes a day, it’s good code. We optimize for utility, not for a code review."*

PIM isn't built to be sold. It's built to solve a specific problem in my life, using whatever tools get the job done fastest.

## 🏗️ How It Works

```mermaid
📱 Instagram DM arrives
        ↓
🔔 Android NotificationListenerService intercepts
        ↓
🛡️ Anti-feedback checks (cooldown, self-reply, duplicate)
        ↓
📤 POST to https://pim-backend-auhy.onrender.com/chat
        ↓
🗄️ Backend fetches last 10 messages for THIS sender only
        ↓
🤖 Gemini generates reply (with 5-key rotation on failure)
        ↓
💾 Saves both messages to database
        ↓
📥 Reply sent back to Android
        ↓
✉️ Auto-reply via notification RemoteInput
        ↓
🗑️ Notification dismissed

```
## 📄 License
MIT. Do whatever you want with it, just don't blame me, Chears☕...
