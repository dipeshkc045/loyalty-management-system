# LMS Frontend - Loyalty Management System

A modern Next.js frontend for managing customer loyalty programs, built with TypeScript and Tailwind CSS.

## Features

- 📊 **Dashboard** - Real-time statistics and tier distribution
- 👥 **Member Management** - Create and manage customer members
- 💳 **Transactions** - Record purchases and award points
- 📋 **Rules Engine** - Manage loyalty rules and bonuses
- 🎁 **Events** - Trigger onboarding and referral bonuses

## Tech Stack

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **HTTP Client**: Axios
- **Icons**: Lucide React

## Prerequisites

- Node.js 18+ and npm
- Backend services running on `http://localhost:8080`

## Installation

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

4. Open [http://localhost:3000](http://localhost:3000) in your browser

## Project Structure

```
frontend/
├── app/                    # Next.js app directory
│   ├── layout.tsx         # Root layout with navigation
│   ├── page.tsx           # Dashboard page
│   ├── members/           # Member management pages
│   ├── transactions/      # Transaction pages
│   ├── rules/             # Rules management pages
│   └── events/            # Events pages
├── lib/
│   ├── api/               # API service layer
│   │   ├── client.ts      # Axios configuration
│   │   ├── memberService.ts
│   │   ├── transactionService.ts
│   │   ├── ruleService.ts
│   │   └── eventService.ts
│   └── types/             # TypeScript type definitions
└── components/            # Reusable React components
```

## API Integration

The frontend communicates with the backend through the API Gateway at `http://localhost:8080/api/v1`.

API endpoints:
- `/members` - Member CRUD operations
- `/transactions` - Transaction creation and summaries
- `/rules` - Rule management
- `/events` - Event triggering

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm start` - Start production server
- `npm run lint` - Run ESLint

## Environment Variables

Create a `.env.local` file if you need to customize the API URL:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

## Features Overview

### Dashboard
- Total members count
- Total points awarded
- Tier distribution (Platinum, Gold, Silver, Bronze)
- Visual statistics

### Members
- View all members in a table
- Create new members
- View member details
- See tier and points

### Transactions
- Create transactions for members
- Select payment method and category
- Automatic point calculation via backend rules

### Rules
- View all active loyalty rules
- Delete rules
- Reload rules dynamically
- View DRL (Drools Rule Language) content

### Events
- Trigger onboarding bonuses (500 points)
- Trigger referral bonuses (1000 to referrer, 500 to referee)

## Development

The frontend uses Next.js API rewrites to proxy requests to the backend, avoiding CORS issues during development.

## Production Build

```bash
npm run build
npm start
```

## License

MIT
