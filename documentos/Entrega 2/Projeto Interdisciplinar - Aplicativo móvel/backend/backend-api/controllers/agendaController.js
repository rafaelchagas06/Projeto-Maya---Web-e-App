const db = require('../db');

exports.marcarSessao = async (req, res) => {
    const { id_paciente, data, horario } = req.body;

    try {
        // Validando se os dados básicos vieram do App (JSON POST do Android)
        if (!id_paciente || !data || !horario) {
            return res.status(400).json({ erro: "Preencha a data e o horário para agendar a sessão." });
        }

        // [SIMULAÇÃO DE INSERÇÃO DB]
        // insert into agendamentos (id, data, horario) values ($1, $2, $3)...

        return res.status(201).json({
            sucesso: true,
            mensagem: "Agendamento da sessão de RPG confirmado com sucesso!",
            detalhes: {
                data: data,
                horario: horario,
                id_paciente: id_paciente
            }
        });

    } catch (err) {
        console.error(err);
        return res.status(500).json({ erro: "Erro ao confirmar agendamento." });
    }
};
