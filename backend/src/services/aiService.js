const Budget = require('../models/Budget');
const Transaction = require('../models/Transaction');
const Insight = require('../models/Insight');

// Predefined high-quality financial savings tips
const SAVINGS_TIPS = [
  "Track your daily subscriptions. Canceling just one unused ₹299 monthly service saves ₹3,588 a year!",
  "Try the 50/30/20 rule: allocate 50% of your income to needs, 30% to wants, and 20% to savings.",
  "Consider a '24-hour cooling-off period' before major non-essential purchases to curb impulsive shopping.",
  "Automate your savings! Setup an auto-transfer to your savings or investment account right on payday.",
  "Cooking at home instead of ordering out can easily save you up to ₹4,000 every single month.",
  "Build an emergency fund covering 3 to 6 months of basic living expenses for financial peace of mind.",
  "Review your utility bills. Turning down water heaters or switching off idle devices cuts monthly costs.",
  "Invest in assets that appreciate or generate passive income, rather than liabilities that depreciate.",
  "Before buying a brand-new item, check online marketplaces for high-quality secondhand options.",
  "Pay off high-interest debts first (the avalanche method) to minimize cumulative interest payments."
];

// Helper to get start and end dates of a month
const getMonthRange = (month, year) => {
  const startDate = new Date(year, month - 1, 1);
  const endDate = new Date(year, month, 0, 23, 59, 59, 999);
  return { startDate, endDate };
};

/**
 * 1. Check Budget Threshold Warnings (80% or 100%+ spent)
 * Triggered on expense transaction creations
 */
const checkBudgetThresholds = async (userId, category, month, year) => {
  try {
    const budget = await Budget.findOne({ userId, category, month, year });
    if (!budget || budget.amount === 0) return;

    const ratio = budget.spent / budget.amount;

    if (ratio >= 0.8) {
      let message = '';
      let type = 'budget_warning';

      if (ratio >= 1.0) {
        message = `You have exceeded 100% of your ${category} budget! Spent ₹${budget.spent.toLocaleString()} of ₹${budget.amount.toLocaleString()}.`;
      } else {
        message = `You've used ${Math.floor(ratio * 100)}% of your ${category} budget (₹${budget.spent.toLocaleString()} of ₹${budget.amount.toLocaleString()}).`;
      }

      // Check if this exact alert was already generated in the last 24 hours to prevent spam
      const oneDayAgo = new Date(Date.now() - 24 * 60 * 60 * 1000);
      const duplicateAlert = await Insight.findOne({
        userId,
        type,
        message,
        createdAt: { $gte: oneDayAgo }
      });

      if (!duplicateAlert) {
        await Insight.create({
          userId,
          type,
          message
        });
        console.log(`AI Insight: Generated budget warning for user ${userId} on ${category}`);
      }
    }
  } catch (err) {
    console.error(`Error checking budget thresholds: ${err.message}`);
  }
};

/**
 * 2. Compare Month-over-Month spending hikes (20%+)
 */
const checkMoMSpendingSpike = async (userId, category, currentMonth, currentYear) => {
  try {
    // Current month range
    const currentRange = getMonthRange(currentMonth, currentYear);
    
    // Previous month details
    let prevMonth = currentMonth - 1;
    let prevYear = currentYear;
    if (prevMonth === 0) {
      prevMonth = 12;
      prevYear = currentYear - 1;
    }
    const prevRange = getMonthRange(prevMonth, prevYear);

    // Sum current month category spending
    const currentSpentResult = await Transaction.aggregate([
      {
        $match: {
          userId,
          category,
          type: 'expense',
          date: { $gte: currentRange.startDate, $lte: currentRange.endDate }
        }
      },
      { $group: { _id: null, total: { $sum: '$amount' } } }
    ]);
    const currentSpent = currentSpentResult.length > 0 ? currentSpentResult[0].total : 0;

    // Sum previous month category spending
    const prevSpentResult = await Transaction.aggregate([
      {
        $match: {
          userId,
          category,
          type: 'expense',
          date: { $gte: prevRange.startDate, $lte: prevRange.endDate }
        }
      },
      { $group: { _id: null, total: { $sum: '$amount' } } }
    ]);
    const prevSpent = prevSpentResult.length > 0 ? prevSpentResult[0].total : 0;

    // Check spike if there was previous spending and it rose by 20%+
    // Only flag if the increase is substantial (e.g. at least ₹500) to keep it actionable
    if (prevSpent > 0 && currentSpent >= prevSpent * 1.2 && (currentSpent - prevSpent >= 500)) {
      const percentageIncrease = Math.round(((currentSpent - prevSpent) / prevSpent) * 100);
      const message = `You spent ${percentageIncrease}% more on ${category} this month (₹${currentSpent.toLocaleString()}) compared to last month (₹${prevSpent.toLocaleString()}).`;
      
      const duplicateAlert = await Insight.findOne({
        userId,
        type: 'spending_alert',
        message
      });

      if (!duplicateAlert) {
        await Insight.create({
          userId,
          type: 'spending_alert',
          message
        });
      }
    }
  } catch (err) {
    console.error(`Error in MoM spending check: ${err.message}`);
  }
};

