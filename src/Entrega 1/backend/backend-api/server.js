require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();

// Middlewares Globais
app.use(cors()); // Permite requisições de outras origens (Ex: Android Emuladores)
app.use(express.json()); // Lê todo o tráfego de chegada em formato JSON

// Logger visual para a avaliação do professor (Mostra tudo que o app pediu)
app.use((req, res, next) => {
    console.log(`[INFO] O App está conversando com a API: ${req.method} ${req.url}`);
    next();
});

// Rotas Básicas
app.use('/api/auth', require('./routes/authRoutes'));
app.use('/api/paciente', require('./routes/pacienteRoutes'));
app.use('/api/agendamentos', require('./routes/agendaRoutes'));

// Rota provisória de Exercícios para o App consumir
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

// Rota de Teste para o navegador
app.get('/', (req, res) => {
    res.json({ message: "O servidor da Clínica RPG está ONLINE! 🌟" });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`[SERVER] Rodando na porta ${PORT}`);
});
