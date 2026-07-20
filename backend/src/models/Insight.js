const mongoose = require('mongoose');
const crypto = require('crypto');

const InsightSchema = new mongoose.Schema(
  {
    insightId: {
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
    type: {
      type: String,
      required: [true, 'Insight type is required'],
      enum: {
        values: ['spending_alert', 'savings_tip', 'budget_warning', 'monthly_summary'],
        message: '{VALUE} is not a valid insight type'
      }
    },
    message: {
      type: String,
      required: [true, 'Insight message is required'],
      trim: true
    },
    isRead: {
      type: Boolean,
      default: false
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

module.exports = mongoose.model('Insight', InsightSchema);
