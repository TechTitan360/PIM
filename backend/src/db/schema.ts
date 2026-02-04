import { pgTable, serial, text, timestamp, boolean } from 'drizzle-orm/pg-core';

/**
 * Messages table - stores all message history for context.
 */
export const messages = pgTable('messages', {
    id: serial('id').primaryKey(),
    contactName: text('contact_name').notNull(),        // e.g., "radha_01"
    messageContent: text('message_content').notNull(),  // The actual message
    isFromUser: boolean('is_from_user').default(false), // true = You replied, false = They sent
    platform: text('platform').default('instagram'),
    createdAt: timestamp('created_at').defaultNow(),
});

/**
 * VIP Contacts - special handling for certain people.
 * Future feature: Different prompts for different people.
 */
export const vipContacts = pgTable('vip_contacts', {
    id: serial('id').primaryKey(),
    username: text('username').notNull().unique(),
    nickname: text('nickname'),                 // How Aditya refers to them
    customPrompt: text('custom_prompt'),        // Override system prompt for this person
    isEnabled: boolean('is_enabled').notNull().default(true),
    createdAt: timestamp('created_at').defaultNow().notNull(),
});

// Type exports for use in the app
export type Message = typeof messages.$inferSelect;
export type NewMessage = typeof messages.$inferInsert;
export type VipContact = typeof vipContacts.$inferSelect;
export type NewVipContact = typeof vipContacts.$inferInsert;
