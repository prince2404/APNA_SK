import { BarChart3, FileText, TrendingUp, ShoppingCart, Users, Package, AlertTriangle, IndianRupee, Activity } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';

const reportTypes = [
  { icon: ShoppingCart, title: 'Sales Report', desc: 'Daily, weekly, monthly sales analytics with geography and product breakdowns.', color: 'from-primary-500 to-primary-700' },
  { icon: Package, title: 'Stock Report', desc: 'Current inventory levels across stores with batch and expiry tracking.', color: 'from-cyan-500 to-cyan-700' },
  { icon: IndianRupee, title: 'Commission Report', desc: 'Monthly commission summaries per user and role level with payout tracking.', color: 'from-emerald-500 to-emerald-700' },
  { icon: Users, title: 'Patient Report', desc: 'Patient demographics, registration trends, and health card statistics.', color: 'from-rose-500 to-rose-700' },
  { icon: FileText, title: 'Bill Report', desc: 'Detailed billing analytics with cancellations, returns, and payment mode analysis.', color: 'from-violet-500 to-violet-700' },
  { icon: AlertTriangle, title: 'Expiry Report', desc: 'Products expiring within 30, 60, or 90 days with automated flagging.', color: 'from-amber-500 to-amber-700' },
  { icon: Activity, title: 'User Activity Report', desc: 'Login history, session tracking, and audit trail analysis per user.', color: 'from-blue-500 to-blue-700' },
  { icon: TrendingUp, title: 'Revenue Report', desc: 'Revenue trends by geography, store, category with MRP vs ASK savings analysis.', color: 'from-indigo-500 to-indigo-700' },
  { icon: Package, title: 'Low Stock Report', desc: 'Items below minimum threshold across stores with urgency classification.', color: 'from-orange-500 to-orange-700' },
];

export default function ReportsPage() {
  return (
    <div className="animate-fade-in">
      <PageHeader title="Reports" description="Analytics and reporting dashboard" />

      {/* Coming Soon Hero */}
      <div className="bg-white rounded-2xl border border-surface-200/60 shadow-card overflow-hidden mb-6">
        <div className="bg-gradient-to-br from-indigo-500 via-indigo-600 to-purple-700 px-8 py-12 relative overflow-hidden">
          <div className="absolute right-0 top-0 w-72 h-72 bg-white/5 rounded-full -translate-y-1/3 translate-x-1/3" />
          <div className="absolute left-10 bottom-0 w-48 h-48 bg-white/5 rounded-full translate-y-1/2" />
          <div className="relative z-10 text-center max-w-lg mx-auto">
            <div className="w-20 h-20 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center mx-auto mb-5 shadow-lg">
              <BarChart3 className="w-10 h-10 text-white" />
            </div>
            <h2 className="text-3xl font-bold text-white mb-3">Reports & Analytics</h2>
            <p className="text-indigo-100 text-sm leading-relaxed">
              9 comprehensive report types with filters, charts, and PDF/Excel export capabilities.
              This module is part of <span className="font-semibold text-white">Phase 5</span> and will be available soon.
            </p>
          </div>
        </div>
      </div>

      {/* Report Type Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {reportTypes.map((r) => (
          <div key={r.title} className="bg-white rounded-xl border border-surface-200/60 p-5 shadow-card hover:shadow-card-hover transition-all group cursor-default">
            <div className="flex items-center gap-3 mb-3">
              <div className={`w-9 h-9 rounded-lg bg-gradient-to-br ${r.color} flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform`}>
                <r.icon className="w-4 h-4 text-white" />
              </div>
              <h3 className="text-sm font-semibold text-surface-900">{r.title}</h3>
            </div>
            <p className="text-xs text-surface-500 leading-relaxed">{r.desc}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
