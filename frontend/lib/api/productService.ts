import apiClient from './client';
import { Product } from '../types';

export const productService = {
    getAll: async (): Promise<Product[]> => {
        const response = await apiClient.get('/products');
        return response.data;
    },
    create: async (product: Partial<Product>): Promise<Product> => {
        const response = await apiClient.post('/products', product);
        return response.data;
    },
    update: async (id: number, product: Partial<Product>): Promise<Product> => {
        const response = await apiClient.put(`/products/${id}`, product);
        return response.data;
    },
    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/products/${id}`);
    },
};
