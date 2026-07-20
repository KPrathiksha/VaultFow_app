const express = require('express');
const { body, param, query } = require('express-validator');
const {
  getTransactions,
  createTransaction,
  getTransaction,
  updateTransaction,
  deleteTransaction,
  getTransactionSummary,
  getTransactionsByCategory
} = require('../controllers/transactionController');
const { protect } = require('../middleware/auth');
const { validateFields } = require('../middleware/validation');

const router = express.Router();

// Apply auth protection to all routes
router.use(protect);

// GET /api/transactions & POST /api/transactions
router
  .route('/')
  .get(
    [
      query('page').optional().isInt({ min: 1 }).toInt(),
      query('limit').optional().isInt({ min: 1 }).toInt(),
      query('type').optional().isIn(['income', 'expense']),
      query('category').optional().isString(),
      query('startDate').optional().isISO8601(),
      query('endDate').optional().isISO8601(),
      validateFields
    ],
    getTransactions
  )
  .post(
    [
      body('amount', 'Amount is required and must be a number').isFloat({ gt: 0 }),
      body('category', 'Valid category is required').isIn([
        'Food', 'Transport', 'Shopping', 'Bills', 'Entertainment',
        'Healthcare', 'Education', 'Rent', 'Utilities', 'Salary',
        'Investment', 'Others'
      ]),
      body('type', 'Type must be income or expense').isIn(['income', 'expense']),
      body('date').optional().isISO8601(),
      body('note').optional().isString().trim(),
      validateFields
    ],
    createTransaction
  );

// GET /api/transactions/summary
router.get(
  '/summary',
  [
    query('startDate').optional().isISO8601(),
    query('endDate').optional().isISO8601(),
    validateFields
  ],
  getTransactionSummary
);

// GET /api/transactions/categories/:category
router.get(
  '/categories/:category',
  [
    param('category', 'Valid category is required').isIn([
      'Food', 'Transport', 'Shopping', 'Bills', 'Entertainment',
      'Healthcare', 'Education', 'Rent', 'Utilities', 'Salary',
      'Investment', 'Others'
    ]),
    query('page').optional().isInt({ min: 1 }).toInt(),
    query('limit').optional().isInt({ min: 1 }).toInt(),
    validateFields
  ],
  getTransactionsByCategory
);

// GET, PUT, DELETE /api/transactions/:id
router
  .route('/:id')
  .get(getTransaction)
  .put(
    [
      body('amount').optional().isFloat({ gt: 0 }),
      body('category').optional().isIn([
        'Food', 'Transport', 'Shopping', 'Bills', 'Entertainment',
        'Healthcare', 'Education', 'Rent', 'Utilities', 'Salary',
        'Investment', 'Others'
      ]),
      body('type').optional().isIn(['income', 'expense']),
      body('date').optional().isISO8601(),
      body('note').optional().isString().trim(),
      validateFields
    ],
    updateTransaction
  )
  .delete(deleteTransaction);

module.exports = router;
