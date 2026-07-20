const express = require('express');
const { body } = require('express-validator');
const { syncBank, getSupportedBanks } = require('../controllers/bankController');
const { protect } = require('../middleware/auth');
const { validateFields } = require('../middleware/validation');

const router = express.Router();

// Shield all endpoints
router.use(protect);

// GET /api/bank/supported
router.get('/supported', getSupportedBanks);

// POST /api/bank/sync
router.post(
  '/sync',
  [
    body('bankId', 'Bank ID must be a non-empty string').notEmpty().isString().trim(),
    validateFields
  ],
  syncBank
);

module.exports = router;
