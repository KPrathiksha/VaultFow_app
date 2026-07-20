const mongoose = require('mongoose');

const connectDB = async () => {
  try {
    const connStr = process.env.MONGO_URI || 'mongodb://localhost:27017/vaultflow';
    console.log(`Connecting to MongoDB at: ${connStr.replace(/:([^:@]+)@/, ':****@')}`);
    
    const conn = await mongoose.connect(connStr);
    
    console.log(`MongoDB Connected successfully: ${conn.connection.host}`);
  } catch (error) {
    console.error(`MongoDB Connection Error: ${error.message}`);
    process.exit(1);
  }
};

module.exports = connectDB;
