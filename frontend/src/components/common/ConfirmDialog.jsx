import { AlertTriangle } from 'lucide-react';
import { Button } from './Button';
import { Modal } from './Modal';

export function ConfirmDialog({ isOpen, onClose, onConfirm, title, message, confirmText = 'Confirm', loading = false, variant = 'danger' }) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title || 'Confirm Action'} size="sm">
      <div className="flex flex-col items-center text-center gap-4">
        <div className="w-12 h-12 rounded-full bg-danger-100 flex items-center justify-center">
          <AlertTriangle className="w-6 h-6 text-danger-600" />
        </div>
        <p className="text-sm text-surface-600">{message || 'Are you sure you want to proceed?'}</p>
        <div className="flex items-center gap-3 w-full">
          <Button variant="secondary" className="flex-1" onClick={onClose} disabled={loading}>Cancel</Button>
          <Button variant={variant} className="flex-1" onClick={onConfirm} loading={loading}>{confirmText}</Button>
        </div>
      </div>
    </Modal>
  );
}
