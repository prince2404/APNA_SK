import { useState, useEffect } from 'react';
import { Plus, Calendar, ShieldCheck, ClipboardCheck } from 'lucide-react';
import { inventoryApi } from '@/api/inventoryApi';
import { productApi } from '@/api/productApi';
import { toast } from '@/store/useNotificationStore';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { Pagination } from '@/components/common/Pagination';

export default function CentralReceiptPage() {
  const [productsList, setProductsList] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(false);

  // Central log State
  const [receiptLog, setReceiptLog] = useState([]);
  const [loadingLog, setLoadingLog] = useState(false);
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const size = 10;

  // Receipt Form State
  const [form, setForm] = useState({
    productId: '',
    batchNumber: '',
    manufacturingDate: '',
    expiryDate: '',
    quantity: '',
  });
  const [submitting, setSubmitting] = useState(false);

  // Load active products for dropdown and log list
  const loadInitialData = async () => {
    setLoadingProducts(true);
    try {
      const prodRes = await productApi.getProducts({ page: 0, size: 1000 });
      setProductsList(prodRes.data.data?.content?.filter(p => p.status === 'ACTIVE') || []);
    } catch {
      toast.error('Failed to load active products catalogue');
    } finally {
      setLoadingProducts(false);
    }
  };

  const fetchReceiptLog = async (pageNumber = 0) => {
    setLoadingLog(true);
    try {
      const res = await inventoryApi.getCentralStock({ page: pageNumber, size });
      setReceiptLog(res.data.data?.content || []);
      setTotalElements(res.data.data?.totalElements || 0);
      setPage(pageNumber);
    } catch {
      toast.error('Failed to load central warehouse stock log');
    } finally {
      setLoadingLog(false);
    }
  };

  useEffect(() => {
    loadInitialData();
    fetchReceiptLog(0);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const qty = parseInt(form.quantity);
    if (!form.productId || !form.batchNumber || !form.manufacturingDate || !form.expiryDate) {
      toast.error('Please fill in all fields');
      return;
    }
    if (isNaN(qty) || qty <= 0) {
      toast.error('Quantity must be greater than 0');
      return;
    }

    const expiry = new Date(form.expiryDate);
    const mfg = new Date(form.manufacturingDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (expiry <= today) {
      toast.error('Expiry date must be in the future');
      return;
    }
    if (expiry <= mfg) {
      toast.error('Expiry date must be after manufacturing date');
      return;
    }

    setSubmitting(true);
    try {
      await inventoryApi.receiveCentralStock({
        productId: parseInt(form.productId),
        batchNumber: form.batchNumber,
        manufacturingDate: form.manufacturingDate,
        expiryDate: form.expiryDate,
        quantity: qty,
      });
      toast.success('Central stock logged successfully');
      setForm({
        productId: '',
        batchNumber: '',
        manufacturingDate: '',
        expiryDate: '',
        quantity: '',
      });
      fetchReceiptLog(0);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to log stock receipt');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
      {/* Receipt Form */}
      <div className="bg-white rounded-xl border border-surface-200/60 p-6 shadow-sm h-fit">
        <h3 className="text-base font-bold text-surface-900 mb-4 flex items-center gap-2 border-b border-surface-150 pb-2">
          <ClipboardCheck className="w-5 h-5 text-primary-600" /> Log Incoming Shipment
        </h3>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-surface-700 mb-1.5">
              Select Product *
            </label>
            <select
              value={form.productId}
              required
              onChange={(e) => setForm({ ...form, productId: e.target.value })}
              className="w-full px-3 py-2.5 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            >
              <option value="">Choose medicine...</option>
              {productsList.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} ({p.brand})
                </option>
              ))}
            </select>
          </div>

          <Input
            label="Batch Number *"
            required
            placeholder="e.g. BATCH102"
            value={form.batchNumber}
            onChange={(e) => setForm({ ...form, batchNumber: e.target.value })}
          />

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Mfg Date *"
              type="date"
              required
              value={form.manufacturingDate}
              onChange={(e) => setForm({ ...form, manufacturingDate: e.target.value })}
            />
            <Input
              label="Expiry Date *"
              type="date"
              required
              value={form.expiryDate}
              onChange={(e) => setForm({ ...form, expiryDate: e.target.value })}
            />
          </div>

          <Input
            label="Quantity (Units) *"
            type="number"
            min="1"
            required
            placeholder="e.g. 500"
            value={form.quantity}
            onChange={(e) => setForm({ ...form, quantity: e.target.value })}
          />

          <Button type="submit" loading={submitting} className="w-full">
            <Plus className="w-4 h-4 mr-1.5" /> Log Stock Receipt
          </Button>
        </form>
      </div>

      {/* Central Stock Log */}
      <div className="lg:col-span-2 space-y-4">
        <div className="bg-white rounded-xl border border-surface-200/60 shadow-sm overflow-hidden">
          <div className="px-6 py-4 border-b border-surface-200 bg-surface-50 flex items-center justify-between">
            <h3 className="font-bold text-surface-900 flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-emerald-600" /> Central Stock Inventory
            </h3>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-surface-50 border-b border-surface-200 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                  <th className="px-6 py-4">Medicine</th>
                  <th className="px-6 py-4">Batch Number</th>
                  <th className="px-6 py-4 text-right">Available Qty</th>
                  <th className="px-6 py-4">Expiry Date</th>
                  <th className="px-6 py-4 text-center">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-200 text-sm">
                {loadingLog ? (
                  <tr>
                    <td colSpan={5} className="px-6 py-10 text-center text-surface-400">
                      Loading central inventory records...
                    </td>
                  </tr>
                ) : receiptLog.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-6 py-10 text-center text-surface-400">
                      No central inventory records logged yet.
                    </td>
                  </tr>
                ) : (
                  receiptLog.map((log) => (
                    <tr key={log.id} className="hover:bg-surface-50/50 transition-colors">
                      <td className="px-6 py-4">
                        <div className="font-semibold text-surface-900">{log.product.name}</div>
                        <div className="text-xs text-surface-500">{log.product.brand}</div>
                      </td>
                      <td className="px-6 py-4 font-mono text-xs font-semibold text-surface-700">
                        {log.batchNumber}
                      </td>
                      <td className="px-6 py-4 text-right font-bold text-surface-800">
                        {log.quantity}
                      </td>
                      <td className="px-6 py-4 font-mono text-xs text-surface-600">
                        {new Date(log.expiryDate).toLocaleDateString('en-IN')}
                      </td>
                      <td className="px-6 py-4 text-center">
                        <span className={`inline-flex px-2 py-0.5 rounded text-xs font-semibold ${
                          log.status === 'AVAILABLE'
                            ? 'bg-emerald-100 text-emerald-800'
                            : log.status === 'TRANSFERRED'
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-rose-100 text-rose-800'
                        }`}>
                          {log.status}
                        </span>
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
                onPageChange={(page) => fetchReceiptLog(page - 1)}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
