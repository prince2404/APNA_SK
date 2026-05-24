import { useState } from 'react';
import { LayoutGrid, Warehouse, Truck, ClipboardList, AlertTriangle, RefreshCw } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';
import { usePermission } from '@/hooks/usePermission';
import { ROLES } from '@/constants/roles';

// Sub-pages/Sections
import StoreStockPage from './StoreStockPage';
import CentralReceiptPage from './CentralReceiptPage';
import TransferOrdersPage from './TransferOrdersPage';
import StockRequestsPage from './StockRequestsPage';
import ExpiryTrackingPage from './ExpiryTrackingPage';
import AdjustmentsLogSection from './AdjustmentsLogSection';

export default function InventoryPage() {
  const { hasPlatformScope, hasRole } = usePermission();
  const isPharmacistOrAdmin = hasPlatformScope || hasRole(ROLES.PHARMACIST);

  // Tab State
  const [activeTab, setActiveTab] = useState('stock'); // 'stock' | 'central' | 'transfers' | 'requests' | 'expiry' | 'adjustments'

  const tabs = [
    { id: 'stock', label: 'Store Stock', icon: LayoutGrid, show: true },
    { id: 'central', label: 'Central Warehouse', icon: Warehouse, show: isPharmacistOrAdmin },
    { id: 'transfers', label: 'Transfer Orders', icon: Truck, show: true },
    { id: 'requests', label: 'Stock Requests', icon: ClipboardList, show: true },
    { id: 'expiry', label: 'Expiry Tracking', icon: AlertTriangle, show: true },
    { id: 'adjustments', label: 'Adjustments Log', icon: RefreshCw, show: true },
  ].filter(t => t.show);

  return (
    <div className="space-y-6 animate-fade-in">
      <PageHeader
        title="Inventory & Stock Management"
        description="Receive stock centrally, dispatch store transfer orders, monitor low-stock limits, and trace adjustments."
      />

      {/* Tabs Row */}
      <div className="border-b border-surface-200">
        <div className="flex flex-wrap gap-2 md:gap-4">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`py-3 px-4 text-xs md:text-sm font-semibold border-b-2 transition-all flex items-center gap-2 cursor-pointer ${
                  isActive
                    ? 'border-primary-600 text-primary-600 font-bold'
                    : 'border-transparent text-surface-500 hover:text-surface-800'
                }`}
              >
                <Icon className="w-4 h-4" /> {tab.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Tab Contents */}
      <div className="mt-4">
        {activeTab === 'stock' && <StoreStockPage />}
        {activeTab === 'central' && isPharmacistOrAdmin && <CentralReceiptPage />}
        {activeTab === 'transfers' && <TransferOrdersPage />}
        {activeTab === 'requests' && <StockRequestsPage />}
        {activeTab === 'expiry' && <ExpiryTrackingPage />}
        {activeTab === 'adjustments' && <AdjustmentsLogSection />}
      </div>
    </div>
  );
}
