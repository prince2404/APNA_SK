import { useAuthStore } from '@/store/useAuthStore';
import { ROLE_DISPLAY_NAMES } from '@/constants/roles';
import { Users, Store, MapPin, TrendingUp, Package, Heart } from 'lucide-react';

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user);

  const stats = [
    { label: 'Total Stores', value: '200+', icon: Store, color: 'from-primary-500 to-primary-700' },
    { label: 'Active States', value: '3', icon: MapPin, color: 'from-accent-500 to-accent-700' },
    { label: 'Total Users', value: '—', icon: Users, color: 'from-amber-500 to-amber-700' },
    { label: 'Products', value: '—', icon: Package, color: 'from-rose-500 to-rose-700' },
  ];

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Welcome Card */}
      <div className="bg-gradient-to-r from-primary-600 via-primary-700 to-primary-800 rounded-2xl p-6 lg:p-8 text-white relative overflow-hidden">
        <div className="absolute right-0 top-0 w-64 h-64 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/2" />
        <div className="absolute right-20 bottom-0 w-40 h-40 bg-white/5 rounded-full translate-y-1/2" />
        <div className="relative z-10">
          <div className="flex items-center gap-3 mb-3">
            <Heart className="w-6 h-6 text-primary-200" />
            <span className="text-sm font-medium text-primary-200">{ROLE_DISPLAY_NAMES[user?.roleName] || 'User'}</span>
          </div>
          <h1 className="text-2xl lg:text-3xl font-bold mb-1">
            Welcome back, {user?.fullName?.split(' ')[0] || 'User'}!
          </h1>
          <p className="text-primary-100 text-sm lg:text-base">
            Here's an overview of your Apna Swasthya Kendra platform.
          </p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat) => (
          <div key={stat.label} className="bg-white rounded-xl border border-surface-200/60 p-5 shadow-card hover:shadow-card-hover transition-shadow group">
            <div className="flex items-center justify-between mb-3">
              <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${stat.color} flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform`}>
                <stat.icon className="w-5 h-5 text-white" />
              </div>
              <TrendingUp className="w-4 h-4 text-emerald-500" />
            </div>
            <p className="text-2xl font-bold text-surface-900">{stat.value}</p>
            <p className="text-sm text-surface-500 mt-0.5">{stat.label}</p>
          </div>
        ))}
      </div>

      {/* Placeholder sections */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="bg-white rounded-xl border border-surface-200/60 p-6 shadow-card">
          <h3 className="text-lg font-semibold text-surface-900 mb-4">Recent Activity</h3>
          <p className="text-sm text-surface-400 text-center py-8">Activity data will appear here once the system is operational.</p>
        </div>
        <div className="bg-white rounded-xl border border-surface-200/60 p-6 shadow-card">
          <h3 className="text-lg font-semibold text-surface-900 mb-4">Quick Actions</h3>
          <p className="text-sm text-surface-400 text-center py-8">Quick action shortcuts will be available in the next update.</p>
        </div>
      </div>
    </div>
  );
}
