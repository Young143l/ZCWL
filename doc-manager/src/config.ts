import { config } from "dotenv";

config();

const DATABASE_USER = process.env.DATABASE_USER;
const DATABASE_HOST = process.env.DATABASE_HOST;
const DATABASE_PASSWORD = process.env.DATABASE_PASSWORD;
const DATABASE_PORT = process.env.DATABASE_PORT;
const DATABASE_NAME = process.env.DATABASE_NAME;

export const PGSQL_CONFIG = {
    user: DATABASE_USER,
    host: DATABASE_HOST,
    database: DATABASE_NAME,
    password: DATABASE_PASSWORD,
    port: parseInt(DATABASE_PORT as string),
    max: 10,
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 10000,
};
