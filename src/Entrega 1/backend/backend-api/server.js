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

// Buscar evolução/prontuário do paciente para a tela de progresso
app.get('/api/prontuarios/:paciente_id', async (req, res) => {
    const { paciente_id } = req.params;
    try {
        const result = await pool.query(
            'SELECT * FROM prontuarios WHERE paciente_id = $1 ORDER BY data DESC',
            [paciente_id]
        );
        res.json(result.rows);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Erro ao buscar prontuário' });
    }
});

// NOVA ROTA DE EXERCÍCIOS: Busca apenas os exercícios prescritos para o paciente logado
// No Android, você deve chamar: /api/exercicios/8 (substituindo o 8 pelo ID do paciente)
app.get('/api/exercicios/:paciente_id', async (req, res) => {
    const { paciente_id } = req.params;
    try {
        const query = `
            SELECT e.* FROM exercicios e
            INNER JOIN prescricoes p ON e.id = p.exercicio_id
            WHERE p.paciente_id = $1
        `;
        const result = await pool.query(query, [paciente_id]);
        
        // Retorna a lista real do banco. Se não houver prescrição, retorna []
        res.json(result.rows);
    } catch (err) {
        console.error('Erro ao buscar exercícios prescritos:', err);
        res.status(500).json({ error: 'Erro ao buscar exercícios no banco' });
    }
});

// ROTA DE CHECK-IN
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

// Rotas de Autenticação e Agenda
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