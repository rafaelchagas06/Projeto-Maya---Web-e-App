const express = require('express');
const router = express.Router();
const agendaController = require('../controllers/agendaController');

// POST /api/agendamentos/novo
router.post('/novo', agendaController.marcarSessao);

module.exports = router;
