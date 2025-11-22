import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,

      setAuth: (user, accessToken, refreshToken) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        set({ user, accessToken, refreshToken });
      },

      clearAuth: () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        set({ user: null, accessToken: null, refreshToken: null });
      },

      logout: () => {
        get().clearAuth();
      },

      isAuthenticated: () => {
        const token = localStorage.getItem('accessToken');
        return !!token;
      }
    }),
    {
      name: 'auth-storage',
    }
  )
);
