# Loyalty Management System (LMS)

The Loyalty Management System (LMS) is a distributed platform designed to automate customer engagement through rewards. It enables businesses to define flexible loyalty rules, track customer transactions, and award points/discounts based on real-time behavior.

![Architecture](https://via.placeholder.com/800x400?text=LMS+Architecture+Diagram)
*(Placeholder for Architecture Diagram - See Documentation for detailed sequence flows)*

## 🚀 Key Features
- **Microservices Architecture**: built with Spring Boot, ensuring scalability and separation of concerns.
- **Event-Driven**: uses RabbitMQ for asynchronous communication between services.
- **Dynamic Rule Engine**: integrated with JBoss Drools to handle complex reward logic dynamically.
- **Tier Management**: automatic tier progression based on point accumulation.
- **Frontend Dashboard**: Next.js-based UI for managing members, transactions, and rules.

---

## 🛠️ Technology Stack
- **Backend Framework**: Spring Boot 3.4.1 (Java 21)
- **Frontend Framework**: Next.js 14 (TypeScript, Tailwind CSS)
- **Messaging**: RabbitMQ
- **Database**: PostgreSQL 15
- **Caching**: Redis 7
- **Rule Engine**: JBoss Drools 8.x
- **API Management**: Spring Cloud Gateway

---

## 🏗️ System Architecture

| Service | Port | Primary Responsibility |
| :--- | :--- | :--- |
| **API Gateway** | 8080 | Routing, load balancing, and central API entry point. |
| **Member Service** | 8081 | Lifecycle of members, tiers, and point balance management. |
| **Transaction Service** | 8082 | Recording sales and publishing transactional events. |
| **Rule Engine** | 8083 | Evaluating complex DRL rules against transaction facts. |
| **Reward Service** | 8084 | Orchestrating reward logic and multiplier calculations. |
| **Event Service** | 8085 | Triggering manual or systemic events like Onboarding/Referrals. |
| **Notification Service** | 8086 | Consumer-only service for user communication (logs/emails). |

---

## 🏁 Getting Started

### Prerequisites
- Java 21+
- Node.js 18+ (for Frontend)
- Docker & Docker Compose

### 1. Start Infrastructure
Run the database, messaging, and caching services using Docker.
```bash
docker-compose up -d
```

### 2. Build and Run Backend Services
You can run each service individually or use the provided scripts (if available).
```bash
./gradlew build
# Run specific service
java -jar services/api-gateway/build/libs/api-gateway-0.0.1-SNAPSHOT.jar
```

### 3. Run Frontend
Navigate to the frontend directory and start the dev server.
```bash
cd frontend
npm install
npm run dev
```
Access the dashboard at `http://localhost:3000`.

---

## 📚 API Reference (Core Endpoints)
The API Gateway exposes the following unified routes:

### Member Management
- `POST /api/v1/members`: Register a new member.
- `GET /api/v1/members/{id}`: Fetch member details.

### Transactions
- `POST /api/v1/transactions`: Record a new transaction.
- `GET /api/v1/transactions/summary/{memberId}`: Get spending summary.

### Rule Management
- `POST /api/v1/rules`: Create a new Drools rule.
- `GET /api/v1/rules`: List active rules.

---

## 📖 Further Documentation
For detailed architectural flows and sequence diagrams, please refer to [SYSTEM_DOCUMENTATION.md](SYSTEM_DOCUMENTATION.md).
