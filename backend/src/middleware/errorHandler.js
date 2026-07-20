const errorHandler = (err, req, res, next) => {
  let error = { ...err };
  error.message = err.message;

  // Log error console details
  if (process.env.NODE_ENV !== 'test') {
    console.error(err);
  }

  // Mongoose Bad ObjectId or Cast Error (e.g. invalid UUID format if customized)
  if (err.name === 'CastError') {
    const message = `Resource not found with id of ${err.value}`;
    error = new Error(message);
    error.statusCode = 404;
  }

  // Mongoose Duplicate Key Error (e.g. duplicate email)
  if (err.code === 11000) {
    let message = 'Duplicate field value entered';
    if (err.keyValue) {
      const keys = Object.keys(err.keyValue);
      message = `A record already exists with that ${keys.join(', ')}.`;
    }
    error = new Error(message);
    error.statusCode = 400;
  }

  // Mongoose Validation Error
  if (err.name === 'ValidationError') {
    const message = Object.values(err.errors).map(val => val.message).join(', ');
    error = new Error(message);
    error.statusCode = 400;
  }

  res.status(error.statusCode || 500).json({
    success: false,
    error: error.message || 'Server Error'
  });
};

module.exports = errorHandler;
