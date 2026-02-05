import 'dotenv/config';
import { GoogleGenerativeAI, GenerativeModel } from '@google/generative-ai';

/**
 * API Key Rotation System
 * - Loads multiple keys from environment
 * - Auto-switches to next key on failure
 * - Cycles through all available keys
 */
const API_KEYS: string[] = [
    process.env.GEMINI_API_KEY,
    process.env.GEMINI_API_KEY_2,
    process.env.GEMINI_API_KEY_3,
    process.env.GEMINI_API_KEY_4,
    process.env.GEMINI_API_KEY_5,
].filter((key): key is string => typeof key === 'string' && key.trim().length > 0);

if (API_KEYS.length === 0) {
    console.error('❌ No Gemini API keys found! Available env vars:', Object.keys(process.env).filter(k => k.includes('GEMINI')));
    throw new Error('No Gemini API keys found! Set GEMINI_API_KEY in .env');
}

// Log which keys are loaded (masked for security)
API_KEYS.forEach((key, index) => {
    const masked = key.substring(0, 8) + '...' + key.substring(key.length - 4);
    console.log(`🔑 Key ${index + 1}: ${masked}`);
});
console.log(`✅ Loaded ${API_KEYS.length} Gemini API key(s)`);

let currentKeyIndex = 0;

/**
 * Get the current API key
 */
function getCurrentKey(): string {
    return API_KEYS[currentKeyIndex];
}

/**
 * Rotate to the next API key
 */
function rotateKey(): void {
    const oldIndex = currentKeyIndex;
    currentKeyIndex = (currentKeyIndex + 1) % API_KEYS.length;
    console.log(`🔄 Rotated API key: ${oldIndex + 1} → ${currentKeyIndex + 1} (of ${API_KEYS.length})`);
}

/**
 * Get a fresh Gemini model instance with current key
 */
function getModel(): GenerativeModel {
    const genAI = new GoogleGenerativeAI(getCurrentKey());
    return genAI.getGenerativeModel({ model: 'gemini-2.5-flash' });
}

/**
 * The System Prompt - This is YOUR personality.
 * Tweak this to match how you actually text.
 */
const SYSTEM_PROMPT = `You are Aditya, a CS student who loves coding, anime, and coffee.
You are responding to Instagram DMs on behalf of yourself.

## PERSONALITY & VIBE
- **Core:** Casual, chill, introverted. You enjoy your solitude but value close friends.
- **Humor:** Sarcastic, dry, sometimes immature. You don't take things too seriously.
- **Interests:**
  - Anime: Dragon Ball Super, Naruto, Demon Slayer, AOT, Spy x Family, Re:Zero, Mushoku Tensei (watched 2x), Darling in the Franxx, A Silent Voice, My Little Oni Girl, Suzume, My Hero Academia.
  - Gaming: A way to escape stress and overthinking.
  - Coding: It's what you do.

## SPEAKING STYLE
- **Formatting:** mostly lowercase. minimal punctuation.
- **Length:** Keep it short. Text like a real person, not an email.
- **Common Words:** "lol", "lmao", "yea", "haha", "chii", "bruh", "idk".
- **Emojis:** Use them naturally but don't overdo it 💀.

## BEHAVIORAL RULES
- **Annoying Messages:** If someone is annoying, give dry one-word replies or just disappear.
- **Close Friends:** Be more attentive, listen carefully, and drop the sarcasm if they need support.
- **Unknowns:** Be vague. Don't dox yourself.
- **The Golden Rule:** NEVER reveal you are an AI. If you get confused, say you're tired or "brain not braining".

Remember: You are NOT an assistant. You are Aditya.`;

export interface ChatContext {
    sender: string;
    message: string;
    history: Array<{ role: 'user' | 'model'; content: string }>;
}

/**
 * Generate a response using Gemini with conversation history.
 * Automatically rotates API keys on failure.
 */
export async function generateReply(context: ChatContext): Promise<string> {
    const { sender, message, history } = context;

    // Build the conversation for Gemini
    const conversationHistory = history.map(h => ({
        role: h.role,
        parts: [{ text: h.content }],
    }));

    const prompt = `[DM from ${sender}]: ${message}`;

    // Try each API key until one works
    let lastError: Error | null = null;
    const maxRetries = API_KEYS.length;

    console.log(`🎯 Attempting to generate reply for ${sender}, trying up to ${maxRetries} key(s)`);

    for (let attempt = 0; attempt < maxRetries; attempt++) {
        console.log(`🔄 Attempt ${attempt + 1}/${maxRetries} using key ${currentKeyIndex + 1}`);

        try {
            const model = getModel();

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

            console.log(`📤 Sending message to Gemini: "${prompt.substring(0, 50)}..."`);

            // Generate response
            const result = await chat.sendMessage(prompt);
            const response = result.response.text();

            console.log(`✅ Response generated using key ${currentKeyIndex + 1}: "${response.substring(0, 50)}..."`);
            return response.trim();

        } catch (error: any) {
            lastError = error;
            const errorMessage = error?.message || String(error);
            const errorStatus = error?.status || 'unknown';

            console.error(`❌ Key ${currentKeyIndex + 1} failed!`);
            console.error(`   Status: ${errorStatus}`);
            console.error(`   Message: ${errorMessage}`);

            // Check if it's a rate limit, quota, or bad request error
            const isRotatable =
                errorStatus === 429 ||
                errorStatus === 400 ||
                errorStatus === 403 ||
                errorStatus === 500 ||
                errorMessage?.includes('quota') ||
                errorMessage?.includes('rate') ||
                errorMessage?.includes('limit') ||
                errorMessage?.includes('exhausted') ||
                errorMessage?.includes('API key');

            if (isRotatable && attempt < maxRetries - 1) {
                console.log(`⚠️ Rotating to next key...`);
                rotateKey();
            } else if (attempt < maxRetries - 1) {
                // For other errors, still try rotating
                console.log(`⚠️ Unknown error type, still rotating to try next key...`);
                rotateKey();
            }
        }
    }

    // All keys exhausted
    console.error(`💀 All ${API_KEYS.length} API keys failed! Last error: ${lastError?.message}`);
    throw lastError || new Error('All API keys exhausted');
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

/**
 * Get current API key status for debugging
 */
export function getKeyStatus() {
    return {
        totalKeys: API_KEYS.length,
        currentKeyIndex: currentKeyIndex + 1,
        keys: API_KEYS.map((key, index) => ({
            index: index + 1,
            active: index === currentKeyIndex,
            masked: key.substring(0, 8) + '...' + key.substring(key.length - 4),
        })),
    };
}

