require('dotenv').config({ path: '.env' });
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const User = require('../src/models/User');
const Transaction = require('../src/models/Transaction');
const Budget = require('../src/models/Budget');
const Insight = require('../src/models/Insight');

const MOCK_PASSWORD = 'password123';

const seedDatabase = async () => {
  try {
    const connStr = process.env.MONGO_URI || 'mongodb://localhost:27017/vaultflow';
    console.log(`Starting Database Seeding at: ${connStr}`);
    
    await mongoose.connect(connStr);
    console.log('Connected to MongoDB.');

    // Clear existing collections
    await User.deleteMany({});
    await Transaction.deleteMany({});
    await Budget.deleteMany({});
    await Insight.deleteMany({});
    console.log('Cleared existing database records.');

    // Hash password
    const salt = await bcrypt.genSalt(10);
    const passwordHash = await bcrypt.hash(MOCK_PASSWORD, salt);

    // 1. Create 5 Sample Users
    const usersData = [
      { name: 'Prathiksha Rao', email: 'prathiksha@vaultflow.com', passwordHash, currency: '₹', monthlyBudget: 50000 },
      { name: 'Rajesh Kumar', email: 'rajesh@vaultflow.com', passwordHash, currency: '₹', monthlyBudget: 40000 },
      { name: 'Aanya Sharma', email: 'aanya@vaultflow.com', passwordHash, currency: '$', monthlyBudget: 3000 },
      { name: 'Vikram Singh', email: 'vikram@vaultflow.com', passwordHash, currency: '₹', monthlyBudget: 75000 },
      { name: 'Sneha Patel', email: 'sneha@vaultflow.com', passwordHash, currency: '₹', monthlyBudget: 35000 }
    ];

    const users = await User.create(usersData);
    console.log(`Seeded ${users.length} users successfully. (Password: "${MOCK_PASSWORD}")`);

    // Let's seed for Prathiksha (users[0]) and Rajesh (users[1]) heavily
    const currentMonth = new Date().getMonth() + 1;
    const currentYear = new Date().getFullYear();

    const lastMonth = currentMonth === 1 ? 12 : currentMonth - 1;
    const lastMonthYear = currentMonth === 1 ? currentYear - 1 : currentYear;

    // 2. Seed Budgets for the users
    const budgetCategories = ['Food', 'Transport', 'Shopping', 'Bills', 'Entertainment', 'Rent', 'Utilities'];
    const budgetsData = [];

    users.forEach(user => {
      budgetCategories.forEach(category => {
        let amount = 5000;
        if (category === 'Rent') amount = 15000;
        if (category === 'Shopping') amount = 8000;
        if (category === 'Food') amount = 6000;

        // Current Month Budget
        budgetsData.push({
          userId: user.userId,
          category,
          amount,
          month: currentMonth,
          year: currentYear,
          spent: 0
        });

        // Last Month Budget
        budgetsData.push({
          userId: user.userId,
          category,
          amount,
          month: lastMonth,
          year: lastMonthYear,
          spent: 0
        });
      });
    });

    const seededBudgets = await Budget.create(budgetsData);
    console.log(`Seeded ${seededBudgets.length} budget limit records.`);

    // 3. Seed Transactions (20-30 records)
    const transactionsData = [];

    // Let's define some sample transaction skeletons
    const sampleTransactions = [
      // Income
      { amount: 65000, category: 'Salary', type: 'income', note: 'Monthly Salary Credit', ageDays: 25 },
      { amount: 5000, category: 'Investment', type: 'income', note: 'Mutual Fund Dividend', ageDays: 15 },
      
      // Rent & Bills (Current Month)
      { amount: 15000, category: 'Rent', type: 'expense', note: 'Appartment Rent Deposit', ageDays: 25 },
      { amount: 2400, category: 'Bills', type: 'expense', note: 'Electricity & Water charges', ageDays: 20 },
      { amount: 999, category: 'Bills', type: 'expense', note: 'Mobile post-paid & Broadband', ageDays: 18 },

      // Food (Current Month)
      { amount: 1200, category: 'Food', type: 'expense', note: 'Dinner at mainland china', ageDays: 10 },
      { amount: 850, category: 'Food', type: 'expense', note: 'Grocery shopping at BigBasket', ageDays: 8 },
      { amount: 1500, category: 'Food', type: 'expense', note: 'Weekend brunch', ageDays: 5 },
      { amount: 1800, category: 'Food', type: 'expense', note: 'Order from Swiggy Gourmet', ageDays: 2 },

      // Transport (Current Month)
      { amount: 1200, category: 'Transport', type: 'expense', note: 'Shell Petrol Pump refuel', ageDays: 14 },
      { amount: 350, category: 'Transport', type: 'expense', note: 'Uber ride to city mall', ageDays: 7 },
      { amount: 400, category: 'Transport', type: 'expense', note: 'Ola Cab fare', ageDays: 3 },

      // Shopping & Entertainment (Current Month)
      { amount: 4500, category: 'Shopping', type: 'expense', note: 'Zara clothing purchase', ageDays: 12 },
      { amount: 1500, category: 'Shopping', type: 'expense', note: 'Leather wallet', ageDays: 4 },
      { amount: 750, category: 'Entertainment', type: 'expense', note: 'Netflix Premium Monthly Renewal', ageDays: 24 },
      { amount: 1800, category: 'Entertainment', type: 'expense', note: 'Concert Tickets', ageDays: 9 },

      // Healthcare
      { amount: 1200, category: 'Healthcare', type: 'expense', note: 'Doctor consultation fee', ageDays: 13 },

      // Last Month Expenses (to compare MoM increases)
      { amount: 2500, category: 'Food', type: 'expense', note: 'Last month dining out', ageDays: 45 },
      { amount: 3000, category: 'Shopping', type: 'expense', note: 'Last month apparel buy', ageDays: 40 },
      { amount: 65000, category: 'Salary', type: 'income', note: 'Last month Salary Credit', ageDays: 55 }
    ];

    // Distribute transactions to primary seeded users
    users.slice(0, 3).forEach(user => {
      sampleTransactions.forEach(sample => {
        // Adjust date based on ageDays
        const date = new Date();
        date.setDate(date.getDate() - sample.ageDays);

        // Adjust amount slightly to keep user transactions unique
        const multiplier = user.email === 'aanya@vaultflow.com' ? 0.05 : 1.0; // scale for dollars
        const variance = 0.95 + Math.random() * 0.1;
        const finalAmount = Math.round(sample.amount * multiplier * variance);

        transactionsData.push({
          userId: user.userId,
          amount: finalAmount,
          category: sample.category,
          type: sample.type,
          date,
          note: user.email === 'aanya@vaultflow.com' ? sample.note.replace('₹', '$') : sample.note
        });
      });
    });

    const seededTransactions = await Transaction.create(transactionsData);
    console.log(`Seeded ${seededTransactions.length} transactions across users.`);

    // 4. Update the 'spent' sums in Budgets collection to match the seeded transactions
    console.log('Calculating and updating spent fields in Budgets...');
    const budgets = await Budget.find({});
    
    for (const budget of budgets) {
      const startDate = new Date(budget.year, budget.month - 1, 1);
      const endDate = new Date(budget.year, budget.month, 0, 23, 59, 59, 999);

      const expenses = await Transaction.aggregate([
        {
          $match: {
            userId: budget.userId,
            category: budget.category,
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

      const totalSpent = expenses.length > 0 ? expenses[0].total : 0;
      budget.spent = totalSpent;
      await budget.save();
    }
    console.log('Budget spent calculations updated successfully.');

    // 5. Seed Predefined AI Insights
    const insightsData = [];
    users.slice(0, 2).forEach(user => {
      // 1 budget warning (simulate high category usage)
      insightsData.push({
        userId: user.userId,
        type: 'budget_warning',
        message: `You've used 89% of your Food budget! Spent ₹5,350 of ₹6,000.`,
        isRead: false
      });

      // 1 spending alert (simulate spike)
      insightsData.push({
        userId: user.userId,
        type: 'spending_alert',
        message: `You spent 110% more on Shopping this month (₹4,500) compared to last month (₹2,140).`,
        isRead: false
      });

      // 1 monthly summary (simulate savings report)
      insightsData.push({
        userId: user.userId,
        type: 'monthly_summary',
        message: `Last month you saved ₹28,450, which is 43% of your total income. Excellent progress!`,
        isRead: true
      });

      // 1 savings tip
      insightsData.push({
        userId: user.userId,
        type: 'savings_tip',
        message: 'Track your daily subscriptions. Canceling just one unused ₹299 monthly service saves ₹3,588 a year!',
        isRead: false
      });
    });

    const seededInsights = await Insight.create(insightsData);
    console.log(`Seeded ${seededInsights.length} initial AI Insights.`);

    console.log('Database seeding completed successfully!');
    process.exit(0);
  } catch (error) {
    console.error(`Seeding failed: ${error.message}`);
    process.exit(1);
  }
};

seedDatabase();
