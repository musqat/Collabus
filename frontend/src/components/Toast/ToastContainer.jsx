import { useToastStore } from '../../store/toastStore';
import Toast from './Toast';

export default function ToastContainer() {
  const toasts = useToastStore((state) => state.toasts);

  return (
    <div className="fixed top-4 right-4 z-50 max-w-md w-full pointer-events-none">
      <div className="pointer-events-auto">
        {toasts.map((toast) => (
          <Toast key={toast.id} toast={toast} />
        ))}
      </div>
    </div>
  );
}
