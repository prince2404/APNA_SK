import { useState } from 'react';
import { KeyRound, Eye, EyeOff, ArrowRight } from 'lucide-react';
import { Navigate } from 'react-router-dom';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { useAuth } from '@/hooks/useAuth';
import { useAuthStore } from '@/store/useAuthStore';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { ROUTES } from '@/constants/routePaths';
import { ToastContainer } from '@/components/common/ToastContainer';

function PasswordField({ label, field, value, showPassword, onChange, onToggleShow }) {
  return (
    <div className="relative">
      <Input
        label={label}
        type={showPassword ? 'text' : 'password'}
        placeholder="••••••••"
        value={value}
        onChange={onChange}
      />
      <button type="button" onClick={onToggleShow} className="absolute right-3 top-[38px] p-1 text-surface-400 hover:text-surface-600 cursor-pointer" tabIndex={-1}>
        {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
      </button>
    </div>
  );
}

export default function ChangePasswordPage() {
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [showPasswords, setShowPasswords] = useState({});
  const [loading, setLoading] = useState(false);
  const { changePassword } = useAuth();
  const { requiresPasswordChange, accessToken, isAuthenticated } = useAuthStore();

  if (!requiresPasswordChange && !accessToken) {
    if (isAuthenticated) return <Navigate to={ROUTES.DASHBOARD} replace />;
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  const updateField = (field, value) => setForm((f) => ({ ...f, [field]: value }));
  const toggleShow = (field) => setShowPasswords((s) => ({ ...s, [field]: !s[field] }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.newPassword.length < 8) { toast.warning('Password must be at least 8 characters'); return; }
    if (form.newPassword !== form.confirmPassword) { toast.warning('Passwords do not match'); return; }
    setLoading(true);
    try { await changePassword(form.currentPassword, form.newPassword, form.confirmPassword); }
    catch (error) { toast.error(getErrorMessage(error)); }
    finally { setLoading(false); }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-surface-50">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-2xl shadow-card p-8 border border-surface-200/60">
          <div className="flex flex-col items-center mb-8">
            <div className="w-16 h-16 rounded-2xl bg-amber-100 flex items-center justify-center mb-4">
              <KeyRound className="w-8 h-8 text-amber-600" />
            </div>
            <h2 className="text-2xl font-bold text-surface-900">Change Password</h2>
            <p className="text-sm text-surface-500 mt-2 text-center">
              {requiresPasswordChange ? 'You must change your temporary password to continue.' : 'Update your account password.'}
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <PasswordField
              label="Current Password"
              field="currentPassword"
              value={form.currentPassword}
              showPassword={showPasswords.currentPassword}
              onChange={(e) => updateField('currentPassword', e.target.value)}
              onToggleShow={() => toggleShow('currentPassword')}
            />
            <PasswordField
              label="New Password"
              field="newPassword"
              value={form.newPassword}
              showPassword={showPasswords.newPassword}
              onChange={(e) => updateField('newPassword', e.target.value)}
              onToggleShow={() => toggleShow('newPassword')}
            />
            <PasswordField
              label="Confirm New Password"
              field="confirmPassword"
              value={form.confirmPassword}
              showPassword={showPasswords.confirmPassword}
              onChange={(e) => updateField('confirmPassword', e.target.value)}
              onToggleShow={() => toggleShow('confirmPassword')}
            />
            <Button type="submit" loading={loading} className="w-full" size="lg">
              Update Password <ArrowRight className="w-4 h-4" />
            </Button>
          </form>
        </div>
      </div>
      <ToastContainer />
    </div>
  );
}
