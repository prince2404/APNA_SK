import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/useAuthStore';
import { ROLE_DISPLAY_NAMES, ROLES } from '@/constants/roles';
import { ROUTES } from '@/constants/routePaths';
import { dashboardApi } from '@/api/dashboardApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { Loader } from '@/components/common/Loader';
import {
  TrendingUp, Users, ShoppingBag, ShoppingCart, AlertCircle,
  Building, MapPin, Package, Clock, BarChart3, ArrowUpRight
} from 'lucide-react';

export default function DashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  const [loading, setLoading] = useState(true);
  const [data, setData] = useState({
    metrics: [],
    trendData: [],
    breakdownData: [],
    topProducts: [],
    recentActivity: [],
  });

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const res = await dashboardApi.getDashboardData();
      setData(res.data.data || {
        metrics: [],
        trendData: [],
        breakdownData: [],
        topProducts: [],
        recentActivity: [],
      });
    } catch (err) {
      toast.error('Failed to load dashboard metrics: ' + getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getMetricIcon = (label) => {
    const labelLower = label.toLowerCase();
    if (labelLower.includes('sales') || labelLower.includes('revenue')) return ShoppingBag;
    if (labelLower.includes('stores') || labelLower.includes('centres')) return Building;
    if (labelLower.includes('patients')) return Users;
    if (labelLower.includes('stock') || labelLower.includes('product')) return Package;
    if (labelLower.includes('warning') || labelLower.includes('alert')) return AlertCircle;
    return ShoppingCart;
  };

  const getMetricStyle = (type) => {
    if (type === 'ALERT') return 'from-rose-500 to-red-600 text-white';
    if (type === 'CURRENCY') return 'from-emerald-500 to-teal-600 text-white';
    return 'from-slate-800 to-slate-900 text-white';
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-96">
        <Loader className="w-10 h-10 text-primary-600" />
      </div>
    );
  }

  // Calculate SVG dimensions for trend chart
  const maxTrendVal = Math.max(...data.trendData.map(d => d.value), 100);
  const trendPoints = data.trendData.map((d, i) => {
    const x = 50 + i * (350 / Math.max(data.trendData.length - 1, 1));
    const y = 160 - (d.value / maxTrendVal) * 120;
    return `${x},${y}`;
  }).join(' ');

  // Calculate breakdown percentages
  const totalBreakdown = data.breakdownData.reduce((sum, d) => sum + d.value, 0);

  return (
    <div className="space-y-6 animate-fade-in text-slate-800">
      {/* Welcome banner */}
      <div className="bg-gradient-to-r from-primary-600 via-primary-700 to-primary-800 rounded-2xl p-6 text-white relative overflow-hidden shadow-md">
        <div className="absolute right-0 top-0 w-64 h-64 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/2" />
        <div className="absolute right-20 bottom-0 w-40 h-40 bg-white/5 rounded-full translate-y-1/2" />
        <div className="relative z-10 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <span className="px-2.5 py-0.5 text-xs font-semibold rounded-full bg-white/20 text-primary-100 uppercase tracking-wider">
              {ROLE_DISPLAY_NAMES[user?.roleName] || 'Coordinator'}
            </span>
            <h1 className="text-2xl lg:text-3xl font-extrabold mt-2">
              Welcome back, {user?.fullName || 'User'}!
            </h1>
            <p className="text-primary-100 text-sm mt-1">
              Here is your scoped business summary & metrics console.
            </p>
          </div>
          <button
            onClick={() => navigate(ROUTES.REPORTS)}
            className="flex items-center gap-1 bg-white/10 hover:bg-white/20 px-4 py-2 rounded-xl text-sm font-semibold transition-all cursor-pointer border border-white/10"
          >
            Analytics Center <ArrowUpRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Metrics Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {data.metrics.map((m, idx) => {
          const Icon = getMetricIcon(m.label);
          return (
            <div key={idx} className={`bg-gradient-to-br ${getMetricStyle(m.type)} rounded-xl p-5 shadow-sm space-y-4`}>
              <div className="flex justify-between items-start">
                <p className="text-xs font-semibold opacity-80 uppercase tracking-wider">{m.label}</p>
                <div className="w-8 h-8 rounded-lg bg-white/15 flex items-center justify-center">
                  <Icon className="w-4 h-4 text-white" />
                </div>
              </div>
              <div>
                <p className="text-2xl font-black">{m.value}</p>
                <p className="text-xs opacity-75 mt-1">{m.change}</p>
              </div>
            </div>
          );
        })}
      </div>

      {/* Charts & Trends Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Trend Area Chart (custom SVG) */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-surface-200 shadow-sm p-5 space-y-4 flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-bold text-surface-900 text-md">Performance Trend</h3>
              <p className="text-xs text-surface-400">Monthly invoice aggregation values</p>
            </div>
            <TrendingUp className="w-5 h-5 text-primary-600" />
          </div>

          {data.trendData.length === 0 ? (
            <div className="h-48 flex items-center justify-center text-xs text-surface-400 font-medium">
              No trend records available for this level.
            </div>
          ) : (
            <div className="relative h-48 w-full mt-4 flex items-end">
              <svg className="w-full h-full" viewBox="0 0 450 180" preserveAspectRatio="none">
                <defs>
                  <linearGradient id="trendGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#4f46e5" stopOpacity="0.35" />
                    <stop offset="100%" stopColor="#4f46e5" stopOpacity="0" />
                  </linearGradient>
                </defs>
                {/* Horizontal grid lines */}
                <line x1="40" y1="40" x2="420" y2="40" stroke="#f1f5f9" strokeWidth="1" />
                <line x1="40" y1="100" x2="420" y2="100" stroke="#f1f5f9" strokeWidth="1" />
                <line x1="40" y1="160" x2="420" y2="160" stroke="#e2e8f0" strokeWidth="1.5" />

                {/* Fill Area under chart */}
                {data.trendData.length > 1 && (
                  <path
                    d={`M50,160 L${trendPoints} L400,160 Z`}
                    fill="url(#trendGrad)"
                  />
                )}

                {/* Line path */}
                {data.trendData.length > 1 && (
                  <polyline
                    fill="none"
                    stroke="#4f46e5"
                    strokeWidth="3"
                    points={trendPoints}
                  />
                )}

                {/* Nodes */}
                {data.trendData.map((d, i) => {
                  const x = 50 + i * (350 / Math.max(data.trendData.length - 1, 1));
                  const y = 160 - (d.value / maxTrendVal) * 120;
                  return (
                    <circle
                      key={i}
                      cx={x}
                      cy={y}
                      r="4.5"
                      fill="#ffffff"
                      stroke="#4f46e5"
                      strokeWidth="2.5"
                    />
                  );
                })}
              </svg>

              {/* Month Labels */}
              <div className="absolute bottom-0 left-0 right-0 flex justify-between px-10 text-[10px] font-bold text-surface-400">
                {data.trendData.map((d, i) => (
                  <span key={i}>{d.name}</span>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Breakdown Card */}
        <div className="bg-white rounded-xl border border-surface-200 shadow-sm p-5 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-bold text-surface-900 text-md">Regional / Share Share</h3>
              <p className="text-xs text-surface-400">Distribution percentages</p>
            </div>
            <BarChart3 className="w-5 h-5 text-indigo-600" />
          </div>

          {data.breakdownData.length === 0 ? (
            <div className="h-48 flex items-center justify-center text-xs text-surface-400 font-medium">
              No breakdown records scoped.
            </div>
          ) : (
            <div className="space-y-4 pt-2">
              {data.breakdownData.map((d, idx) => {
                const pct = totalBreakdown > 0 ? (d.value / totalBreakdown) * 100 : 0;
                return (
                  <div key={idx} className="space-y-1.5">
                    <div className="flex justify-between text-xs font-semibold text-surface-700">
                      <span>{d.name}</span>
                      <span>{pct.toFixed(1)}% (₹{d.value.toLocaleString()})</span>
                    </div>
                    <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-indigo-600 rounded-full transition-all"
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Top Products & Recent Actions */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Top Selling Products */}
        <div className="lg:col-span-1 bg-white rounded-xl border border-surface-200 shadow-sm p-5 space-y-4">
          <h3 className="font-bold text-surface-900 text-md border-b pb-2">Top Selling Products</h3>
          {data.topProducts.length === 0 ? (
            <p className="text-xs text-surface-400 py-6 text-center">No sales registered yet.</p>
          ) : (
            <div className="divide-y divide-slate-100">
              {data.topProducts.map((p, idx) => (
                <div key={idx} className="flex justify-between items-center py-3">
                  <div>
                    <p className="text-sm font-semibold text-slate-800">{p.productName}</p>
                    <p className="text-xs text-slate-400">{p.quantity} units sold</p>
                  </div>
                  <span className="text-sm font-black text-emerald-600">₹{p.totalRevenue.toFixed(2)}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recent Invoices / Actions */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-surface-200 shadow-sm p-5 space-y-4">
          <h3 className="font-bold text-surface-900 text-md border-b pb-2 flex items-center gap-2">
            <Clock className="w-4 h-4 text-surface-400" /> Scoped Store Invoice Trail
          </h3>
          {data.recentActivity.length === 0 ? (
            <p className="text-xs text-surface-400 py-6 text-center">No activities recorded.</p>
          ) : (
            <div className="space-y-4">
              {data.recentActivity.map((a, idx) => (
                <div key={idx} className="flex items-start gap-3">
                  <div className="w-7 h-7 rounded-lg bg-indigo-50 border border-indigo-100 flex items-center justify-center shrink-0 mt-0.5">
                    <ShoppingCart className="w-3.5 h-3.5 text-indigo-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-slate-700 leading-relaxed font-semibold">{a.description}</p>
                    <p className="text-xs text-slate-400 mt-0.5">
                      {new Date(a.timestamp).toLocaleString('en-IN', {
                        day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
                      })}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
