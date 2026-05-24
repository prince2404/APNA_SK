import { useState, useEffect } from 'react';
import { Search, AlertTriangle, AlertCircle, RefreshCw, Layers } from 'lucide-react';
import { inventoryApi } from '@/api/inventoryApi';
import { productApi } from '@/api/productApi';
import { geographyApi } from '@/api/geographyApi';
import { toast } from '@/store/useNotificationStore';
import { useAuthStore } from '@/store/useAuthStore';
import { usePermission } from '@/hooks/usePermission';
import { Modal } from '@/components/common/Modal';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { Pagination } from '@/components/common/Pagination';
import { ROLES } from '@/constants/roles';

export default function StoreStockPage() {
  const user = useAuthStore((s) => s.user);
  const { hasPlatformScope, hasAnyRole, isSuperAdmin } = usePermission();

  // Scope check: adjust stock allowed for RECEPTIONIST, BLOCK_ADMIN, SUPER_ADMIN
  const canAdjust = isSuperAdmin || hasAnyRole(ROLES.RECEPTIONIST, ROLES.BLOCK_ADMIN);

  // Filter States
  const [stores, setStores] = useState([]);
  const [selectedStoreId, setSelectedStoreId] = useState('');
  const [categories, setCategories] = useState([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  // Stock List State
  const [stocks, setStocks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const size = 10;

  // Adjustment Modal
  const [isAdjustModalOpen, setIsAdjustModalOpen] = useState(false);
  const [selectedStock, setSelectedStock] = useState(null);
  const [adjustmentForm, setAdjustmentForm] = useState({
    quantityChange: '',
    adjustmentType: 'DAMAGE',
    reason: '',
  });
  const [submittingAdjustment, setSubmittingAdjustment] = useState(false);

  // Initialize store selection
  useEffect(() => {
    const initFilters = async () => {
      // Categories
      try {
        const catRes = await productApi.getCategories();
        setCategories(catRes.data.data || []);
      } catch {}

      // Stores
      if (hasPlatformScope || hasAnyRole(ROLES.PHARMACIST, ROLES.STATE_ADMIN, ROLES.DISTRICT_ADMIN)) {
        try {
          const storeRes = await geographyApi.getStores({ page: 0, size: 100 });
          const storeList = storeRes.data.data?.content || [];
          setStores(storeList);
          if (storeList.length > 0) {
            setSelectedStoreId(storeList[0].id.toString());
          }
        } catch {
          toast.error('Failed to load stores filter');
        }
      } else if (user?.store) {
        setSelectedStoreId(user.store.id.toString());
      }
    };
    initFilters();
  }, [hasPlatformScope, user]);

  // Fetch stock levels
  const fetchStock = async (pageNumber = 0) => {
    if (!selectedStoreId) return;
    setLoading(true);
    try {
      const params = {
        storeId: parseInt(selectedStoreId),
        search: searchQuery || undefined,
        categoryId: selectedCategoryId || undefined,
        page: pageNumber,
        size,
      };
      const res = await inventoryApi.getStoreStock(params);
      setStocks(res.data.data?.content || []);
      setTotalElements(res.data.data?.totalElements || 0);
      setPage(pageNumber);
    } catch (err) {
      toast.error('Failed to load store stock levels');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStock(0);
  }, [selectedStoreId, selectedCategoryId, searchQuery]);

  // Adjust stock submit
  const handleAdjustSubmit = async (e) => {
    e.preventDefault();
    const qtyChange = parseInt(adjustmentForm.quantityChange);

    if (isNaN(qtyChange) || qtyChange === 0) {
      toast.error('Quantity change must be a non-zero integer');
      return;
    }
    if (!adjustmentForm.reason.trim()) {
      toast.error('Reason is required');
      return;
    }

    const futureQty = selectedStock.quantity + qtyChange;
    if (futureQty < 0) {
      toast.error(`Stock cannot drop below 0. Current stock: ${selectedStock.quantity}`);
      return;
    }

    setSubmittingAdjustment(true);
    try {
      await inventoryApi.adjustStock({
        productId: selectedStock.product.id,
        batchNumber: selectedStock.batchNumber,
        quantityChange: qtyChange,
        adjustmentType: adjustmentForm.adjustmentType,
        reason: adjustmentForm.reason,
      });
      toast.success('Stock adjusted successfully');
      setIsAdjustModalOpen(false);
      setAdjustmentForm({ quantityChange: '', adjustmentType: 'DAMAGE', reason: '' });
      fetchStock(page);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to adjust stock');
    } finally {
      setSubmittingAdjustment(false);
    }
  };

  const openAdjustment = (stock) => {
    setSelectedStock(stock);
    setIsAdjustModalOpen(true);
  };

  // Helper formatting near-expiry date
  const getExpiryAlert = (dateStr) => {
    const expiry = new Date(dateStr);
    const today = new Date();
    const diffDays = Math.ceil((expiry - today) / (1000 * 60 * 60 * 24));
    if (diffDays <= 0) return { label: 'Expired', style: 'bg-rose-100 text-rose-800' };
    if (diffDays <= 30) return { label: `${diffDays} days left`, style: 'bg-rose-50 text-rose-700 border-rose-200' };
    if (diffDays <= 90) return { label: `${diffDays} days left`, style: 'bg-amber-50 text-amber-700 border-amber-200' };
    return null;
  };

  return (
    <div className="space-y-4">
      {/* Search & Selector Card */}
      <div className="bg-white rounded-xl border border-surface-200/60 p-4 shadow-sm grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Store Selector (if platform-wide or admin/pharmacist role) */}
        {(hasPlatformScope || hasAnyRole(ROLES.PHARMACIST, ROLES.STATE_ADMIN, ROLES.DISTRICT_ADMIN)) ? (
          <div>
            <label className="block text-xs font-semibold text-surface-500 uppercase tracking-wider mb-1.5">
              Select Store
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

        {/* Category filter */}
        <div>
          <label className="block text-xs font-semibold text-surface-500 uppercase tracking-wider mb-1.5">
            Category
          </label>
          <select
            value={selectedCategoryId}
            onChange={(e) => setSelectedCategoryId(e.target.value)}
            className="w-full px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
          >
            <option value="">All Categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </div>

        {/* Search Input */}
        <div className="md:col-span-2">
          <label className="block text-xs font-semibold text-surface-500 uppercase tracking-wider mb-1.5">
            Search Medicine
          </label>
          <div className="relative">
            <Search className="absolute left-3 top-3 w-4 h-4 text-surface-400" />
            <input
              type="text"
              placeholder="Search by product name or brand..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-sm rounded-lg border border-surface-300 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            />
          </div>
        </div>
      </div>

      {/* Stock Levels List */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-50 border-b border-surface-200 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                <th className="px-6 py-4">Medicine Details</th>
                <th className="px-6 py-4">Batch Details</th>
                <th className="px-6 py-4 text-right">Available Qty</th>
                <th className="px-6 py-4 text-center">Expiry Alerts</th>
                <th className="px-6 py-4 text-center">Low Stock status</th>
                {canAdjust && <th className="px-6 py-4 text-right">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-200 text-sm">
              {loading ? (
                <tr>
                  <td colSpan={canAdjust ? 6 : 5} className="px-6 py-10 text-center text-surface-400">
                    Loading stock levels...
                  </td>
                </tr>
              ) : stocks.length === 0 ? (
                <tr>
                  <td colSpan={canAdjust ? 6 : 5} className="px-6 py-10 text-center text-surface-400">
                    No stock logs found for this store.
                  </td>
                </tr>
              ) : (
                stocks.map((s) => {
                  const expiryAlert = getExpiryAlert(s.expiryDate);
                  const isLowStock = s.quantity < s.product.minStockThreshold;

                  return (
                    <tr key={s.id} className="hover:bg-surface-50/50 transition-colors">
                      <td className="px-6 py-4">
                        <div className="font-semibold text-surface-900">{s.product.name}</div>
                        <div className="text-xs text-surface-500">{s.product.brand} | {s.product.category?.name}</div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="font-mono text-xs font-semibold text-surface-700">Batch: {s.batchNumber}</div>
                        <div className="text-[11px] text-surface-500">Exp: {new Date(s.expiryDate).toLocaleDateString('en-IN')}</div>
                      </td>
                      <td className="px-6 py-4 text-right font-bold text-surface-800">
                        {s.quantity}
                      </td>
                      <td className="px-6 py-4 text-center">
                        {expiryAlert ? (
                          <span className={`inline-flex items-center px-2 py-0.5 rounded-full border text-xs font-semibold ${expiryAlert.style}`}>
                            <AlertCircle className="w-3.5 h-3.5 mr-1" /> {expiryAlert.label}
                          </span>
                        ) : (
                          <span className="text-xs text-surface-400">—</span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-center">
                        {isLowStock ? (
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold">
                            <AlertTriangle className="w-3.5 h-3.5 mr-1" /> Low Stock
                          </span>
                        ) : (
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full bg-emerald-50 text-emerald-700 text-xs font-semibold">
                            Sufficient
                          </span>
                        )}
                      </td>
                      {canAdjust && (
                        <td className="px-6 py-4 text-right">
                          <Button size="sm" variant="secondary" onClick={() => openAdjustment(s)}>
                            <RefreshCw className="w-3.5 h-3.5 mr-1" /> Adjust
                          </Button>
                        </td>
                      )}
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {totalElements > size && (
          <div className="px-6 py-4 border-t border-surface-200">
            <Pagination
              currentPage={page + 1}
              totalPages={Math.ceil(totalElements / size)}
              onPageChange={(page) => fetchStock(page - 1)}
            />
          </div>
        )}
      </div>

      {/* Adjust Stock Modal */}
      {selectedStock && (
        <Modal
          isOpen={isAdjustModalOpen}
          onClose={() => setIsAdjustModalOpen(false)}
          title={`Adjust Inventory Level: ${selectedStock.product.name}`}
        >
          <form onSubmit={handleAdjustSubmit} className="space-y-4">
            <div className="p-3 bg-surface-50 border border-surface-200 rounded-lg text-xs space-y-1">
              <div><span className="font-semibold">Batch Number:</span> {selectedStock.batchNumber}</div>
              <div><span className="font-semibold">Current Quantity:</span> {selectedStock.quantity}</div>
              <div><span className="font-semibold">Min Stock Threshold:</span> {selectedStock.product.minStockThreshold}</div>
            </div>

            <Input
              label="Quantity Change *"
              type="number"
              required
              placeholder="e.g. -5 to reduce, 10 to increase"
              value={adjustmentForm.quantityChange}
              onChange={(e) => setAdjustmentForm({ ...adjustmentForm, quantityChange: e.target.value })}
            />

            <div>
              <label className="block text-sm font-medium text-surface-700 mb-1.5">
                Adjustment Type *
              </label>
              <select
                value={adjustmentForm.adjustmentType}
                onChange={(e) => setAdjustmentForm({ ...adjustmentForm, adjustmentType: e.target.value })}
                className="w-full px-3 py-2.5 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
              >
                <option value="DAMAGE">DAMAGE (Reduces Stock)</option>
                <option value="RETURN">RETURN (Increases Stock)</option>
                <option value="CORRECTION">CORRECTION (Manual audit audit trail)</option>
                <option value="EXPIRY">EXPIRY (Stock disposal)</option>
              </select>
            </div>

            <div className="space-y-1.5">
              <label className="block text-sm font-medium text-surface-700">
                Reason *
              </label>
              <textarea
                rows={3}
                required
                placeholder="Provide a reason for the stock adjustment (e.g. glass broken, customer returned)"
                value={adjustmentForm.reason}
                onChange={(e) => setAdjustmentForm({ ...adjustmentForm, reason: e.target.value })}
                className="w-full px-3.5 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
              />
            </div>

            <div className="flex justify-end gap-3 pt-4 border-t border-surface-200">
              <Button variant="secondary" onClick={() => setIsAdjustModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" loading={submittingAdjustment}>
                Save Adjustment
              </Button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
