// TypeScript types for LMS entities

export interface Member {
    id?: number;
    email: string;
    name: string;
    phone: string;
    tier: 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM' | 'DIAMOND';
    role: 'CUSTOMER' | 'ADMIN';
    totalPoints: number;
    lifetimePoints: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface PagedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
}


export interface Transaction {
    id?: number;
    memberId: number;
    amount: number;
    paymentMethod: string;
    productCategory: string;
    pointsEarned?: number;
    status?: 'PENDING' | 'COMPLETED' | 'FAILED';
    transactionDate?: string;
}

export interface TransactionSummary {
    memberId: number;
    transactionCount: number;
    totalAmount: number;
}

export interface Rule {
    id?: number;
    ruleType: 'DROOLS' | 'EVENT' | 'TRANSACTION' | 'PRODUCT' | 'REWARD' | 'SIMPLE';
    ruleName: string;
    conditions: any;
    actions: any;
    drlContent?: string;
    priority: number;
    isActive: boolean;
    validFrom?: string | null;
    validUntil?: string | null;

    // New Advanced Fields
    evaluationType?: 'TRANSACTION' | 'MONTHLY' | 'QUARTERLY';
    targetProductCode?: string;
    targetProductCodes?: string[];
    minAmount?: number;
    maxAmount?: number;
    minVolume?: number;
    maxVolume?: number;
    targetTier?: string;
    rewardType?: 'POINTS' | 'DISCOUNT';
}

export interface Product {
    id?: number;
    code: string;
    name: string;
    category: string;
    description: string;
    isActive: boolean;
    createdAt?: string;
}

export interface TransactionFact {
    memberId: number;
    memberTier: string;
    amount: number;
    paymentMethod: string;
    productCategory: string;
    pointMultiplier: number;
    bonusPoints: number;
}

export interface EventRequest {
    memberId?: number;
    referrerId?: number;
    refereeId?: number;
}

export interface DashboardStats {
    totalMembers: number;
    totalPoints: number;
    tierCounts: Record<string, number>;
    platinumCount: number;
    diamondCount: number;
    goldCount: number;
    silverCount: number;
    bronzeCount: number;
}
