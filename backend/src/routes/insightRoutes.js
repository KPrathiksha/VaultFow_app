const express = require('express');
const {
  getInsights,
  getUnreadInsights,
  markAsRead,
  triggerGenerateInsights
} = require('../controllers/insightController');
const { protect } = require('../middleware/auth');

const router = express.Router();

// Apply auth shield to all routes
router.use(protect);

// GET /api/insights & POST /api/insights/generate
router.get('/', getInsights);
router.post('/generate', triggerGenerateInsights);

// GET /api/insights/unread
router.get('/unread', getUnreadInsights);

// PUT /api/insights/:id/read
router.put('/:id/read', markAsRead);

module.exports = router;
