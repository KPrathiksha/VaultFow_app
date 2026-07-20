const mongoose = require('mongoose');
const crypto = require('crypto');

const TransactionSchema = new mongoose.Schema(
  {
    transactionId: {
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
    amount: {
      type: Number,
      required: [true, 'Amount is required']
    },
    category: {
      type: String,
      required: [true, 'Category is required'],
      enum: {
        values: [
          'Food', 'Transport', 'Shopping', 'Bills', 'Entertainment',
          'Healthcare', 'Education', 'Rent', 'Utilities', 'Salary',
          'Investment', 'Others'
        ],
        message: '{VALUE} is not a supported category'
      }
    },
    type: {
      type: String,
      required: [true, 'Transaction type is required'],
      enum: {
        values: ['income', 'expense'],
        message: '{VALUE} is not a valid transaction type'
      }
    },
    date: {
      type: Date,
      default: Date.now,
      index: true
    },
    note: {
      type: String,
      trim: true,
      default: ""
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

module.exports = mongoose.model('Transaction', TransactionSchema);
