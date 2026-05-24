import { useState, useEffect } from 'react';
import { CalendarRange, AlertTriangle, AlertCircle, Search } from 'lucide-react';
import { inventoryApi } from '@/api/inventoryApi';
import { geographyApi } from '@/api/geographyApi';
import { toast } from '@/store/useNotificationStore';
import { useAuthStore } from '@/store/useAuthStore';
import { usePermission } from '@/hooks/usePermission';
import { ROLES } from '@/constants/roles';

export default function ExpiryTrackingPage() {
  const user = useAuthStore((s) => s.user);
  const { hasPlatformScope, hasAnyRole } = usePermission();

  const [stores, setStores] = useState([]);
  const [selectedStoreId, setSelectedStoreId] = useState('');
  const [daysFilter, setDaysFilter] = useState('90');
  const [searchQuery, setSearchQuery] = useState('');

  const [expiringStocks, setExpiringStocks] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const init = async () => {
      if (hasPlatformScope || hasAnyRole(ROLES.PHARMACIST, ROLES.STATE_ADMIN, ROLES.DISTRICT_ADMIN)) {
        try {
          const res = await geographyApi.getStores({ page: 0, size: 100 });
          setStores(res.data.data?.content || []);
        } catch {}
      } else if (user?.store) {
        setSelectedStoreId(user.store.id.toString());
      }
    };
    init();
  }, [hasPlatformScope, user]);

  const fetchExpiringStock = async () => {
    setLoading(true);
    try {
      const params = {
        storeId: selectedStoreId ? parseInt(selectedStoreId) : undefined,
        days: parseInt(daysFilter),
      };
      const res = await inventoryApi.getExpiringStockAlerts(params);
      setExpiringStocks(res.data.data || []);
    } catch {
      toast.error('Failed to fetch expiring stock alerts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExpiringStock();
  }, [selectedStoreId, daysFilter]);

  const getDaysRemaining = (expiryDateStr) => {
    const expiry = new Date(expiryDateStr);
    const today = new Date();
    const diffTime = expiry - today;
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  };

  const filteredStocks = expiringStocks.filter(s => {
    if (!searchQuery) return true;
    return s.product?.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
           s.product?.brand?.toLowerCase().includes(searchQuery.toLowerCase()) ||
           s.batchNumber?.toLowerCase().includes(searchQuery.toLowerCase());
  });

  return (
    <div className="space-y-4">
      {/* Filters Header */}
      <div className="bg-white rounded-xl border border-surface-200/60 p-4 shadow-sm grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Store Selector */}
        {(hasPlatformScope || hasAnyRole(ROLES.PHARMACIST, ROLES.STATE_ADMIN, ROLES.DISTRICT_ADMIN)) ? (
          <div>
            <label className="block text-xs font-semibold text-surface-500 uppercase tracking-wider mb-1.5">
              Select Store Filter
            </label>
            <select
              value={selectedStoreId}
              onChange={(e) => setSelectedStoreId(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            >
              <option value="">All Platform Stores</option>
              {stores.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </div>
        ) : (
          <div>
            <label className="block text-xs font-semibold text-surface-500 uppercase tracking-wider mb-1.5">
              Assigned Store
            </label>
            <div className="px-3 py-2 text-sm rounded-lg border border-surface-200 bg-surface-50 text-surface-700 font-semibold">
              {user?.store?.name || 'No Store Assigned'}
            </div>
          </div>
        )}

        {/* Days Expiry window selection */}
        <div>
          <label className="block text-xs font-semibold text-surface-500 uppercase tracking-wider mb-1.5">
            Expiry Window
          </label>
          <select
            value={daysFilter}
            onChange={(e) => setDaysFilter(e.target.value)}
            className="w-full px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
          >
            <option value="30">Within 30 Days</option>
            <option value="60">Within 60 Days</option>
            <option value="90">Within 90 Days</option>
          </select>
        </div>

        {/* Search */}
        <div className="md:col-span-2">
          <label className="block text-xs font-semibold text-surface-500 uppercase tracking-wider mb-1.5">
            Search Medicine / Batch
          </label>
          <div className="relative">
            <Search className="absolute left-3 top-3 w-4 h-4 text-surface-400" />
            <input
              type="text"
              placeholder="Search by product name, brand or batch..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-sm rounded-lg border border-surface-300 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            />
          </div>
        </div>
      </div>

      {/* Grid of Expiring Stocks */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-50 border-b border-surface-200 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                <th className="px-6 py-4">Medicine Details</th>
                {(!selectedStoreId || hasPlatformScope) && <th className="px-6 py-4">Store Name</th>}
                <th className="px-6 py-4">Batch Details</th>
                <th className="px-6 py-4 text-right">Available Qty</th>
                <th className="px-6 py-4 text-center">Days Remaining</th>
                <th className="px-6 py-4">Expiry Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-200 text-sm">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-10 text-center text-surface-400">
                    Loading expiring stocks...
                  </td>
                </tr>
              ) : filteredStocks.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-10 text-center text-surface-400">
                    No medicine batches expiring within {daysFilter} days.
                  </td>
                </tr>
              ) : (
                filteredStocks.map((s) => {
                  const daysRemaining = getDaysRemaining(s.expiryDate);
                  const isSevere = daysRemaining <= 30;

                  return (
                    <tr key={s.id} className="hover:bg-surface-50/50 transition-colors">
                      <td className="px-6 py-4">
                        <div className="font-semibold text-surface-900">{s.product?.name}</div>
                        <div className="text-xs text-surface-500">{s.product?.brand} | {s.product?.category?.name}</div>
                      </td>
                      {(!selectedStoreId || hasPlatformScope) && (
                        <td className="px-6 py-4 font-semibold text-surface-650">{s.store?.name}</td>
                      )}
                      <td className="px-6 py-4 font-mono text-xs font-semibold text-surface-700">
                        {s.batchNumber}
                      </td>
                      <td className="px-6 py-4 text-right font-bold text-surface-800">
                        {s.quantity}
                      </td>
                      <td className="px-6 py-4 text-center">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full border text-xs font-semibold ${
                          isSevere
                            ? 'bg-rose-50 border-rose-200 text-rose-700 font-bold animate-pulse'
                            : 'bg-amber-50 border-amber-200 text-amber-700'
                        }`}>
                          {isSevere ? <AlertTriangle className="w-3.5 h-3.5 mr-1" /> : <AlertCircle className="w-3.5 h-3.5 mr-1" />}
                          {daysRemaining <= 0 ? 'Expired' : `${daysRemaining} days left`}
                        </span>
                      </td>
                      <td className="px-6 py-4 font-mono text-xs text-surface-650 font-semibold">
                        {new Date(s.expiryDate).toLocaleDateString('en-IN')}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
