import apiClient from './client'
import { Rule } from '../types'

export const ruleService = {
    // Get all rules
    getAll: async (): Promise<Rule[]> => {
        const response = await apiClient.get('/rules')
        return response.data
    },

    // Create a new rule
    create: async (rule: Partial<Rule>): Promise<Rule> => {
        const response = await apiClient.post('/rules', rule)
        return response.data
    },

    // Update an existing rule
    update: async (id: number, rule: Partial<Rule>): Promise<Rule> => {
        const response = await apiClient.put(`/rules/${id}`, rule)
        return response.data
    },

    // Delete a rule
    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/rules/${id}`)
    }
}
