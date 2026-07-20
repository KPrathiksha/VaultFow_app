const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const { apiLimiter } = require('./middleware/rateLimiter');
const errorHandler = require('./middleware/errorHandler');

// Route Imports
const authRoutes = require('./routes/authRoutes');
const transactionRoutes = require('./routes/transactionRoutes');
const budgetRoutes = require('./routes/budgetRoutes');
const insightRoutes = require('./routes/insightRoutes');
const analyticsRoutes = require('./routes/analyticsRoutes');
const bankRoutes = require('./routes/bankRoutes');

const app = express();

// 1. Security & Request Parsing Middlewares
app.use(helmet()); // Sets protective HTTP headers
app.use(cors()); // Enables cross-origin request sharing for Flutter app
app.use(express.json()); // Built-in body parser for raw JSON payloads
app.use(express.urlencoded({ extended: true }));

// 2. Global Rate Limiter (100 reqs/minute)
app.use('/api', apiLimiter);

// 3. API Endpoints Mounting
app.use('/api/auth', authRoutes);
app.use('/api/transactions', transactionRoutes);
app.use('/api/budgets', budgetRoutes);
app.use('/api/insights', insightRoutes);
app.use('/api/analytics', analyticsRoutes);
app.use('/api/bank', bankRoutes);

// Base Route
app.get('/', (req, res) => {
  res.status(200).json({
    success: true,
    message: 'Welcome to the VaultFlow Personal Finance Manager REST API'
  });
});

// 4. Undefined Route Handler (404)
app.use((req, res, next) => {
  res.status(404).json({
    success: false,
    error: 'Requested API endpoint does not exist'
  });
});

// 5. Centralized Global Error Handler Middleware
app.use(errorHandler);

module.exports = app;
