const { Pool } = require('pg');

// Nós usamos as variáveis do arquivo .env
const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    // Para bancos na web (Supabase, Neon, etc.) é necessário o 'ssl' ativo
    ssl: { rejectUnauthorized: false }
});

module.exports = {
    query: (text, params) => pool.query(text, params),
};
