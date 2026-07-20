const Transaction = require('../models/Transaction');

// @desc    Get monthly spending breakdown (income vs expense) for a specific year
// @route   GET /api/analytics/monthly
// @access  Private
const getMonthlyBreakdown = async (req, res, next) => {
  try {
    const year = parseInt(req.query.year, 10) || new Date().getFullYear();
    const startDate = new Date(year, 0, 1);
    const endDate = new Date(year, 11, 31, 23, 59, 59, 999);

    const match = {
      userId: req.user.userId,
      date: { $gte: startDate, $lte: endDate }
    };

    const breakdown = await Transaction.aggregate([
      { $match: match },
      {
        $group: {
          _id: {
            month: { $month: '$date' },
            type: '$type'
          },
          total: { $sum: '$amount' }
        }
      }
    ]);

    // Format breakdown into clean 12-month array
    const monthlyData = Array.from({ length: 12 }, (_, i) => ({
      month: i + 1,
      monthName: new Date(year, i, 1).toLocaleString('default', { month: 'long' }),
      income: 0,
      expense: 0,
      savings: 0
    }));

    breakdown.forEach(item => {
      const idx = item._id.month - 1;
      if (item._id.type === 'income') {
        monthlyData[idx].income = item.total;
      } else if (item._id.type === 'expense') {
        monthlyData[idx].expense = item.total;
      }
    });

    // Calculate savings
    monthlyData.forEach(data => {
      data.savings = data.income - data.expense;
    });

    res.status(200).json({
      success: true,
      year,
      data: monthlyData
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Get spending breakdown by category with percentages
// @route   GET /api/analytics/categories
// @access  Private
const getCategoryPercentages = async (req, res, next) => {
  try {
    const month = parseInt(req.query.month, 10) || new Date().getMonth() + 1;
    const year = parseInt(req.query.year, 10) || new Date().getFullYear();

    const startDate = new Date(year, month - 1, 1);
    const endDate = new Date(year, month, 0, 23, 59, 59, 999);

    const match = {
      userId: req.user.userId,
      type: 'expense',
      date: { $gte: startDate, $lte: endDate }
    };

    const categoryBreakdown = await Transaction.aggregate([
      { $match: match },
      {
        $group: {
          _id: '$category',
          amount: { $sum: '$amount' }
        }
      },
      { $sort: { amount: -1 } }
    ]);

    // Calculate total expense for percentage math
    const totalSpent = categoryBreakdown.reduce((sum, item) => sum + item.amount, 0);

    const data = categoryBreakdown.map(item => {
      const percentage = totalSpent > 0 ? (item.amount / totalSpent) * 100 : 0;
      return {
        category: item._id,
        amount: item.amount,
        percentage: parseFloat(percentage.toFixed(2))
      };
    });

    res.status(200).json({
      success: true,
      month,
      year,
      totalSpent,
      data
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Get spending trends over time (MoM spending graphs)
// @route   GET /api/analytics/trends
// @access  Private
const getSpendingTrends = async (req, res, next) => {
  try {
    const limitMonths = parseInt(req.query.limitMonths, 10) || 6; // default last 6 months

    // Aggregate monthly expense totals
    const trends = await Transaction.aggregate([
      {
        $match: {
          userId: req.user.userId,
          type: 'expense'
        }
      },
      {
        $group: {
          _id: {
            year: { $year: '$date' },
            month: { $month: '$date' }
          },
          totalSpent: { $sum: '$amount' }
        }
      },
      {
        $sort: {
          '_id.year': -1,
          '_id.month': -1
        }
      },
      { $limit: limitMonths }
    ]);

    // Reverse to chronological order (past to present)
    trends.reverse();

    // Map trends and calculate MoM percentage changes
    const formattedTrends = trends.map((item, idx) => {
      let changePercentage = 0;
      if (idx > 0) {
        const prevTotal = trends[idx - 1].totalSpent;
        changePercentage = prevTotal > 0 ? ((item.totalSpent - prevTotal) / prevTotal) * 100 : 0;
      }

      return {
        month: item._id.month,
        year: item._id.year,
        monthName: new Date(item._id.year, item._id.month - 1, 1).toLocaleString('default', { month: 'short' }),
        totalSpent: item.totalSpent,
        changePercentage: parseFloat(changePercentage.toFixed(2))
      };
    });

    res.status(200).json({
      success: true,
      data: formattedTrends
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Calculate savings rate over time (savings as % of income)
// @route   GET /api/analytics/savings-rate
// @access  Private
const getSavingsRate = async (req, res, next) => {
  try {
    const limitMonths = parseInt(req.query.limitMonths, 10) || 6;

    // Group transactions by month/year and type
    const aggregates = await Transaction.aggregate([
      { $match: { userId: req.user.userId } },
      {
        $group: {
          _id: {
            year: { $year: '$date' },
            month: { $month: '$date' },
            type: '$type'
          },
          total: { $sum: '$amount' }
        }
      },
      {
        $sort: {
          '_id.year': -1,
          '_id.month': -1
        }
      }
    ]);

    // Group by month node
    const monthlyGroups = {};

    aggregates.forEach(item => {
      const key = `${item._id.year}-${item._id.month}`;
      if (!monthlyGroups[key]) {
        monthlyGroups[key] = {
          year: item._id.year,
          month: item._id.month,
          monthName: new Date(item._id.year, item._id.month - 1, 1).toLocaleString('default', { month: 'short' }),
          income: 0,
          expense: 0
        };
      }

      if (item._id.type === 'income') {
        monthlyGroups[key].income = item.total;
      } else if (item._id.type === 'expense') {
        monthlyGroups[key].expense = item.total;
      }
    });

    // Format into sorted array
    const sortedRates = Object.values(monthlyGroups)
      .sort((a, b) => b.year - a.year || b.month - a.month)
      .slice(0, limitMonths);

    sortedRates.reverse(); // past to present

    const data = sortedRates.map(item => {
      const netSavings = item.income - item.expense;
      const savingsRate = item.income > 0 ? (netSavings / item.income) * 100 : 0;
      return {
        month: item.month,
        year: item.year,
        monthName: item.monthName,
        income: item.income,
        expense: item.expense,
        savings: netSavings,
        savingsRate: parseFloat(savingsRate.toFixed(2))
      };
    });

    res.status(200).json({
      success: true,
      data
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getMonthlyBreakdown,
  getCategoryPercentages,
  getSpendingTrends,
  getSavingsRate
};
