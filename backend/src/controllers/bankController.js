const { SUPPORTED_BANKS, syncMockBankTransactions } = require('../services/bankService');

// @desc    Simulate sync of transactions from a supported bank
// @route   POST /api/bank/sync
// @access  Private
const syncBank = async (req, res, next) => {
  try {
    const { bankId } = req.body;

    if (!bankId) {
      return res.status(400).json({
        success: false,
        error: 'Bank ID is required for synchronization'
      });
    }

    const bankExists = SUPPORTED_BANKS.find(b => b.id === bankId.toLowerCase());
    if (!bankExists) {
      return res.status(400).json({
        success: false,
        error: `Supported bank with ID "${bankId}" was not found`
      });
    }

    if (bankExists.status === 'maintenance') {
      return res.status(503).json({
        success: false,
        error: `Sync connection with ${bankExists.name} is currently down for maintenance. Please try again later.`
      });
    }

    const transactions = await syncMockBankTransactions(req.user.userId, bankId);

    res.status(200).json({
      success: true,
      message: `Successfully synchronized ${transactions.length} transactions from ${bankExists.name}.`,
      count: transactions.length,
      data: transactions
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Get list of supported banks for demo sync
// @route   GET /api/bank/supported
// @access  Private
const getSupportedBanks = async (req, res, next) => {
  try {
    res.status(200).json({
      success: true,
      count: SUPPORTED_BANKS.length,
      data: SUPPORTED_BANKS
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  syncBank,
  getSupportedBanks
};
