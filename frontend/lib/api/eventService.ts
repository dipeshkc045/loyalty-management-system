import apiClient from './client';
import { EventRequest } from '../types';

export const eventService = {
    // Trigger onboarding event
    onboard: async (memberId: number): Promise<string> => {
        const response = await apiClient.post('/events/onboard', { memberId });
        return response.data;
    },

    // Trigger referral event
    referral: async (referrerId: number, refereeId: number): Promise<string> => {
        const response = await apiClient.post('/events/referral', {
            referrerId,
            refereeId,
        });
        return response.data;
    },
};
