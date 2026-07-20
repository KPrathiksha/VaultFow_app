const mongoose = require('mongoose');
const crypto = require('crypto');

const BudgetSchema = new mongoose.Schema(
  {
    budgetId: {
      type: String,
      default: () => crypto.randomUUID(),
      unique: true,
      index: true
    },
    userId: {
      type: String,
      ref: 'User',
      required: [true, 'User ID is required'],
      index: true
    },
    category: {
      type: String,
      required: [true, 'Category is required'],
      enum: {
        values: [
          'Food', 'Transport', 'Shopping', 'Bills', 'Entertainment',
          'Healthcare', 'Education', 'Rent', 'Utilities', 'Others'
        ],
        message: '{VALUE} is not a valid budget category'
      }
    },
    amount: {
      type: Number,
      required: [true, 'Budget amount is required'],
      min: [0, 'Amount must be greater than or equal to 0']
    },
    month: {
      type: Number,
      required: [true, 'Month is required'],
      min: [1, 'Month must be between 1 and 12'],
      max: [12, 'Month must be between 1 and 12']
    },
    year: {
      type: Number,
      required: [true, 'Year is required']
    },
    spent: {
      type: Number,
      default: 0,
      min: [0, 'Spent amount must be non-negative']
    }
  },
  {
    timestamps: true,
    toJSON: {
      transform: function (doc, ret) {
        delete ret._id;
        delete ret.__v;
        return ret;
      }
    }
  }
);

// Compound unique index so a user cannot have two budget targets for the exact same category/month/year.
BudgetSchema.index({ userId: 1, category: 1, month: 1, year: 1 }, { unique: true });

module.exports = mongoose.model('Budget', BudgetSchema);
