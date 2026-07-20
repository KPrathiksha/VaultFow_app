const Budget = require('../models/Budget');
const Transaction = require('../models/Transaction');

// Helper to get start and end dates of a month
const getMonthRange = (month, year) => {
  const startDate = new Date(year, month - 1, 1);
  const endDate = new Date(year, month, 0, 23, 59, 59, 999);
  return { startDate, endDate };
};

// Helper to calculate total spent in a category/month/year
const calculateSpent = async (userId, category, month, year) => {
  const { startDate, endDate } = getMonthRange(month, year);
  
  const result = await Transaction.aggregate([
    {
      $match: {
        userId,
        category,
        type: 'expense',
        date: { $gte: startDate, $lte: endDate }
      }
    },
    {
      $group: {
        _id: null,
        total: { $sum: '$amount' }
      }
    }
  ]);

  return result.length > 0 ? result[0].total : 0;
};

// @desc    Get all budgets for user
// @route   GET /api/budgets
// @access  Private
const getBudgets = async (req, res, next) => {
  try {
    const month = parseInt(req.query.month, 10) || new Date().getMonth() + 1;
    const year = parseInt(req.query.year, 10) || new Date().getFullYear();

    const budgets = await Budget.find({
      userId: req.user.userId,
      month,
      year
    });

    res.status(200).json({
      success: true,
      count: budgets.length,
      data: budgets
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Create or update budget for category (upsert)
// @route   POST /api/budgets
// @access  Private
const createOrUpdateBudget = async (req, res, next) => {
  try {
    const { category, amount, month, year } = req.body;
    const currentMonth = month ? parseInt(month, 10) : new Date().getMonth() + 1;
    const currentYear = year ? parseInt(year, 10) : new Date().getFullYear();

    // Calculate current spent from transactions for this category/month/year
    const calculatedSpent = await calculateSpent(req.user.userId, category, currentMonth, currentYear);

    // Try finding existing budget to update, or create a new one
    let budget = await Budget.findOne({
      userId: req.user.userId,
      category,
      month: currentMonth,
      year: currentYear
    });

    if (budget) {
      budget.amount = amount;
      budget.spent = calculatedSpent;
      await budget.save();
    } else {
      budget = await Budget.create({
        userId: req.user.userId,
        category,
        amount,
        month: currentMonth,
        year: currentYear,
        spent: calculatedSpent
      });
    }

    res.status(200).json({
      success: true,
      data: budget
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Get budget for specific category
// @route   GET /api/budgets/:category
// @access  Private
const getBudgetByCategory = async (req, res, next) => {
  try {
    const month = parseInt(req.query.month, 10) || new Date().getMonth() + 1;
    const year = parseInt(req.query.year, 10) || new Date().getFullYear();

    const budget = await Budget.findOne({
      userId: req.user.userId,
      category: req.params.category,
      month,
      year
    });

    if (!budget) {
      return res.status(404).json({
        success: false,
        error: `No budget setup found for category ${req.params.category} in month ${month}/${year}`
      });
    }

    res.status(200).json({
      success: true,
      data: budget
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Delete budget
// @route   DELETE /api/budgets/:category
// @access  Private
const deleteBudget = async (req, res, next) => {
  try {
    const month = parseInt(req.query.month, 10) || new Date().getMonth() + 1;
    const year = parseInt(req.query.year, 10) || new Date().getFullYear();

    const result = await Budget.deleteOne({
      userId: req.user.userId,
      category: req.params.category,
      month,
      year
    });

    if (result.deletedCount === 0) {
      return res.status(404).json({
        success: false,
        error: `No budget setup found for category ${req.params.category} to delete`
      });
    }

    res.status(200).json({
      success: true,
      data: {}
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Get budget progress with spent amounts
// @route   GET /api/budgets/progress
// @access  Private
const getBudgetProgress = async (req, res, next) => {
  try {
    const month = parseInt(req.query.month, 10) || new Date().getMonth() + 1;
    const year = parseInt(req.query.year, 10) || new Date().getFullYear();

    const budgets = await Budget.find({
      userId: req.user.userId,
      month,
      year
    });

    const progress = budgets.map(b => {
      const percentage = b.amount > 0 ? (b.spent / b.amount) * 100 : 0;
      return {
        budgetId: b.budgetId,
        category: b.category,
        amount: b.amount,
        spent: b.spent,
        remaining: Math.max(0, b.amount - b.spent),
        percentage: parseFloat(percentage.toFixed(2)),
        isOverBudget: b.spent > b.amount
      };
    });

    res.status(200).json({
      success: true,
      data: progress
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getBudgets,
  createOrUpdateBudget,
  getBudgetByCategory,
  deleteBudget,
  getBudgetProgress
};
