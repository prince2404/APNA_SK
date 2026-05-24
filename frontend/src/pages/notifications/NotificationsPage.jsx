import { useState, useEffect } from 'react';
import { PageHeader } from '@/components/common/PageHeader';
import { notificationApi } from '@/api/notificationApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { Loader } from '@/components/common/Loader';
import { Button } from '@/components/common/Button';
import { Pagination } from '@/components/common/Pagination';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@/constants/routePaths';
import { Bell, CheckCircle2, User, Key, Package, AlertTriangle, ShieldCheck, Mail } from 'lucide-react';

export default function NotificationsPage() {
  const navigate = useNavigate();

  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [size] = useState(15);
  const [filterType, setFilterType] = useState('ALL');

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const res = await notificationApi.getNotifications({ page, size });
      setNotifications(res.data.data?.content || []);
      setTotalElements(res.data.data?.totalElements || 0);
    } catch (err) {
      toast.error('Failed to load notifications: ' + getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const handleMarkRead = async (id, isRead) => {
    if (isRead) return;
    try {
      await notificationApi.markAsRead(id);
      fetchNotifications();
    } catch (err) {
      toast.error('Failed to mark read: ' + getErrorMessage(err));
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      toast.success('All notifications marked as read');
      fetchNotifications();
    } catch (err) {
      toast.error('Failed to mark all as read: ' + getErrorMessage(err));
    }
  };

  const handleNotificationRedirect = (n) => {
    // Mark as read first if unread
    handleMarkRead(n.id, n.isRead);

    // Redirect
    if (n.type === 'KYC_SUBMISSION' || n.type === 'KYC_REVIEW') {
      navigate(ROUTES.VERIFICATION_QUEUE);
    } else if (n.type === 'PERMISSION_REQUEST' || n.type === 'PERMISSION_REVIEW') {
      navigate(ROUTES.PERMISSION_REQUESTS);
    } else if (n.type?.includes('STOCK') || n.type?.includes('TRANSFER')) {
      navigate(ROUTES.INVENTORY);
    } else if (n.type === 'EXPIRY_ALERT') {
      navigate(ROUTES.REPORTS);
    } else if (n.type === 'COMMISSION_EARNED') {
      navigate(ROUTES.COMMISSIONS);
    }
  };

  const getNotificationIcon = (type) => {
    if (type?.includes('KYC')) return User;
    if (type?.includes('PERMISSION')) return Key;
    if (type?.includes('STOCK')) return Package;
    if (type === 'EXPIRY_ALERT') return AlertTriangle;
    if (type === 'COMMISSION_EARNED') return ShieldCheck;
    return Bell;
  };

  const getIconColor = (type) => {
    if (type?.includes('KYC')) return 'bg-emerald-50 text-emerald-600 border-emerald-100';
    if (type?.includes('PERMISSION')) return 'bg-amber-50 text-amber-600 border-amber-100';
    if (type?.includes('STOCK')) return 'bg-blue-50 text-blue-600 border-blue-100';
    if (type === 'EXPIRY_ALERT') return 'bg-rose-50 text-rose-600 border-rose-100';
    if (type === 'COMMISSION_EARNED') return 'bg-indigo-50 text-indigo-600 border-indigo-100';
    return 'bg-slate-50 text-slate-600 border-slate-100';
  };

  const filteredNotifications = filterType === 'ALL'
    ? notifications
    : notifications.filter(n => n.type === filterType);

  return (
    <div className="space-y-6 animate-fade-in text-slate-800">
      <PageHeader title="Notification Center" description="Inbox of all scoped activity alerts, alerts, and approvals.">
        <Button variant="secondary" onClick={handleMarkAllRead} className="flex items-center gap-1.5 cursor-pointer">
          <CheckCircle2 className="w-4 h-4" /> Mark All as Read
        </Button>
      </PageHeader>

      {/* Filter Chips */}
      <div className="flex flex-wrap gap-2">
        {[
          { key: 'ALL', label: 'All Alerts' },
          { key: 'KYC_SUBMISSION', label: 'KYC Verification' },
          { key: 'PERMISSION_REQUEST', label: 'Permissions' },
          { key: 'STOCK_ALERT', label: 'Stock Alerts' },
          { key: 'EXPIRY_ALERT', label: 'Expiry warnings' },
          { key: 'COMMISSION_EARNED', label: 'Commissions' }
        ].map((chip) => (
          <button
            key={chip.key}
            onClick={() => setFilterType(chip.key)}
            className={`px-3 py-1.5 rounded-full text-xs font-semibold border transition-all cursor-pointer ${
              filterType === chip.key
                ? 'bg-primary-600 border-primary-600 text-white shadow-sm'
                : 'bg-white border-surface-200 text-slate-600 hover:bg-slate-50'
            }`}
          >
            {chip.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <Loader className="w-8 h-8 text-primary-600 animate-spin" />
        </div>
      ) : filteredNotifications.length === 0 ? (
        <div className="bg-white p-12 text-center border border-surface-200 rounded-xl shadow-sm">
          <Bell className="w-12 h-12 text-surface-300 mx-auto mb-3 animate-pulse" />
          <p className="text-surface-500 font-medium">Your notification inbox is clean!</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-surface-200 shadow-sm overflow-hidden divide-y divide-surface-100">
          {filteredNotifications.map((n) => {
            const Icon = getNotificationIcon(n.type);
            return (
              <div
                key={n.id}
                onClick={() => handleNotificationRedirect(n)}
                className={`p-4 flex gap-4 transition-all cursor-pointer ${
                  n.isRead ? 'hover:bg-slate-50/50' : 'bg-primary-50/15 hover:bg-primary-50/25 border-l-4 border-l-primary-600'
                }`}
              >
                <div className={`w-9 h-9 rounded-xl border flex items-center justify-center shrink-0 ${getIconColor(n.type)}`}>
                  <Icon className="w-4 h-4" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between items-start gap-2">
                    <h4 className="font-bold text-surface-900 text-sm">{n.title}</h4>
                    <span className="text-[10px] text-slate-400 font-medium whitespace-nowrap">
                      {new Date(n.createdAt).toLocaleString('en-IN', {
                        day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
                      })}
                    </span>
                  </div>
                  <p className="text-xs text-slate-500 mt-1 leading-relaxed">{n.message}</p>
                </div>
              </div>
            );
          })}
          <div className="p-4 bg-slate-50/30">
            <Pagination
              currentPage={page}
              totalItems={totalElements}
              itemsPerPage={size}
              onPageChange={setPage}
            />
          </div>
        </div>
      )}
    </div>
  );
}
