import apiClient from './client';
import { Transaction, TransactionSummary } from '../types';

export const transactionService = {
    // Create new transaction
    create: async (transaction: Omit<Transaction, 'id' | 'transactionDate'>): Promise<Transaction> => {
        const response = await apiClient.post('/transactions', transaction);
        return response.data;
    },

    // Get transaction summary
    getSummary: async (memberId: number, period?: string): Promise<TransactionSummary> => {
        const params = period ? { period } : {};
        const response = await apiClient.get(`/transactions/summary/${memberId}`, { params });
        return response.data;
    },

    // Get all transactions
    getAll: async (): Promise<Transaction[]> => {
        const response = await apiClient.get('/transactions');
        return response.data;
    },

    // Get transactions by member
    getByMember: async (memberId: number): Promise<Transaction[]> => {
        const response = await apiClient.get<Transaction[]>(`/transactions/member/${memberId}`);
        return response.data;
    },
};
