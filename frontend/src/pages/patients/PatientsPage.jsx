import { Heart, UserPlus, Upload, CreditCard, Building2 } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';

const upcomingFeatures = [
  { icon: UserPlus, title: 'Patient Registration', desc: 'Register patients with demographics, geography mapping, and hospital referral tracking.' },
  { icon: Upload, title: 'Bulk Upload', desc: 'CSV-based bulk patient upload with duplicate detection by phone number and validation preview.' },
  { icon: CreditCard, title: 'Health Cards', desc: 'Digital health card issuance with QR codes, family member management (up to 5), and PDF download.' },
  { icon: Building2, title: 'Hospital Partners', desc: 'Manage partner hospitals and clinics for patient referral tracking and commission distribution.' },
];

export default function PatientsPage() {
  return (
    <div className="animate-fade-in">
      <PageHeader title="Patients" description="Patient registration and health card management" />

      {/* Coming Soon Hero */}
      <div className="bg-white rounded-2xl border border-surface-200/60 shadow-card overflow-hidden mb-6">
        <div className="bg-gradient-to-br from-rose-500 via-rose-600 to-pink-600 px-8 py-12 relative overflow-hidden">
          <div className="absolute right-0 top-0 w-72 h-72 bg-white/5 rounded-full -translate-y-1/3 translate-x-1/3" />
          <div className="absolute left-10 bottom-0 w-48 h-48 bg-white/5 rounded-full translate-y-1/2" />
          <div className="relative z-10 text-center max-w-lg mx-auto">
            <div className="w-20 h-20 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center mx-auto mb-5 shadow-lg">
              <Heart className="w-10 h-10 text-white" />
            </div>
            <h2 className="text-3xl font-bold text-white mb-3">Patient Management</h2>
            <p className="text-rose-100 text-sm leading-relaxed">
              Complete patient lifecycle — registration, health card issuance, family members, and purchase history.
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
              <div className="w-10 h-10 rounded-xl bg-rose-50 flex items-center justify-center shrink-0 group-hover:bg-rose-100 transition-colors">
                <f.icon className="w-5 h-5 text-rose-600" />
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
