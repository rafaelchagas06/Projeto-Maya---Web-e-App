const db = require('../db');

exports.getHomeData = async (req, res) => {
    const pacienteId = req.params.id;

    try {
        // [SIMULAÇÃO PARA MOCKUP FRONTEND]
        // Substituindo consulta real no DB por dados perfeitamente adequados ao Figma que desenhamos.
        // Simulaticamente: const status = await db.query('SELECT * FROM tratamentos WHERE pac_id=$1', [pacienteId]);

        return res.status(200).json({
            sucesso: true,
            dados: {
                id_paciente: pacienteId,
                nome_paciente: "Rafael C.",
                status_dor_hoje: null, // Deixando "null" para o app forçar o usuário a preencher os circulos de 1 a 10
                pontuacao_postura: 88,
                meta_atual: "Corrigir Ombros Curvados (Semana 3/6)",
                prox_alongamento_hora: "09:30"
            }
        });

    } catch (err) {
        console.error(err);
        return res.status(500).json({ erro: "Erro ao buscar dados do paciente na Home." });
    }
};
