import { useState, useEffect, useCallback } from 'react';
import { Monitor, Smartphone, Globe, Trash2, ShieldCheck } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { Loader } from '@/components/common/Loader';
import { EmptyState } from '@/components/common/EmptyState';
import { sessionApi } from '@/api/sessionApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';

export default function SessionsPage() {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [revokeConfirm, setRevokeConfirm] = useState(null);
  const [revoking, setRevoking] = useState(false);

  const fetchSessions = useCallback(async () => {
    setLoading(true);
    try {
      const res = await sessionApi.getSessions();
      const data = res.data.data;
      setSessions(data?.content || data || []);
    } catch (error) { toast.error(getErrorMessage(error)); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { fetchSessions(); }, [fetchSessions]);

  const handleRevoke = async () => {
    if (!revokeConfirm) return;
    setRevoking(true);
    try {
      await sessionApi.revokeSession(revokeConfirm.id);
      toast.success('Session revoked');
      setRevokeConfirm(null);
      fetchSessions();
    } catch (error) { toast.error(getErrorMessage(error)); }
    finally { setRevoking(false); }
  };

  const getDeviceIcon = (deviceInfo) => {
    if (!deviceInfo) return Globe;
    const d = deviceInfo.toLowerCase();
    if (d.includes('mobile') || d.includes('android') || d.includes('iphone')) return Smartphone;
    return Monitor;
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
  };

  return (
    <div className="animate-fade-in max-w-3xl mx-auto">
      <PageHeader title="Active Sessions" description="View and manage your active login sessions" />

      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden">
        {loading ? <Loader text="Loading sessions..." /> : sessions.length === 0 ? (
          <EmptyState icon={ShieldCheck} title="No active sessions" description="No sessions found." />
        ) : (
          <div className="divide-y divide-surface-100">
            {sessions.map((session) => {
              const DeviceIcon = getDeviceIcon(session.deviceInfo);
              return (
                <div key={session.id} className="p-4 flex items-center gap-4 hover:bg-surface-50/50 transition-colors">
                  <div className="w-10 h-10 rounded-xl bg-surface-100 flex items-center justify-center shrink-0">
                    <DeviceIcon className="w-5 h-5 text-surface-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-surface-900 truncate">{session.deviceInfo || 'Unknown device'}</p>
                    <div className="flex items-center gap-3 mt-0.5">
                      <span className="text-xs text-surface-500">{session.ipAddress || '—'}</span>
                      <span className="text-xs text-surface-400">•</span>
                      <span className="text-xs text-surface-500">Last active: {formatDate(session.lastActiveAt)}</span>
                    </div>
                    {session.isCurrent && (
                      <span className="inline-flex items-center px-2 py-0.5 text-xs font-medium bg-emerald-100 text-emerald-700 rounded-full mt-1">Current session</span>
                    )}
                  </div>
                  {!session.isCurrent && (
                    <button
                      onClick={() => setRevokeConfirm(session)}
                      className="p-2 rounded-lg text-surface-400 hover:text-danger-600 hover:bg-danger-50 transition-colors cursor-pointer shrink-0"
                      title="Revoke session"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      <ConfirmDialog isOpen={!!revokeConfirm} onClose={() => setRevokeConfirm(null)} onConfirm={handleRevoke}
        title="Revoke Session" message="Are you sure you want to revoke this session? The device will be logged out."
        confirmText="Revoke" loading={revoking} variant="danger" />
    </div>
  );
}
