import { Package, Tags, BarChart3, AlertTriangle, ArrowRight } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';

const upcomingFeatures = [
  { icon: Package, title: 'Product Catalogue', desc: 'Manage products with name, brand, category, HSN code, MRP, ASK price, and GST details.' },
  { icon: Tags, title: 'Category Management', desc: 'Organize products into categories: Medicine, Baby Food, Cosmetics, and more.' },
  { icon: BarChart3, title: 'Price Management', desc: 'Configure MRP vs ASK pricing to show customer savings on every purchase.' },
  { icon: AlertTriangle, title: 'Stock Thresholds', desc: 'Set minimum stock levels per product and receive automatic low-stock alerts.' },
];

export default function ProductsPage() {
  return (
    <div className="animate-fade-in">
      <PageHeader title="Products" description="Product catalogue management" />

      {/* Coming Soon Hero */}
      <div className="bg-white rounded-2xl border border-surface-200/60 shadow-card overflow-hidden mb-6">
        <div className="bg-gradient-to-br from-amber-500 via-amber-600 to-orange-600 px-8 py-12 relative overflow-hidden">
          <div className="absolute right-0 top-0 w-72 h-72 bg-white/5 rounded-full -translate-y-1/3 translate-x-1/3" />
          <div className="absolute left-10 bottom-0 w-48 h-48 bg-white/5 rounded-full translate-y-1/2" />
          <div className="relative z-10 text-center max-w-lg mx-auto">
            <div className="w-20 h-20 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center mx-auto mb-5 shadow-lg">
              <Package className="w-10 h-10 text-white" />
            </div>
            <h2 className="text-3xl font-bold text-white mb-3">Product Catalogue</h2>
            <p className="text-amber-100 text-sm leading-relaxed">
              Complete product management with categories, pricing, GST configuration, and stock thresholds.
              This module is part of <span className="font-semibold text-white">Phase 3</span> and will be available soon.
            </p>
          </div>
        </div>
      </div>

      {/* Feature Preview */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {upcomingFeatures.map((f) => (
          <div key={f.title} className="bg-white rounded-xl border border-surface-200/60 p-5 shadow-card hover:shadow-card-hover transition-all group">
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center shrink-0 group-hover:bg-amber-100 transition-colors">
                <f.icon className="w-5 h-5 text-amber-600" />
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
