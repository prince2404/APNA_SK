import { useState, useEffect } from 'react';
import { RefreshCw, User, Calendar } from 'lucide-react';
import { inventoryApi } from '@/api/inventoryApi';
import { geographyApi } from '@/api/geographyApi';
import { toast } from '@/store/useNotificationStore';
import { useAuthStore } from '@/store/useAuthStore';
import { usePermission } from '@/hooks/usePermission';
import { Pagination } from '@/components/common/Pagination';
import { ROLES } from '@/constants/roles';

export default function AdjustmentsLogSection() {
  const user = useAuthStore((s) => s.user);
  const { hasPlatformScope, hasAnyRole } = usePermission();

  const [stores, setStores] = useState([]);
  const [selectedStoreId, setSelectedStoreId] = useState('');
  const [adjustments, setAdjustments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const size = 10;

  useEffect(() => {
    const init = async () => {
      if (hasPlatformScope || hasAnyRole(ROLES.PHARMACIST, ROLES.STATE_ADMIN, ROLES.DISTRICT_ADMIN)) {
        try {
          const res = await geographyApi.getStores({ page: 0, size: 100 });
          const storeList = res.data.data?.content || [];
          setStores(storeList);
          if (storeList.length > 0) {
            setSelectedStoreId(storeList[0].id.toString());
          }
        } catch {}
      } else if (user?.store) {
        setSelectedStoreId(user.store.id.toString());
      }
    };
    init();
  }, [hasPlatformScope, user]);

  const fetchAdjustments = async (pageNumber = 0) => {
    if (!selectedStoreId) return;
    setLoading(true);
    try {
      const res = await inventoryApi.getStockAdjustments({
        storeId: parseInt(selectedStoreId),
        page: pageNumber,
        size,
      });
      setAdjustments(res.data.data?.content || []);
      setTotalElements(res.data.data?.totalElements || 0);
      setPage(pageNumber);
    } catch {
      toast.error('Failed to load adjustments log');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAdjustments(0);
  }, [selectedStoreId]);

  return (
    <div className="space-y-4">
      {/* Selector Header */}
      <div className="bg-white rounded-xl border border-surface-200/60 p-4 shadow-sm flex items-center justify-between gap-4">
        {(hasPlatformScope || hasAnyRole(ROLES.PHARMACIST, ROLES.STATE_ADMIN, ROLES.DISTRICT_ADMIN)) ? (
          <div className="w-64">
            <label className="block text-xs font-semibold text-surface-500 uppercase tracking-wider mb-1.5">
              Select Store Filter
            </label>
            <select
              value={selectedStoreId}
              onChange={(e) => setSelectedStoreId(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            >
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
      </div>

      {/* Adjustments Table */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-50 border-b border-surface-200 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                <th className="px-6 py-4">Medicine Details</th>
                <th className="px-6 py-4">Batch Details</th>
                <th className="px-6 py-4 text-center">Type</th>
                <th className="px-6 py-4 text-right">Qty Change</th>
                <th className="px-6 py-4">Reason / Notes</th>
                <th className="px-6 py-4">Adjusted By</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-200 text-sm">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-10 text-center text-surface-400">
                    Loading adjustments history...
                  </td>
                </tr>
              ) : adjustments.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-10 text-center text-surface-400">
                    No stock adjustments logged for this store.
                  </td>
                </tr>
              ) : (
                adjustments.map((a) => (
                  <tr key={a.id} className="hover:bg-surface-50/50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="font-semibold text-surface-900">{a.product?.name}</div>
                      <div className="text-xs text-surface-500">{a.product?.brand}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="font-mono text-xs font-semibold text-surface-700">Batch: {a.batchNumber}</div>
                      <div className="text-[10px] text-surface-500 flex items-center gap-1 mt-0.5">
                        <Calendar className="w-3 h-3" /> {new Date(a.createdAt).toLocaleString('en-IN')}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span className={`inline-flex px-2 py-0.5 rounded text-xs font-bold ${
                        a.adjustmentType === 'DAMAGE'
                          ? 'bg-rose-50 text-rose-700 border border-rose-200'
                          : a.adjustmentType === 'RETURN'
                          ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                          : a.adjustmentType === 'EXPIRY'
                          ? 'bg-red-50 text-red-700 border border-red-200'
                          : 'bg-surface-100 text-surface-700'
                      }`}>
                        {a.adjustmentType}
                      </span>
                    </td>
                    <td className={`px-6 py-4 text-right font-bold ${
                      a.quantityChange < 0 ? 'text-rose-600' : 'text-emerald-600'
                    }`}>
                      {a.quantityChange > 0 ? `+${a.quantityChange}` : a.quantityChange}
                    </td>
                    <td className="px-6 py-4 text-surface-600 italic text-xs max-w-xs truncate" title={a.reason}>
                      {a.reason}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-1.5 text-xs text-surface-700">
                        <User className="w-3.5 h-3.5 text-surface-400" />
                        <div>
                          <div className="font-semibold">{a.adjustedBy?.fullName || 'System'}</div>
                          <div className="text-[10px] text-surface-400">{a.adjustedBy?.email}</div>
                        </div>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {totalElements > size && (
          <div className="px-6 py-4 border-t border-surface-200">
            <Pagination
              currentPage={page + 1}
              totalPages={Math.ceil(totalElements / size)}
              onPageChange={(page) => fetchAdjustments(page - 1)}
            />
          </div>
        )}
      </div>
    </div>
  );
}
