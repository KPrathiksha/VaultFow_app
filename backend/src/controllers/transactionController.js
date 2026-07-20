const Transaction = require('../models/Transaction');
const Budget = require('../models/Budget');
const { checkBudgetThresholds } = require('../services/aiService');

// @desc    Get all transactions for authenticated user with pagination
// @route   GET /api/transactions
// @access  Private
const getTransactions = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page, 10) || 1;
    const limit = parseInt(req.query.limit, 10) || 20;
    const startIndex = (page - 1) * limit;

    const query = { userId: req.user.userId };

    // Optional filters
    if (req.query.type) {
      query.type = req.query.type;
    }
    if (req.query.category) {
      query.category = req.query.category;
    }
    if (req.query.startDate && req.query.endDate) {
      query.date = {
        $gte: new Date(req.query.startDate),
        $lte: new Date(req.query.endDate)
      };
    }

    const total = await Transaction.countDocuments(query);
    const transactions = await Transaction.find(query)
      .sort({ date: -1 })
      .skip(startIndex)
      .limit(limit);

    res.status(200).json({
      success: true,
      count: transactions.length,
      pagination: {
        page,
        limit,
        totalPages: Math.ceil(total / limit),
        total
      },
      data: transactions
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Create new transaction
// @route   POST /api/transactions
// @access  Private
const createTransaction = async (req, res, next) => {
  try {
    const { amount, category, type, date, note } = req.body;

    const transaction = await Transaction.create({
      userId: req.user.userId,
      amount,
      category,
      type,
      date: date ? new Date(date) : new Date(),
      note
    });

    // If transaction is an expense, update corresponding budget spent
    if (type === 'expense') {
      const txDate = transaction.date;
      const month = txDate.getMonth() + 1; // 0-11 to 1-12
      const year = txDate.getFullYear();

      const budget = await Budget.findOne({
        userId: req.user.userId,
        category,
        month,
        year
      });

      if (budget) {
        budget.spent += amount;
        await budget.save();

        // Trigger AI Insight checking asynchronously
        checkBudgetThresholds(req.user.userId, category, month, year).catch(err => 
          console.error(`AI Budget Alert Error: ${err.message}`)
        );
      }
    }

    res.status(201).json({
      success: true,
      data: transaction
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Get specific transaction
// @route   GET /api/transactions/:id
// @access  Private
const getTransaction = async (req, res, next) => {
  try {
    const transaction = await Transaction.findOne({
      transactionId: req.params.id,
      userId: req.user.userId
    });

    if (!transaction) {
      return res.status(404).json({
        success: false,
        error: `Transaction not found with id ${req.params.id}`
      });
    }

    res.status(200).json({
      success: true,
      data: transaction
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Update transaction
// @route   PUT /api/transactions/:id
// @access  Private
const updateTransaction = async (req, res, next) => {
  try {
    let transaction = await Transaction.findOne({
      transactionId: req.params.id,
      userId: req.user.userId
    });

    if (!transaction) {
      return res.status(404).json({
        success: false,
        error: `Transaction not found with id ${req.params.id}`
      });
    }

    const oldAmount = transaction.amount;
    const oldType = transaction.type;
    const oldCategory = transaction.category;
    const oldDate = transaction.date;

    // Update transaction
    transaction = await Transaction.findOneAndUpdate(
      { transactionId: req.params.id, userId: req.user.userId },
      req.body,
      { new: true, runValidators: true }
    );

    // Sync budget spent
    // 1. Revert old transaction spent if it was an expense
    if (oldType === 'expense') {
      const oldMonth = oldDate.getMonth() + 1;
      const oldYear = oldDate.getFullYear();
      const oldBudget = await Budget.findOne({
        userId: req.user.userId,
        category: oldCategory,
        month: oldMonth,
        year: oldYear
      });
      if (oldBudget) {
        oldBudget.spent = Math.max(0, oldBudget.spent - oldAmount);
        await oldBudget.save();
      }
    }

    // 2. Add new transaction spent if it is an expense
    if (transaction.type === 'expense') {
      const newMonth = transaction.date.getMonth() + 1;
      const newYear = transaction.date.getFullYear();
      const newBudget = await Budget.findOne({
        userId: req.user.userId,
        category: transaction.category,
        month: newMonth,
        year: newYear
      });
      if (newBudget) {
        newBudget.spent += transaction.amount;
        await newBudget.save();

        // Trigger AI Insight checking
        checkBudgetThresholds(req.user.userId, transaction.category, newMonth, newYear).catch(err =>
          console.error(`AI Budget Alert Error: ${err.message}`)
        );
      }
    }

    res.status(200).json({
      success: true,
      data: transaction
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Delete transaction
// @route   DELETE /api/transactions/:id
// @access  Private
const deleteTransaction = async (req, res, next) => {
  try {
    const transaction = await Transaction.findOne({
      transactionId: req.params.id,
      userId: req.user.userId
    });

    if (!transaction) {
      return res.status(404).json({
        success: false,
        error: `Transaction not found with id ${req.params.id}`
      });
    }

    await Transaction.deleteOne({ transactionId: req.params.id, userId: req.user.userId });

    // Sync budget spent
    if (transaction.type === 'expense') {
      const txDate = transaction.date;
      const month = txDate.getMonth() + 1;
      const year = txDate.getFullYear();

      const budget = await Budget.findOne({
        userId: req.user.userId,
        category: transaction.category,
        month,
        year
      });

      if (budget) {
        budget.spent = Math.max(0, budget.spent - transaction.amount);
        await budget.save();
      }
    }

    res.status(200).json({
      success: true,
      data: {}
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Get transaction summary
// @route   GET /api/transactions/summary
// @access  Private
const getTransactionSummary = async (req, res, next) => {
  try {
    const { startDate, endDate } = req.query;

    const query = { userId: req.user.userId };

    if (startDate && endDate) {
      query.date = {
        $gte: new Date(startDate),
        $lte: new Date(endDate)
      };
    }

    const transactions = await Transaction.find(query);

    let totalIncome = 0;
    let totalExpense = 0;

    transactions.forEach(t => {
      if (t.type === 'income') {
        totalIncome += t.amount;
      } else if (t.type === 'expense') {
        totalExpense += t.amount;
      }
    });

    res.status(200).json({
      success: true,
      data: {
        totalIncome,
        totalExpense,
        balance: totalIncome - totalExpense,
        currency: req.user.currency || '₹'
      }
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Get transactions by category
// @route   GET /api/transactions/categories/:category
// @access  Private
const getTransactionsByCategory = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page, 10) || 1;
    const limit = parseInt(req.query.limit, 10) || 20;
    const startIndex = (page - 1) * limit;

    const query = {
      userId: req.user.userId,
      category: req.params.category
    };

    const total = await Transaction.countDocuments(query);
    const transactions = await Transaction.find(query)
      .sort({ date: -1 })
      .skip(startIndex)
      .limit(limit);

    res.status(200).json({
      success: true,
      count: transactions.length,
      pagination: {
        page,
        limit,
        totalPages: Math.ceil(total / limit),
        total
      },
      data: transactions
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getTransactions,
  createTransaction,
  getTransaction,
  updateTransaction,
  deleteTransaction,
  getTransactionSummary,
  getTransactionsByCategory
};
