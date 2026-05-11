const db = require('../db');

exports.login = async (req, res) => {
    const { email, senha } = req.body;

    try {
        if (!email || !senha) return res.status(401).json({ erro: "E-mail ou senha inválidos." });

        const supabaseUrl = 'https://gzxqsxjluahixavoklhd.supabase.co';
        const anonKey = 'sb_publishable_gblVzVE_U0seUjRRB9EK7g_gpb9hz5A';
        
        process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

        // ALTERAÇÃO: Buscamos apenas pelo e-mail na API do Supabase
        const apiEndpoint = `${supabaseUrl}/rest/v1/pacientes?email=eq.${encodeURIComponent(email)}&select=*`;
        
        const response = await fetch(apiEndpoint, {
            method: 'GET',
            headers: {
                'apikey': anonKey,
                'Authorization': `Bearer ${anonKey}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        
        const data = await response.json();
        
        if (data && data.length > 0) {
            const user = data[0];

            // Retornamos sucesso = true e enviamos a senha (hash) para o Android conferir
            return res.status(200).json({
                sucesso: true,
                mensagem: "Usuário localizado na Nuvem!",
                usuario: {
                    id: user.id,
                    nome: user.nome || "Paciente",
                    email: user.email,
                    senha: user.senha // <--- IMPORTANTE: Enviando o Hash para o BCrypt no Android
                },
                token: "JWT_SECURE_TOKEN_FALSO_123"
            });
        }

        // Se o array 'data' vier vazio, significa que o e-mail não existe
        return res.status(401).json({ sucesso: false, erro: "Usuário não encontrado." });

    } catch (err) {
        console.error("ERRO SUPABASE: ", err);
        return res.status(500).json({ sucesso: false, erro: "Erro interno no servidor." });
    }
};

exports.register = async (req, res) => {
    // Sua lógica de registro atual...
    const { nome, email, senha } = req.body;
    try {
        if (nome && email && senha) {
            return res.status(201).json({
                sucesso: true,
                mensagem: "Cadastro realizado com sucesso!",
                usuario: { id: 2, nome: nome, email: email },
                token: "JWT_SECURE_TOKEN_FALSO_456"
            });
        }
        return res.status(400).json({ erro: "Dados incompletos para cadastro." });
    } catch (err) {
        console.error(err);
        return res.status(500).json({ erro: "Erro interno no servidor." });
    }
};