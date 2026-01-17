import apiClient from './client';
import { Member, PagedResponse, DashboardStats } from '../types';

export const memberService = {
    // Get paginated members with optional search and filter
    getAll: async (page = 0, size = 20, search?: string, tier?: string): Promise<PagedResponse<Member>> => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            ...(search && { search }),
            ...(tier && { tier }),
        });
        const response = await apiClient.get(`/members?${params}`);
        return response.data;
    },

    // Get lite member list for dropdowns (cached)
    getLite: async (limit = 100): Promise<Member[]> => {
        const response = await apiClient.get(`/members/lite?limit=${limit}`);
        return response.data;
    },

    // Get dashboard stats
    getStats: async (): Promise<DashboardStats> => {
        const response = await apiClient.get('/members/stats');
        return response.data;
    },

    // Get member by ID
    getById: async (id: number): Promise<Member> => {
        const response = await apiClient.get(`/members/${id}`);
        return response.data;
    },

    // Create new member
    create: async (member: Omit<Member, 'id' | 'createdAt' | 'updatedAt'>): Promise<Member> => {
        const response = await apiClient.post('/members', member);
        return response.data;
    },

    // Update member
    update: async (id: number, member: Partial<Member>): Promise<Member> => {
        const response = await apiClient.put(`/members/${id}`, member);
        return response.data;
    },

    // Get member points
    getPoints: async (id: number): Promise<number> => {
        const response = await apiClient.get(`/members/${id}/points`);
        return response.data;
    },
};
