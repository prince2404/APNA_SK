import { Receipt, CreditCard, RotateCcw, Percent, FileText } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';

const upcomingFeatures = [
  { icon: Receipt, title: 'POS Billing', desc: 'Point-of-sale billing with patient search, item selection, real-time savings display, and instant bill generation.' },
  { icon: Percent, title: 'Scheme Management', desc: 'Super Admin creates discount schemes that are auto-applied during billing based on category and geography.' },
  { icon: RotateCcw, title: 'Returns & Refunds', desc: '7-day configurable return window with approval workflow. Automatic stock restoration on approved returns.' },
  { icon: FileText, title: 'Bill History & PDF', desc: 'Complete bill history with search, filters, PDF download, and print-ready formats.' },
];

export default function BillingPage() {
  return (
    <div className="animate-fade-in">
      <PageHeader title="Billing" description="Point of Sale and bill management" />

      {/* Coming Soon Hero */}
      <div className="bg-white rounded-2xl border border-surface-200/60 shadow-card overflow-hidden mb-6">
        <div className="bg-gradient-to-br from-violet-500 via-violet-600 to-purple-700 px-8 py-12 relative overflow-hidden">
          <div className="absolute right-0 top-0 w-72 h-72 bg-white/5 rounded-full -translate-y-1/3 translate-x-1/3" />
          <div className="absolute left-10 bottom-0 w-48 h-48 bg-white/5 rounded-full translate-y-1/2" />
          <div className="relative z-10 text-center max-w-lg mx-auto">
            <div className="w-20 h-20 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center mx-auto mb-5 shadow-lg">
              <Receipt className="w-10 h-10 text-white" />
            </div>
            <h2 className="text-3xl font-bold text-white mb-3">Billing & POS</h2>
            <p className="text-violet-100 text-sm leading-relaxed">
              Complete point-of-sale system with patient-linked billing, savings display, scheme management, and returns.
              This module is part of <span className="font-semibold text-white">Phase 4</span> and will be available soon.
            </p>
          </div>
        </div>
      </div>

      {/* Feature Preview */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {upcomingFeatures.map((f) => (
          <div key={f.title} className="bg-white rounded-xl border border-surface-200/60 p-5 shadow-card hover:shadow-card-hover transition-all group">
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 rounded-xl bg-violet-50 flex items-center justify-center shrink-0 group-hover:bg-violet-100 transition-colors">
                <f.icon className="w-5 h-5 text-violet-600" />
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
