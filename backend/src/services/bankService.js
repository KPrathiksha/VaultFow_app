const Transaction = require('../models/Transaction');
const Budget = require('../models/Budget');
const { checkBudgetThresholds } = require('./aiService');

const SUPPORTED_BANKS = [
  { id: 'hdfc', name: 'HDFC Bank', country: 'IN', status: 'active' },
  { id: 'sbi', name: 'State Bank of India (SBI)', country: 'IN', status: 'active' },
  { id: 'icici', name: 'ICICI Bank', country: 'IN', status: 'active' },
  { id: 'axis', name: 'Axis Bank', country: 'IN', status: 'active' },
  { id: 'kotak', name: 'Kotak Mahindra Bank', country: 'IN', status: 'maintenance' }
];

const MOCK_BANK_TRANSACTIONS = [
  { amount: 1500, category: 'Food', type: 'expense', note: 'Dinner with friends at Barbeque Nation' },
  { amount: 450, category: 'Transport', type: 'expense', note: 'Uber ride to office' },
  { amount: 3500, category: 'Shopping', type: 'expense', note: 'Nike sneakers sale purchase' },
  { amount: 25000, category: 'Salary', type: 'income', note: 'Monthly payroll deposit' },
  { amount: 1200, category: 'Entertainment', type: 'expense', note: 'PVR movie ticket and popcorn' },
  { amount: 800, category: 'Bills', type: 'expense', note: 'Airtel Fiber Broadband bill payment' },
  { amount: 6500, category: 'Utilities', type: 'expense', note: 'Electricity bill payment' },
  { amount: 300, category: 'Healthcare', type: 'expense', note: 'Apollo Pharmacy medicines' }
];

/**
 * Generate 3-5 random transactions from the mock list and insert into DB for the user
 */
const syncMockBankTransactions = async (userId, bankId) => {
  try {
    // Select a random sample of 3 to 5 transactions
    const count = Math.floor(Math.random() * 3) + 3; // 3 to 5
    const shuffled = [...MOCK_BANK_TRANSACTIONS].sort(() => 0.5 - Math.random());
    const selected = shuffled.slice(0, count);

    const createdTransactions = [];

    for (const item of selected) {
      // Add a slight random variance in amounts (+/- 10%) to make it look active/real
      const varianceFactor = 0.9 + Math.random() * 0.2;
      const finalAmount = Math.round(item.amount * varianceFactor);

      // Create transaction
      const transaction = await Transaction.create({
        userId,
        amount: finalAmount,
        category: item.category,
        type: item.type,
        date: new Date(), // current time
        note: `${item.note} (Synced via ${bankId.toUpperCase()})`
      });

      createdTransactions.push(transaction);

      // If expense, update the budget!
      if (item.type === 'expense') {
        const month = transaction.date.getMonth() + 1;
        const year = transaction.date.getFullYear();

        const budget = await Budget.findOne({
          userId,
          category: item.category,
          month,
          year
        });

        if (budget) {
          budget.spent += finalAmount;
          await budget.save();

          // Check if warning triggers
          checkBudgetThresholds(userId, item.category, month, year).catch(err =>
            console.error(`AI Budget warning error in bank sync: ${err.message}`)
          );
        }
      }
    }

    return createdTransactions;
  } catch (error) {
    console.error('Error generating sync bank transactions:', error.message);
    throw error;
  }
};

module.exports = {
  SUPPORTED_BANKS,
  syncMockBankTransactions
};
