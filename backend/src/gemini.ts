import { GoogleGenerativeAI } from '@google/generative-ai';

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY!);

// The "Brain" - Gemini 2.5 Flash (fast & capable)
const model = genAI.getGenerativeModel({ model: 'gemini-2.5-flash' });

/**
 * The System Prompt - This is YOUR personality.
 * Tweak this to match how you actually text.
 */
const SYSTEM_PROMPT = `You are Aditya, a CS student who loves coding, anime, and coffee.
You're responding to Instagram DMs on behalf of yourself.

Your personality:
- Casual and chill, but not overly friendly with strangers
- Sarcastic humor, but know when to be genuine
- Use lowercase mostly, occasional emojis but don't overdo it
- Short responses unless the topic needs more
- You say "lol" and "lmao" sometimes
- If someone's being annoying, be dismissive but not rude
- If it's a close friend, be warmer and more engaged

Rules:
- NEVER reveal you're an AI or automated
- Keep responses natural and conversational
- Match the energy of the other person
- If you don't know something specific about "your" life, be vague
- Don't use fancy punctuation or perfect grammar

Remember: You ARE Aditya. This is not roleplay.`;

export interface ChatContext {
    sender: string;
    message: string;
    history: Array<{ role: 'user' | 'model'; content: string }>;
}

/**
 * Generate a response using Gemini with conversation history.
 */
export async function generateReply(context: ChatContext): Promise<string> {
    const { sender, message, history } = context;

    // Build the conversation for Gemini
    const conversationHistory = history.map(h => ({
        role: h.role,
        parts: [{ text: h.content }],
    }));

    // Start chat with history
    const chat = model.startChat({
        history: [
            {
                role: 'user',
                parts: [{ text: SYSTEM_PROMPT }],
            },
            {
                role: 'model',
                parts: [{ text: 'Got it. I am Aditya now. Ready to reply to DMs.' }],
            },
            ...conversationHistory,
        ],
    });

    // Generate response
    const prompt = `[DM from ${sender}]: ${message}`;
    const result = await chat.sendMessage(prompt);
    const response = result.response.text();

    return response.trim();
}

/**
 * Quick test function - can be called without DB
 */
export async function quickReply(message: string): Promise<string> {
    return generateReply({
        sender: 'Unknown',
        message,
        history: [],
    });
}
