const express = require('express');
const { body, param, query } = require('express-validator');
const {
  getBudgets,
  createOrUpdateBudget,
  getBudgetByCategory,
  deleteBudget,
  getBudgetProgress
} = require('../controllers/budgetController');
const { protect } = require('../middleware/auth');
const { validateFields } = require('../middleware/validation');

const router = express.Router();

// Protect all routes
router.use(protect);

// GET /api/budgets/progress
router.get(
  '/progress',
  [
    query('month').optional().isInt({ min: 1, max: 12 }).toInt(),
    query('year').optional().isInt().toInt(),
    validateFields
  ],
  getBudgetProgress
);

// GET /api/budgets & POST /api/budgets
router
  .route('/')
  .get(
    [
      query('month').optional().isInt({ min: 1, max: 12 }).toInt(),
      query('year').optional().isInt().toInt(),
      validateFields
    ],
    getBudgets
  )
  .post(
    [
      body('category', 'Valid category is required').isIn([
        'Food', 'Transport', 'Shopping', 'Bills', 'Entertainment',
        'Healthcare', 'Education', 'Rent', 'Utilities', 'Others'
      ]),
      body('amount', 'Amount is required and must be positive').isFloat({ gt: 0 }),
      body('month').optional().isInt({ min: 1, max: 12 }).toInt(),
      body('year').optional().isInt().toInt(),
      validateFields
    ],
    createOrUpdateBudget
  );

// GET, DELETE /api/budgets/:category
router
  .route('/:category')
  .get(
    [
      param('category', 'Valid category is required').isIn([
        'Food', 'Transport', 'Shopping', 'Bills', 'Entertainment',
        'Healthcare', 'Education', 'Rent', 'Utilities', 'Others'
      ]),
      query('month').optional().isInt({ min: 1, max: 12 }).toInt(),
      query('year').optional().isInt().toInt(),
      validateFields
    ],
    getBudgetByCategory
  )
  .delete(
    [
      param('category', 'Valid category is required').isIn([
        'Food', 'Transport', 'Shopping', 'Bills', 'Entertainment',
        'Healthcare', 'Education', 'Rent', 'Utilities', 'Others'
      ]),
      query('month').optional().isInt({ min: 1, max: 12 }).toInt(),
      query('year').optional().isInt().toInt(),
      validateFields
    ],
    deleteBudget
  );

module.exports = router;
