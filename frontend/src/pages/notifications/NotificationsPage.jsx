import { Bell, CheckCircle2, AlertTriangle, ShieldCheck, Package, Info } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';

const notificationTypes = [
  { icon: ShieldCheck, title: 'Permission Requests', desc: 'Notifications when users request new permissions, and updates on approval or rejection status.', color: 'bg-primary-50 text-primary-600' },
  { icon: Package, title: 'Stock Alerts', desc: 'Low stock alerts, expiry warnings, and stock request notifications for timely inventory action.', color: 'bg-amber-50 text-amber-600' },
  { icon: AlertTriangle, title: 'System Alerts', desc: 'Important system-level notifications including security events and configuration changes.', color: 'bg-rose-50 text-rose-600' },
  { icon: Info, title: 'Activity Updates', desc: 'Updates on user verifications, commission calculations, and other operational activities.', color: 'bg-accent-50 text-accent-600' },
];

export default function NotificationsPage() {
  return (
    <div className="animate-fade-in">
      <PageHeader title="Notifications" description="Stay updated with system activity" />

      {/* Coming Soon Hero */}
      <div className="bg-white rounded-2xl border border-surface-200/60 shadow-card overflow-hidden mb-6">
        <div className="bg-gradient-to-br from-blue-500 via-blue-600 to-indigo-600 px-8 py-12 relative overflow-hidden">
          <div className="absolute right-0 top-0 w-72 h-72 bg-white/5 rounded-full -translate-y-1/3 translate-x-1/3" />
          <div className="absolute left-10 bottom-0 w-48 h-48 bg-white/5 rounded-full translate-y-1/2" />
          <div className="relative z-10 text-center max-w-lg mx-auto">
            <div className="w-20 h-20 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center mx-auto mb-5 shadow-lg">
              <Bell className="w-10 h-10 text-white" />
            </div>
            <h2 className="text-3xl font-bold text-white mb-3">Notification Center</h2>
            <p className="text-blue-100 text-sm leading-relaxed">
              Centralized notification hub with real-time alerts for stock, permissions, verifications, and system events.
              Full notification center is coming in <span className="font-semibold text-white">Phase 6</span>.
            </p>
          </div>
        </div>
      </div>

      {/* Preview */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {notificationTypes.map((n) => (
          <div key={n.title} className="bg-white rounded-xl border border-surface-200/60 p-5 shadow-card hover:shadow-card-hover transition-all group">
            <div className="flex items-start gap-4">
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${n.color} group-hover:scale-110 transition-transform`}>
                <n.icon className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-semibold text-surface-900 mb-1">{n.title}</h3>
                <p className="text-xs text-surface-500 leading-relaxed">{n.desc}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Empty State */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card p-8 mt-6 text-center">
        <CheckCircle2 className="w-12 h-12 text-surface-300 mx-auto mb-3" />
        <h3 className="text-sm font-semibold text-surface-700 mb-1">No notifications yet</h3>
        <p className="text-xs text-surface-400">You're all caught up! Notifications will appear here as the system becomes operational.</p>
      </div>
    </div>
  );
}
