import { useState } from 'react';
import { User, Mail, Phone, MapPin, Lock, Save, Edit3 } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { StatusBadge } from '@/components/common/StatusBadge';
import { Badge } from '@/components/common/Badge';
import { useAuthStore } from '@/store/useAuthStore';
import { useAuth } from '@/hooks/useAuth';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { ROLE_DISPLAY_NAMES } from '@/constants/roles';

export default function ProfilePage() {
  const user = useAuthStore((s) => s.user);
  const { changePassword } = useAuth();
  const [showPwForm, setShowPwForm] = useState(false);
  const [currentPw, setCurrentPw] = useState('');
  const [newPw, setNewPw] = useState('');
  const [confirmPw, setConfirmPw] = useState('');
  const [changingPw, setChangingPw] = useState(false);

  const getInitials = (name) => name?.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() || 'U';

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (!currentPw || !newPw || !confirmPw) { toast.warning('Please fill all password fields'); return; }
    if (newPw !== confirmPw) { toast.warning('Passwords do not match'); return; }
    if (newPw.length < 8) { toast.warning('Password must be at least 8 characters'); return; }
    setChangingPw(true);
    try {
      await changePassword(currentPw, newPw, confirmPw);
      setShowPwForm(false);
      setCurrentPw(''); setNewPw(''); setConfirmPw('');
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setChangingPw(false);
    }
  };

  const geoPath = [user?.stateName, user?.districtName, user?.blockName, user?.storeName].filter(Boolean).join(' → ');

  return (
    <div className="animate-fade-in max-w-3xl mx-auto">
      <PageHeader title="My Profile" description="View your account information" />

      {/* Profile Header Card */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden mb-6">
        <div className="bg-gradient-to-r from-primary-600 via-primary-700 to-primary-800 px-6 py-8 relative overflow-hidden">
          <div className="absolute right-0 top-0 w-48 h-48 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/2" />
          <div className="relative flex items-center gap-5">
            <div className="w-20 h-20 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center text-2xl font-bold text-white shadow-lg">
              {getInitials(user?.fullName)}
            </div>
            <div className="text-white">
              <h1 className="text-2xl font-bold">{user?.fullName || 'User'}</h1>
              <div className="flex items-center gap-3 mt-2">
                <Badge>{ROLE_DISPLAY_NAMES[user?.roleName] || user?.roleName}</Badge>
                <StatusBadge status={user?.status || 'ACTIVE'} />
              </div>
            </div>
          </div>
        </div>

        <div className="p-6 space-y-5">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center shrink-0">
                <Mail className="w-4 h-4 text-primary-600" />
              </div>
              <div>
                <p className="text-xs text-surface-500 mb-0.5">Email Address</p>
                <p className="text-sm font-medium text-surface-900">{user?.email || '—'}</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center shrink-0">
                <Phone className="w-4 h-4 text-primary-600" />
              </div>
              <div>
                <p className="text-xs text-surface-500 mb-0.5">Phone Number</p>
                <p className="text-sm font-medium text-surface-900">{user?.phone || '—'}</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center shrink-0">
                <User className="w-4 h-4 text-primary-600" />
              </div>
              <div>
                <p className="text-xs text-surface-500 mb-0.5">Gender</p>
                <p className="text-sm font-medium text-surface-900">{user?.gender || '—'}</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center shrink-0">
                <MapPin className="w-4 h-4 text-primary-600" />
              </div>
              <div>
                <p className="text-xs text-surface-500 mb-0.5">Geography</p>
                <p className="text-sm font-medium text-surface-900">{geoPath || 'Platform-wide access'}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Change Password */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-amber-50 flex items-center justify-center">
              <Lock className="w-4 h-4 text-amber-600" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-surface-900">Password & Security</h3>
              <p className="text-xs text-surface-500">Update your password to keep your account secure</p>
            </div>
          </div>
          {!showPwForm && (
            <Button variant="secondary" size="sm" onClick={() => setShowPwForm(true)}>
              <Edit3 className="w-3 h-3" /> Change Password
            </Button>
          )}
        </div>

        {showPwForm && (
          <form onSubmit={handleChangePassword} className="space-y-4 pt-2 border-t border-surface-100 mt-4">
            <div className="pt-4">
              <Input label="Current Password" type="password" placeholder="Enter current password" value={currentPw} onChange={(e) => setCurrentPw(e.target.value)} autoFocus />
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input label="New Password" type="password" placeholder="Min 8 characters" value={newPw} onChange={(e) => setNewPw(e.target.value)} />
              <Input label="Confirm New Password" type="password" placeholder="Re-enter new password" value={confirmPw} onChange={(e) => setConfirmPw(e.target.value)} />
            </div>
            <div className="flex gap-3 pt-2">
              <Button variant="secondary" type="button" onClick={() => { setShowPwForm(false); setCurrentPw(''); setNewPw(''); setConfirmPw(''); }}>Cancel</Button>
              <Button type="submit" loading={changingPw}><Save className="w-4 h-4" /> Update Password</Button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