/**
 * 3. Generate Monthly Savings Rate Summaries
 */
const generateMonthlySummary = async (userId, month, year) => {
  try {
    const { startDate, endDate } = getMonthRange(month, year);

    const transactions = await Transaction.find({
      userId,
      date: { $gte: startDate, $lte: endDate }
    });

    let income = 0;
    let expenses = 0;

    transactions.forEach(t => {
      if (t.type === 'income') income += t.amount;
      else if (t.type === 'expense') expenses += t.amount;
    });

    if (income > 0) {
      const savings = income - expenses;
      const savingsRate = Math.round((savings / income) * 100);
      
      let message = '';
      if (savings > 0) {
        message = `This month you saved ₹${savings.toLocaleString()}, which is ${savingsRate}% of your total income. Great job!`;
      } else {
        message = `This month you spent ₹${Math.abs(savings).toLocaleString()} more than your income. Review your budgets to cut back on expenses next month.`;
      }

      // Prevent exact duplicate notifications
      const duplicateSummary = await Insight.findOne({
        userId,
        type: 'monthly_summary',
        message
      });

      if (!duplicateSummary) {
        await Insight.create({
          userId,
          type: 'monthly_summary',
          message
        });
      }
    }
  } catch (err) {
    console.error(`Error generating monthly summary: ${err.message}`);
  }
};

/**
 * Core Orchestrator: Generate a fully refreshed batch of insights
 * 1. Checks budget caps
 * 2. Checks MoM hikes in all categories
 * 3. Generates monthly summary
 * 4. Inserts a fresh random savings tip
 */
const generateAllInsights = async (userId) => {
  try {
    const currentMonth = new Date().getMonth() + 1;
    const currentYear = new Date().getFullYear();

    // 1. Refresh MoM checks for major categories
    const categories = [
      'Food', 'Transport', 'Shopping', 'Bills', 'Entertainment',
      'Healthcare', 'Education', 'Rent', 'Utilities', 'Others'
    ];
    for (const cat of categories) {
      await checkMoMSpendingSpike(userId, cat, currentMonth, currentYear);
    }

    // 2. Refresh active budget limits
    const budgets = await Budget.find({ userId, month: currentMonth, year: currentYear });
    for (const b of budgets) {
      await checkBudgetThresholds(userId, b.category, currentMonth, currentYear);
    }

    // 3. Generate Monthly Summary
    await generateMonthlySummary(userId, currentMonth, currentYear);

    // 4. Sample a random savings tip and record
    const randomTip = SAVINGS_TIPS[Math.floor(Math.random() * SAVINGS_TIPS.length)];
    
    // Check if the user already has this tip in their database
    const hasTip = await Insight.findOne({
      userId,
      type: 'savings_tip',
      message: randomTip
    });

    if (!hasTip) {
      await Insight.create({
        userId,
        type: 'savings_tip',
        message: randomTip
      });
    }

    return true;
  } catch (error) {
    console.error(`Failed generating insights for user ${userId}:`, error.message);
    throw error;
  }
};

module.exports = {
  checkBudgetThresholds,
  checkMoMSpendingSpike,
  generateMonthlySummary,
  generateAllInsights,
  SAVINGS_TIPS
};
