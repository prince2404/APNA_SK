import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/useAuthStore';
import { ROLE_DISPLAY_NAMES, ROLES } from '@/constants/roles';
import { ROUTES } from '@/constants/routePaths';
import { usePermission } from '@/hooks/usePermission';
import axiosInstance from '@/api/axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';
import {
  Users, Store, MapPin, TrendingUp, Package, Heart,
  UserPlus, ArrowRight, Building2, Layers,
  ShieldCheck, BarChart3, Clock, Activity,
} from 'lucide-react';

export default function DashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const { isSuperAdmin, hasPlatformScope, hasPermission } = usePermission();
  // Capture permission values in a ref so the effect doesn't re-trigger on every render
  const permRef = useRef({ hasPlatformScope, hasPermission });
  permRef.current = { hasPlatformScope, hasPermission };

  const [stats, setStats] = useState({
    states: '—',
    districts: '—',
    blocks: '—',
    stores: '—',
    users: '—',
  });
  const [loadingStats, setLoadingStats] = useState(true);

  useEffect(() => {
    const controller = new AbortController();
    const signal = controller.signal;

    const fetchStats = async () => {
      const results = { states: '—', districts: '—', blocks: '—', stores: '—', users: '—' };

      // Fetch geography stats sequentially to reduce backend load
      try {
        const statesRes = await axiosInstance.get(API_PATHS.STATES, { params: { page: 0, size: 1 }, signal });
        results.states = statesRes.data.data?.totalElements ?? '—';
      } catch { /* ignore */ }

      if (signal.aborted) return;

      try {
        const districtsRes = await axiosInstance.get(API_PATHS.DISTRICTS, { params: { page: 0, size: 1 }, signal });
        results.districts = districtsRes.data.data?.totalElements ?? '—';
      } catch { /* ignore */ }

      if (signal.aborted) return;

      try {
        const storesRes = await axiosInstance.get(API_PATHS.STORES, { params: { page: 0, size: 1 }, signal });
        results.stores = storesRes.data.data?.totalElements ?? '—';
      } catch { /* ignore */ }

      if (signal.aborted) return;

      const { hasPlatformScope: pScope, hasPermission: hasPerm } = permRef.current;
      if (pScope || hasPerm('USERS:VIEW')) {
        try {
          const usersRes = await axiosInstance.get(API_PATHS.USERS, { params: { page: 0, size: 1 }, signal });
          results.users = usersRes.data.data?.totalElements ?? '—';
        } catch { /* ignore */ }
      }

      if (!signal.aborted) {
        setStats(results);
        setLoadingStats(false);
      }
    };

    fetchStats();

    return () => controller.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const statCards = [
    { label: 'Active States', value: stats.states, icon: MapPin, color: 'from-accent-500 to-accent-700', path: ROUTES.STATES },
    { label: 'Districts', value: stats.districts, icon: Building2, color: 'from-blue-500 to-blue-700', path: ROUTES.DISTRICTS },
    { label: 'Stores', value: stats.stores, icon: Store, color: 'from-primary-500 to-primary-700', path: ROUTES.STORES },
    { label: 'System Users', value: stats.users, icon: Users, color: 'from-amber-500 to-amber-700', path: ROUTES.USERS },
  ];

  const quickActions = [
    { label: 'Add User', icon: UserPlus, path: ROUTES.USER_CREATE, color: 'text-primary-600 bg-primary-50 hover:bg-primary-100', show: isSuperAdmin || hasPermission('USERS:CREATE') },
    { label: 'Add State', icon: MapPin, path: ROUTES.STATES, color: 'text-accent-600 bg-accent-50 hover:bg-accent-100', show: isSuperAdmin },
    { label: 'Add Store', icon: Store, path: ROUTES.STORES, color: 'text-cyan-600 bg-cyan-50 hover:bg-cyan-100', show: isSuperAdmin || hasPermission('GEOGRAPHY:CREATE') },
    { label: 'View Reports', icon: BarChart3, path: ROUTES.REPORTS, color: 'text-violet-600 bg-violet-50 hover:bg-violet-100', show: true },
    { label: 'Manage Sessions', icon: ShieldCheck, path: ROUTES.SESSIONS, color: 'text-emerald-600 bg-emerald-50 hover:bg-emerald-100', show: true },
    { label: 'System Settings', icon: Layers, path: ROUTES.SETTINGS, color: 'text-rose-600 bg-rose-50 hover:bg-rose-100', show: isSuperAdmin },
  ].filter(a => a.show);

  const recentModules = [
    { title: 'Geography Management', desc: 'States, Districts, Blocks & Stores are fully operational.', icon: MapPin, status: 'Live', statusColor: 'bg-emerald-100 text-emerald-700' },
    { title: 'User Management', desc: 'User CRUD, role assignment & permissions are ready.', icon: Users, status: 'Live', statusColor: 'bg-emerald-100 text-emerald-700' },
    { title: 'Authentication & 2FA', desc: 'Login, OTP verification, password management active.', icon: ShieldCheck, status: 'Live', statusColor: 'bg-emerald-100 text-emerald-700' },
    { title: 'Product Catalogue', desc: 'Product management and categorization.', icon: Package, status: 'Phase 3', statusColor: 'bg-amber-100 text-amber-700' },
    { title: 'Billing & POS', desc: 'Point of Sale billing system.', icon: TrendingUp, status: 'Phase 4', statusColor: 'bg-blue-100 text-blue-700' },
    { title: 'Reports & Analytics', desc: '9 report types with export capabilities.', icon: BarChart3, status: 'Phase 5', statusColor: 'bg-violet-100 text-violet-700' },
  ];

  const formatTime = () => {
    const now = new Date();
    return now.toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Welcome Card */}
      <div className="bg-gradient-to-r from-primary-600 via-primary-700 to-primary-800 rounded-2xl p-6 lg:p-8 text-white relative overflow-hidden">
        <div className="absolute right-0 top-0 w-64 h-64 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/2" />
        <div className="absolute right-20 bottom-0 w-40 h-40 bg-white/5 rounded-full translate-y-1/2" />
        <div className="relative z-10">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
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
            <div className="flex items-center gap-2 text-primary-200 text-sm">
              <Clock className="w-4 h-4" />
              <span>{formatTime()}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {statCards.map((stat) => (
          <button
            key={stat.label}
            onClick={() => navigate(stat.path)}
            className="bg-white rounded-xl border border-surface-200/60 p-5 shadow-card hover:shadow-card-hover transition-all group text-left cursor-pointer"
          >
            <div className="flex items-center justify-between mb-3">
              <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${stat.color} flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform`}>
                <stat.icon className="w-5 h-5 text-white" />
              </div>
              <ArrowRight className="w-4 h-4 text-surface-300 group-hover:text-primary-500 group-hover:translate-x-0.5 transition-all" />
            </div>
            {loadingStats ? (
              <div className="space-y-2">
                <div className="h-7 w-16 bg-surface-200 rounded-md animate-pulse" />
                <div className="h-4 w-24 bg-surface-100 rounded-md animate-pulse" />
              </div>
            ) : (
              <>
                <p className="text-2xl font-bold text-surface-900">{stat.value}</p>
                <p className="text-sm text-surface-500 mt-0.5">{stat.label}</p>
              </>
            )}
          </button>
        ))}
      </div>

      {/* Quick Actions + Module Status */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Quick Actions */}
        <div className="bg-white rounded-xl border border-surface-200/60 p-6 shadow-card">
          <div className="flex items-center gap-2 mb-5">
            <Activity className="w-5 h-5 text-primary-600" />
            <h3 className="text-lg font-semibold text-surface-900">Quick Actions</h3>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
            {quickActions.map((action) => (
              <button
                key={action.label}
                onClick={() => navigate(action.path)}
                className={`flex flex-col items-center gap-2 p-4 rounded-xl transition-all cursor-pointer ${action.color}`}
              >
                <action.icon className="w-5 h-5" />
                <span className="text-xs font-medium">{action.label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Module Status */}
        <div className="bg-white rounded-xl border border-surface-200/60 p-6 shadow-card">
          <div className="flex items-center gap-2 mb-5">
            <Layers className="w-5 h-5 text-accent-600" />
            <h3 className="text-lg font-semibold text-surface-900">Module Status</h3>
          </div>
          <div className="space-y-3">
            {recentModules.map((mod) => (
              <div key={mod.title} className="flex items-center gap-3 p-2.5 rounded-lg hover:bg-surface-50 transition-colors">
                <div className="w-8 h-8 rounded-lg bg-surface-100 flex items-center justify-center shrink-0">
                  <mod.icon className="w-4 h-4 text-surface-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-surface-900 truncate">{mod.title}</p>
                  <p className="text-xs text-surface-500 truncate">{mod.desc}</p>
                </div>
                <span className={`px-2 py-0.5 text-[10px] font-semibold rounded-full shrink-0 ${mod.statusColor}`}>
                  {mod.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Geography Overview (for admins) */}
      {hasPlatformScope && (
        <div className="bg-white rounded-xl border border-surface-200/60 p-6 shadow-card">
          <div className="flex items-center justify-between mb-5">
            <div className="flex items-center gap-2">
              <MapPin className="w-5 h-5 text-primary-600" />
              <h3 className="text-lg font-semibold text-surface-900">Geographic Coverage</h3>
            </div>
            <button
              onClick={() => navigate(ROUTES.STATES)}
              className="text-sm text-primary-600 hover:text-primary-700 font-medium flex items-center gap-1 cursor-pointer"
            >
              View All <ArrowRight className="w-3 h-3" />
            </button>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
            {[
              { label: 'States', value: stats.states, icon: MapPin, color: 'bg-accent-50 text-accent-600' },
              { label: 'Districts', value: stats.districts, icon: Building2, color: 'bg-blue-50 text-blue-600' },
              { label: 'Stores', value: stats.stores, icon: Store, color: 'bg-primary-50 text-primary-600' },
            ].map((geo) => (
              <div key={geo.label} className="text-center p-4 rounded-xl bg-surface-50/50 border border-surface-200/40">
                <div className={`w-10 h-10 rounded-xl ${geo.color} flex items-center justify-center mx-auto mb-2`}>
                  <geo.icon className="w-5 h-5" />
                </div>
                {loadingStats ? (
                  <div className="h-6 w-10 bg-surface-200 rounded-md animate-pulse mx-auto mb-1" />
                ) : (
                  <p className="text-xl font-bold text-surface-900">{geo.value}</p>
                )}
                <p className="text-xs text-surface-500">{geo.label}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
