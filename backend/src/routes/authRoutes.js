const express = require('express');
const { body } = require('express-validator');
const { register, login, logout, getMe } = require('../controllers/authController');
const { protect } = require('../middleware/auth');
const { validateFields } = require('../middleware/validation');

const router = express.Router();

// Register route
router.post(
  '/register',
  [
    body('name', 'Name is required').notEmpty().trim(),
    body('email', 'Please include a valid email').isEmail().normalizeEmail(),
    body('password', 'Password must be at least 6 characters').isLength({ min: 6 }),
    body('currency').optional().isString(),
    body('monthlyBudget').optional().isNumeric(),
    validateFields
  ],
  register
);

// Login route
router.post(
  '/login',
  [
    body('email', 'Please include a valid email').isEmail().normalizeEmail(),
    body('password', 'Password is required').notEmpty(),
    validateFields
  ],
  login
);

// Logout route
router.post('/logout', logout);

// Get Profile route
router.get('/me', protect, getMe);

module.exports = router;
