# Build stage
FROM node:18-alpine

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies (include development tools for local composition running)
RUN npm install

# Copy the rest of the application code
COPY . .

# Expose server port
EXPOSE 5001

# Command to run the application
CMD ["npm", "start"]
