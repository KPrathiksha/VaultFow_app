# VaultFlow Backend REST API

This is the secure, production-ready, and containerized Node.js + Express + MongoDB backend for **VaultFlow** - an AI-Powered Personal Finance Manager mobile application built with Flutter.

---

## 🚀 Key Features

*   **Secure Authentication**: JWT authorization valid for 7 days, with secure password hashing via `bcryptjs`.
*   **Transaction Manager**: Paginated CRUD queries, custom categories filter, and income vs expense aggregations.
*   **Live Budgets Progress**: Auto-tracked and upserted monthly category limit targets, with real-time expenditure calculations.
*   **Analytical AI Engine**: Rule-based MVP analytics generating real-time budget warnings (at 80%+ usage), MoM spending surge spikes (20%+ increases), savings rates, and financial tips.
*   **Advanced Analytics**: Custom aggregations for monthly breakouts, percentage compositions, and MoM trend graphs.
*   **Demo Bank Sync**: Simulates transaction integrations with 5 major Indian banking institutions.
*   **Docker Ready**: Simple containerization for local execution or cloud stacks.

---

## 🛠️ Technology Stack

*   **Core API Framework**: [Node.js](https://nodejs.org) + [Express.js](https://expressjs.com)
*   **Database Engine**: [MongoDB](https://www.mongodb.com) + [Mongoose ODM](https://mongoosejs.com)
*   **Security Guards**: [Helmet](https://helmetjs.github.io/) (HTTP Headers), [CORS](https://github.com/expressjs/cors) (Flutter requests), [Express-Rate-Limit](https://github.com/express-rate-limit/express-rate-limit) (100 reqs/min)
*   **Validation Rules**: [Express-Validator](https://express-validator.github.io)
*   **Testing Suite**: [Jest](https://jestjs.io) + [Supertest](https://github.com/ladjs/supertest)

---

## 📁 Repository Structure

```text
backend/
├── src/
│   ├── config/db.js           # Database Mongoose connector
│   ├── models/                # User, Transaction, Budget, Insight schemas
│   ├── middleware/            # JWT auth guard, rate limiters, error handlers
│   ├── controllers/           # Auth, CRUD operations, aggregations
│   ├── routes/                # Endpoint routing mounts
│   ├── services/              # AI rule engine & simulated bank sync
│   └── app.js                 # Express main entrypoint
├── scripts/
│   └── seed.js                # Database seeder (5 users, 25+ transactions, budgets, insights)
├── tests/
│   └── auth.test.js           # Automated Jest integration tests
├── Dockerfile                 # Container configurations
├── docker-compose.yml         # Container stack composition (Express + Mongo)
├── swagger.json               # OpenAPI documentation specs
└── README.md                  # This file
```

---

## 💻 Local Quickstart

### Option A: Standard Native Setup
1.  **Clone the backend folder** and navigate to it:
    ```bash
    cd backend
    ```
2.  **Install dependencies**:
    ```bash
    npm install
    ```
3.  **Setup environment variables**:
    Copy `.env.example` to `.env` and fill in your values (or use defaults):
    ```bash
    cp .env.example .env
    ```
4.  **Run a local MongoDB instance** (ensure your MongoDB service is running on `mongodb://localhost:27017`).
5.  **Seed the Database**:
    ```bash
    npm run seed
    ```
6.  **Start the server in Development mode**:
    ```bash
    npm run dev
    ```
    The API will be live at `http://localhost:5001`.

### Option B: Docker Compose (Highly Recommended)
No need to install Node or MongoDB on your host machine!
1.  Ensure you have **Docker Desktop** installed.
2.  Inside the `backend/` directory, launch the stack:
    ```bash
    docker-compose up --build
    ```
    This launches:
    *   `vaultflow_backend`: Node Express application listening on `http://localhost:5001`.
    *   `vaultflow_mongodb`: MongoDB database persistence mapped locally to `mongodb://localhost:27017` with a persistent docker volume.
3.  Seed the Docker container database:
    ```bash
    docker exec -it vaultflow_backend npm run seed
    ```

---

## 🧪 Running Automated Tests

Launch the Jest integration suite to verify registration and login paths:
```bash
npm run test
```

---

## ☁️ Deployment Instructions

### Render (Option A)
1.  Create a **Web Service** on [Render](https://render.com).
2.  Connect your GitHub repository.
3.  Choose **Node** as the environment runtime.
4.  Set build and start scripts:
    *   **Build Command**: `npm install`
    *   **Start Command**: `npm start`
5.  Launch a **Render Blueprint (Infrastructure as Code)** or spin up a free **MongoDB Atlas Database** to get a connection string.
6.  Add **Environment Variables** in Render Settings:
    *   `PORT` = `10000` (Render binds this dynamically)
    *   `MONGO_URI` = `mongodb+srv://<user>:<password>@cluster.mongodb.net/vaultflow`
    *   `JWT_SECRET` = `your_secure_random_jwt_key_here`
    *   `NODE_ENV` = `production`

### Railway (Option B)
1.  Click **New Project** on [Railway](https://railway.app).
2.  Add a **MongoDB** plugin database.
3.  Click **New Service** -> **Github Repo** -> choose your repo.
4.  Railway automatically reads the `Dockerfile` in your repository.
5.  Link environment variables:
    *   Bind `MONGO_URI` to `${{MongoDB.MONGO_URL}}`
    *   Set `JWT_SECRET` to a random value.
6.  Deploy!

---

## 📱 Flutter Client Integration

The Flutter client integrations are fully written inside the root folder:
*   **Models**: Located in `lib/models/` (`user.dart`, `transaction.dart`, `budget.dart`, `insight.dart`).
*   **Client Core**: Located in `lib/services/api_service.dart`. Initialize `ApiService(baseUrl: "http://localhost:5001")`.
*   **State Management**: Located in `lib/providers/finance_provider.dart`. Uses Flutter `ChangeNotifierProvider` to bind UI widgets and trigger dynamic state updates.
