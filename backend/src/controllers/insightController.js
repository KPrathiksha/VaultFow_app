const Insight = require('../models/Insight');
const { generateAllInsights } = require('../services/aiService');

// @desc    Get all financial insights for authenticated user
// @route   GET /api/insights
// @access  Private
const getInsights = async (req, res, next) => {
  try {
    const insights = await Insight.find({ userId: req.user.userId }).sort({ createdAt: -1 });

    res.status(200).json({
      success: true,
      count: insights.length,
      data: insights
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Get all unread financial insights
// @route   GET /api/insights/unread
// @access  Private
const getUnreadInsights = async (req, res, next) => {
  try {
    const insights = await Insight.find({
      userId: req.user.userId,
      isRead: false
    }).sort({ createdAt: -1 });

    res.status(200).json({
      success: true,
      count: insights.length,
      data: insights
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Mark specific insight as read
// @route   PUT /api/insights/:id/read
// @access  Private
const markAsRead = async (req, res, next) => {
  try {
    const insight = await Insight.findOneAndUpdate(
      { insightId: req.params.id, userId: req.user.userId },
      { isRead: true },
      { new: true }
    );

    if (!insight) {
      return res.status(404).json({
        success: false,
        error: `Insight not found with id ${req.params.id}`
      });
    }

    res.status(200).json({
      success: true,
      data: insight
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Force generate AI insights for the current user
// @route   POST /api/insights/generate
// @access  Private
const triggerGenerateInsights = async (req, res, next) => {
  try {
    await generateAllInsights(req.user.userId);

    // Fetch the newly updated insights to return to the user
    const insights = await Insight.find({ userId: req.user.userId }).sort({ createdAt: -1 });

    res.status(200).json({
      success: true,
      message: 'AI Insights generated and refreshed successfully',
      data: insights
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getInsights,
  getUnreadInsights,
  markAsRead,
  triggerGenerateInsights
};
