const express = require('express');
const router = express.Router();
const pacienteController = require('../controllers/pacienteController');

// GET /api/paciente/:id/home
router.get('/:id/home', pacienteController.getHomeData);

module.exports = router;
