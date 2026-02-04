import { drizzle } from 'drizzle-orm/postgres-js';
import postgres from 'postgres';
import * as schema from './schema';

const connectionString = process.env.DATABASE_URL;

// Create DB connection only if DATABASE_URL is properly configured
let db: ReturnType<typeof drizzle<typeof schema>> | null = null;

if (connectionString && !connectionString.includes('your_') && !connectionString.includes('password@host')) {
    const queryClient = postgres(connectionString);
    db = drizzle(queryClient, { schema });
    console.log('✅ Database connected');
} else {
    console.warn('⚠️ DATABASE_URL not configured - running without database');
}

export { db };
