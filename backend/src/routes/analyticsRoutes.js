const express = require('express');
const { query } = require('express-validator');
const {
  getMonthlyBreakdown,
  getCategoryPercentages,
  getSpendingTrends,
  getSavingsRate
} = require('../controllers/analyticsController');
const { protect } = require('../middleware/auth');
const { validateFields } = require('../middleware/validation');

const router = express.Router();

// Shield all routes
router.use(protect);

// GET /api/analytics/monthly
router.get(
  '/monthly',
  [
    query('year').optional().isInt().toInt(),
    validateFields
  ],
  getMonthlyBreakdown
);

// GET /api/analytics/categories
router.get(
  '/categories',
  [
    query('month').optional().isInt({ min: 1, max: 12 }).toInt(),
    query('year').optional().isInt().toInt(),
    validateFields
  ],
  getCategoryPercentages
);

// GET /api/analytics/trends
router.get(
  '/trends',
  [
    query('limitMonths').optional().isInt({ min: 1 }).toInt(),
    validateFields
  ],
  getSpendingTrends
);

// GET /api/analytics/savings-rate
router.get(
  '/savings-rate',
  [
    query('limitMonths').optional().isInt({ min: 1 }).toInt(),
    validateFields
  ],
  getSavingsRate
);

module.exports = router;
