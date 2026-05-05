require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');

const app = express();

// Middlewares
app.use(cors());
app.use(express.json());

// Configuração do Banco de Dados (Supabase)
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
});

// 1. ROTA DE CONSULTA (O que faz os exercícios aparecerem no celular)
app.get('/api/exercicios', (req, res) => {
    res.json([
        {
            "id": 1,
            "nome": "Alongamento Cervical",
            "repeticoes": "3 séries de 15s",
            "descricao": "Puxe levemente a cabeça para a direita e depois para a esquerda."
        },
        {
            "id": 2,
            "nome": "Prancha Isométrica",
            "repeticoes": "3 séries de 30s",
            "descricao": "Mantenha o abdômen contraído e a coluna reta."
        },
        {
            "id": 3,
            "nome": "Rotação de Tronco",
            "repeticoes": "10 repetições cada lado",
            "descricao": "Gire o tronco suavemente mantendo o quadril fixo."
        }
    ]);
});

// 2. ROTA DE CHECK-IN (O que envia a dor para o seu Web Admin/Supabase)
app.post('/api/exercicios/checkin', async (req, res) => {
    const { paciente_id, paciente_nome, exercicio_nome, dor } = req.body;

    try {
        const query = `
            INSERT INTO checkins (paciente_id, paciente_nome, exercicio_nome, dor)
            VALUES ($1, $2, $3, $4)
            RETURNING *;
        `;
        const values = [paciente_id, paciente_nome, exercicio_nome, dor];
        const result = await pool.query(query, values);

        console.log(`[CHECKIN] Registro salvo para: ${paciente_nome}`);
        res.status(201).json({ message: "Check-in salvo com sucesso!", data: result.rows[0] });
    } catch (err) {
        console.error('[ERRO BANCO]', err);
        res.status(500).json({ error: "Erro ao salvar no banco de dados" });
    }
});

// Rotas de Autenticação e Agenda (seus arquivos separados)
app.use('/api/auth', require('./routes/authRoutes'));
app.use('/api/paciente', require('./routes/pacienteRoutes'));
app.use('/api/agendamentos', require('./routes/agendaRoutes'));

app.get('/', (req, res) => {
    res.json({ message: "Servidor RPG Clinic ONLINE! " });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`[SERVER] Rodando na porta ${PORT}`);
});