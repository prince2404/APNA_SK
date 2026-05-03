import { PieChart, Settings, TrendingUp, FileSpreadsheet, IndianRupee } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';

const upcomingFeatures = [
  { icon: Settings, title: 'Commission Configuration', desc: 'Super Admin sets commission percentages per role level in the geographic hierarchy.' },
  { icon: TrendingUp, title: 'Automatic Calculation', desc: 'Commission auto-distributes up the geographic chain on each sale — receptionist to block to district to state admin.' },
  { icon: IndianRupee, title: 'Monthly Reports', desc: 'Monthly commission summaries per user with drill-down, totals, and payment status tracking.' },
  { icon: FileSpreadsheet, title: 'Export & Download', desc: 'Download commission reports in PDF and Excel formats for external payout processing.' },
];

export default function CommissionsPage() {
  return (
    <div className="animate-fade-in">
      <PageHeader title="Commissions" description="Commission tracking and reporting" />

      {/* Coming Soon Hero */}
      <div className="bg-white rounded-2xl border border-surface-200/60 shadow-card overflow-hidden mb-6">
        <div className="bg-gradient-to-br from-emerald-500 via-emerald-600 to-green-700 px-8 py-12 relative overflow-hidden">
          <div className="absolute right-0 top-0 w-72 h-72 bg-white/5 rounded-full -translate-y-1/3 translate-x-1/3" />
          <div className="absolute left-10 bottom-0 w-48 h-48 bg-white/5 rounded-full translate-y-1/2" />
          <div className="relative z-10 text-center max-w-lg mx-auto">
            <div className="w-20 h-20 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center mx-auto mb-5 shadow-lg">
              <PieChart className="w-10 h-10 text-white" />
            </div>
            <h2 className="text-3xl font-bold text-white mb-3">Commission Management</h2>
            <p className="text-emerald-100 text-sm leading-relaxed">
              Automated commission calculation and distribution across the geographic hierarchy with monthly reporting.
              This module is part of <span className="font-semibold text-white">Phase 5</span> and will be available soon.
            </p>
          </div>
        </div>
      </div>

      {/* Feature Preview */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {upcomingFeatures.map((f) => (
          <div key={f.title} className="bg-white rounded-xl border border-surface-200/60 p-5 shadow-card hover:shadow-card-hover transition-all group">
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center shrink-0 group-hover:bg-emerald-100 transition-colors">
                <f.icon className="w-5 h-5 text-emerald-600" />
              </div>
              <div>
                <h3 className="text-sm font-semibold text-surface-900 mb-1">{f.title}</h3>
                <p className="text-xs text-surface-500 leading-relaxed">{f.desc}</p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
