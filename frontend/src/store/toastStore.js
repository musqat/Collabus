import { create } from 'zustand';

export const useToastStore = create((set) => ({
  toasts: [],

  addToast: (toast) => {
    const id = Date.now() + Math.random();
    const newToast = {
      id,
      type: toast.type || 'info', // 'success', 'error', 'warning', 'info'
      message: toast.message,
      duration: toast.duration || 3000,
    };

    set((state) => ({
      toasts: [...state.toasts, newToast],
    }));

    // 자동 제거
    if (newToast.duration > 0) {
      setTimeout(() => {
        set((state) => ({
          toasts: state.toasts.filter((t) => t.id !== id),
        }));
      }, newToast.duration);
    }

    return id;
  },

  removeToast: (id) => {
    set((state) => ({
      toasts: state.toasts.filter((toast) => toast.id !== id),
    }));
  },

  clearAll: () => {
    set({ toasts: [] });
  },
}));

export const showToast = {
  success: (message, duration) => useToastStore.getState().addToast({ type: 'success', message, duration }),
  error: (message, duration) => useToastStore.getState().addToast({ type: 'error', message, duration }),
  warning: (message, duration) => useToastStore.getState().addToast({ type: 'warning', message, duration }),
  info: (message, duration) => useToastStore.getState().addToast({ type: 'info', message, duration }),
};
