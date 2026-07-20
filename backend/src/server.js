require('dotenv').config();
const app = require('./app');
const connectDB = async () => {
  // Connect to database
  const dbConnector = require('./config/db');
  await dbConnector();
};

const startServer = async () => {
  // Connect database first
  await connectDB();

  const PORT = process.env.PORT || 5001;

  const server = app.listen(PORT, () => {
    console.log(`VaultFlow Backend Server listening on port ${PORT} in ${process.env.NODE_ENV || 'development'} mode.`);
  });

  // Handle unhandled promise rejections globally
  process.on('unhandledRejection', (err, promise) => {
    console.error(`Unhandled Rejection Error: ${err.message}`);
    // Close server & exit process
    server.close(() => process.exit(1));
  });

  // Handle uncaught exceptions globally
  process.on('uncaughtException', (err) => {
    console.error(`Uncaught Exception Error: ${err.message}`);
    process.exit(1);
  });
};

startServer();
