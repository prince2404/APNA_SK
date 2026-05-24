import { useState, useEffect } from 'react';
import { Truck, Plus, Eye, Check, X, ClipboardList, Trash2, Calendar } from 'lucide-react';
import { inventoryApi } from '@/api/inventoryApi';
import { geographyApi } from '@/api/geographyApi';
import { toast } from '@/store/useNotificationStore';
import { useAuthStore } from '@/store/useAuthStore';
import { usePermission } from '@/hooks/usePermission';
import { Modal } from '@/components/common/Modal';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { Pagination } from '@/components/common/Pagination';
import { ROLES } from '@/constants/roles';

export default function TransferOrdersPage() {
  const user = useAuthStore((s) => s.user);
  const { hasPlatformScope, hasRole } = usePermission();
  const isPharmacistOrAdmin = hasPlatformScope || hasRole(ROLES.PHARMACIST);

  // Transfers List
  const [transfers, setTransfers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const size = 10;

  // Modals
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [selectedTransfer, setSelectedTransfer] = useState(null);

  // Form State
  const [stores, setStores] = useState([]);
  const [centralBatches, setCentralBatches] = useState([]);
  const [selectedStoreId, setSelectedStoreId] = useState('');
  const [transferNotes, setTransferNotes] = useState('');
  const [transferItems, setTransferItems] = useState([
    { productId: '', batchNumber: '', quantity: '', availableQty: 0, key: Date.now() }
  ]);
  const [submitting, setSubmitting] = useState(false);

  const fetchTransfers = async (pageNumber = 0) => {
    setLoading(true);
    try {
      const res = await inventoryApi.getTransferOrders({ page: pageNumber, size });
      setTransfers(res.data.data?.content || []);
      setTotalElements(res.data.data?.totalElements || 0);
      setPage(pageNumber);
    } catch {
      toast.error('Failed to load transfer orders list');
    } finally {
      setLoading(false);
    }
  };

  const loadCentralBatches = async () => {
    try {
      const res = await inventoryApi.getCentralStock({ page: 0, size: 500 });
      const available = res.data.data?.content?.filter(b => b.status === 'AVAILABLE' && b.quantity > 0) || [];
      setCentralBatches(available);
    } catch {
      toast.error('Failed to load central warehouse inventory');
    }
  };

  const loadStores = async () => {
    try {
      const res = await geographyApi.getStores({ page: 0, size: 100 });
      setStores(res.data.data?.content || []);
    } catch {}
  };

  useEffect(() => {
    fetchTransfers(0);
    if (isPharmacistOrAdmin) {
      loadCentralBatches();
      loadStores();
    }
  }, [isPharmacistOrAdmin]);

  // Form helpers
  const handleAddItemRow = () => {
    setTransferItems([
      ...transferItems,
      { productId: '', batchNumber: '', quantity: '', availableQty: 0, key: Date.now() }
    ]);
  };

  const handleRemoveItemRow = (index) => {
    if (transferItems.length === 1) return;
    setTransferItems(transferItems.filter((_, i) => i !== index));
  };

  const handleItemChange = (index, batchIdStr) => {
    const newItems = [...transferItems];
    if (!batchIdStr) {
      newItems[index] = { ...newItems[index], productId: '', batchNumber: '', availableQty: 0 };
      setTransferItems(newItems);
      return;
    }

    const batch = centralBatches.find(b => b.id.toString() === batchIdStr);
    if (batch) {
      // Check if this batch is already added in another row
      const alreadyAdded = transferItems.some((item, i) => i !== index && item.productId === batch.product.id && item.batchNumber === batch.batchNumber);
      if (alreadyAdded) {
        toast.warning(`Batch ${batch.batchNumber} for ${batch.product.name} is already selected in another row.`);
      }

      newItems[index] = {
        ...newItems[index],
        productId: batch.product.id,
        batchNumber: batch.batchNumber,
        availableQty: batch.quantity,
        productName: batch.product.name,
      };
      setTransferItems(newItems);
    }
  };

  const handleQtyChange = (index, qtyStr) => {
    const newItems = [...transferItems];
    newItems[index].quantity = qtyStr;
    setTransferItems(newItems);
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    if (!selectedStoreId) {
      toast.error('Please select a target store');
      return;
    }

    const itemsPayload = [];
    for (const item of transferItems) {
      if (!item.productId || !item.batchNumber || !item.quantity) {
        toast.error('Please complete all item selections');
        return;
      }
      const qty = parseInt(item.quantity);
      if (isNaN(qty) || qty <= 0) {
        toast.error('Item transfer quantities must be greater than 0');
        return;
      }
      if (qty > item.availableQty) {
        toast.error(`Insufficient stock for ${item.productName} (batch: ${item.batchNumber}). Available: ${item.availableQty}`);
        return;
      }
      itemsPayload.push({
        productId: item.productId,
        batchNumber: item.batchNumber,
        quantity: qty,
      });
    }

    setSubmitting(true);
    try {
      await inventoryApi.createTransferOrder({
        storeId: parseInt(selectedStoreId),
        notes: transferNotes,
        items: itemsPayload,
      });
      toast.success('Stock transfer order created and dispatched');
      setIsCreateModalOpen(false);
      setSelectedStoreId('');
      setTransferNotes('');
      setTransferItems([{ productId: '', batchNumber: '', quantity: '', availableQty: 0, key: Date.now() }]);
      fetchTransfers(0);
      loadCentralBatches(); // Reload central stocks
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to dispatch transfer');
    } finally {
      setSubmitting(false);
    }
  };

  const viewDetail = async (transferId) => {
    try {
      const res = await inventoryApi.getTransferOrder(transferId);
      setSelectedTransfer(res.data.data);
      setIsDetailModalOpen(true);
    } catch {
      toast.error('Failed to fetch transfer order details');
    }
  };

  const handleConfirmReceipt = async (transferId) => {
    if (!window.confirm('Confirm that all items in this transfer order have been received?')) return;
    try {
      await inventoryApi.confirmTransferReceipt(transferId);
      toast.success('Transfer shipment received and store stock updated');
      setIsDetailModalOpen(false);
      fetchTransfers(page);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to confirm receipt');
    }
  };

  const handleCancelOrder = async (transferId) => {
    if (!window.confirm('Are you sure you want to cancel this transfer? Central stock will be restored.')) return;
    try {
      await inventoryApi.cancelTransferOrder(transferId);
      toast.success('Transfer order cancelled');
      setIsDetailModalOpen(false);
      fetchTransfers(page);
      if (isPharmacistOrAdmin) loadCentralBatches();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to cancel transfer');
    }
  };

  return (
    <div className="space-y-4">
      {/* Header Bar */}
      <div className="bg-white rounded-xl border border-surface-200/60 p-4 shadow-sm flex items-center justify-between">
        <h3 className="font-bold text-surface-900 flex items-center gap-2">
          <Truck className="w-5 h-5 text-primary-600" /> Stock Transfer Shipments
        </h3>
        {isPharmacistOrAdmin && (
          <Button size="sm" onClick={() => setIsCreateModalOpen(true)}>
            <Plus className="w-4 h-4 mr-1.5" /> Dispatch Stock
          </Button>
        )}
      </div>

      {/* Grid of Transfer Orders */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-50 border-b border-surface-200 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                <th className="px-6 py-4">Transfer Number</th>
                <th className="px-6 py-4">Target Store</th>
                <th className="px-6 py-4">Created Date</th>
                <th className="px-6 py-4 text-center">Items Count</th>
                <th className="px-6 py-4 text-center">Status</th>
                <th className="px-6 py-4 text-right">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-200 text-sm">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-10 text-center text-surface-400">
                    Loading transfer orders...
                  </td>
                </tr>
              ) : transfers.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-10 text-center text-surface-400">
                    No transfer orders found.
                  </td>
                </tr>
              ) : (
                transfers.map((t) => (
                  <tr key={t.id} className="hover:bg-surface-50/50 transition-colors">
                    <td className="px-6 py-4 font-mono font-bold text-surface-900">{t.transferNumber}</td>
                    <td className="px-6 py-4 font-semibold text-surface-700">{t.store?.name}</td>
                    <td className="px-6 py-4 text-surface-500">{new Date(t.createdAt).toLocaleDateString('en-IN')}</td>
                    <td className="px-6 py-4 text-center font-semibold text-surface-800">{t.items?.length || 0}</td>
                    <td className="px-6 py-4 text-center">
                      <span className={`inline-flex px-2 py-0.5 rounded text-xs font-semibold ${
                        t.status === 'PENDING'
                          ? 'bg-amber-100 text-amber-800'
                          : t.status === 'IN_TRANSIT'
                          ? 'bg-blue-100 text-blue-800'
                          : t.status === 'RECEIVED'
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-rose-100 text-rose-800'
                      }`}>
                        {t.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <Button size="sm" variant="secondary" onClick={() => viewDetail(t.id)}>
                        <Eye className="w-4 h-4 mr-1.5" /> View
                      </Button>
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
              onPageChange={(page) => fetchTransfers(page - 1)}
            />
          </div>
        )}
      </div>

      {/* Dispatch Stock Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create Stock Transfer Dispatch"
        size="xl"
      >
        <form onSubmit={handleCreateSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-surface-700 mb-1.5">
              Select Destination Store *
            </label>
            <select
              value={selectedStoreId}
              required
              onChange={(e) => setSelectedStoreId(e.target.value)}
              className="w-full px-3 py-2.5 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            >
              <option value="">Select store...</option>
              {stores.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} ({s.block?.name})
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <label className="block text-sm font-medium text-surface-700">
              Select Central Stock Batches to Transfer *
            </label>

            <div className="space-y-3 max-h-60 overflow-y-auto pr-1">
              {transferItems.map((item, index) => {
                // Find selected batch value
                const selectedBatchVal = centralBatches.find(
                  b => b.product.id === item.productId && b.batchNumber === item.batchNumber
                )?.id.toString() || '';

                return (
                  <div key={item.key} className="flex gap-3 items-end bg-surface-50 p-3 rounded-lg border border-surface-200">
                    <div className="flex-1">
                      <label className="block text-[10px] font-bold text-surface-500 uppercase mb-1">Select Batch</label>
                      <select
                        value={selectedBatchVal}
                        onChange={(e) => handleItemChange(index, e.target.value)}
                        className="w-full px-3 py-2 text-xs rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
                      >
                        <option value="">Choose available batch...</option>
                        {centralBatches.map((b) => (
                          <option key={b.id} value={b.id}>
                            {b.product.name} (Batch: {b.batchNumber}) - Avail: {b.quantity} (Exp: {new Date(b.expiryDate).toLocaleDateString('en-IN')})
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="w-24">
                      <Input
                        label="Qty"
                        type="number"
                        min="1"
                        max={item.availableQty || 1}
                        placeholder="0"
                        className="py-2 text-xs"
                        value={item.quantity}
                        onChange={(e) => handleQtyChange(index, e.target.value)}
                      />
                    </div>

                    <button
                      type="button"
                      onClick={() => handleRemoveItemRow(index)}
                      disabled={transferItems.length === 1}
                      className="p-2.5 rounded-lg text-rose-500 hover:bg-rose-50 border border-transparent disabled:opacity-30 cursor-pointer"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                );
              })}
            </div>

            <Button type="button" variant="outline" size="sm" onClick={handleAddItemRow} className="mt-2">
              <Plus className="w-4.5 h-4.5 mr-1" /> Add Medicine Batch
            </Button>
          </div>

          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-surface-700">
              Dispatch Notes
            </label>
            <textarea
              rows={2}
              placeholder="e.g. Sent via courier, urgent replenishment"
              value={transferNotes}
              onChange={(e) => setTransferNotes(e.target.value)}
              className="w-full px-3.5 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            />
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t border-surface-200">
            <Button variant="secondary" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={submitting}>
              Dispatch Shipment
            </Button>
          </div>
        </form>
      </Modal>

      {/* Detail Modal */}
      {selectedTransfer && (
        <Modal
          isOpen={isDetailModalOpen}
          onClose={() => setIsDetailModalOpen(false)}
          title={`Transfer Order: ${selectedTransfer.transferNumber}`}
          size="lg"
        >
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4 bg-surface-50 p-4 border border-surface-200 rounded-xl text-xs">
              <div><span className="font-semibold text-surface-500">Target Store:</span> {selectedTransfer.store?.name}</div>
              <div><span className="font-semibold text-surface-500">Status:</span> <span className="font-bold">{selectedTransfer.status}</span></div>
              <div><span className="font-semibold text-surface-500">Created By:</span> {selectedTransfer.createdBy?.fullName || selectedTransfer.createdBy?.email}</div>
              <div><span className="font-semibold text-surface-500">Dispatched At:</span> {new Date(selectedTransfer.createdAt).toLocaleString('en-IN')}</div>
              {selectedTransfer.confirmedBy && (
                <>
                  <div><span className="font-semibold text-surface-500">Confirmed By:</span> {selectedTransfer.confirmedBy?.fullName || selectedTransfer.confirmedBy?.email}</div>
                  <div><span className="font-semibold text-surface-500">Confirmed At:</span> {new Date(selectedTransfer.confirmedAt).toLocaleString('en-IN')}</div>
                </>
              )}
              {selectedTransfer.notes && <div className="col-span-2"><span className="font-semibold text-surface-500">Notes:</span> {selectedTransfer.notes}</div>}
            </div>

            <div className="space-y-2">
              <h4 className="font-bold text-sm text-surface-900">Shipment Items</h4>
              <div className="border border-surface-200 rounded-lg overflow-hidden">
                <table className="w-full text-left border-collapse text-xs">
                  <thead>
                    <tr className="bg-surface-50 border-b border-surface-200 font-semibold text-surface-500 uppercase tracking-wider">
                      <th className="px-4 py-3">Medicine Name</th>
                      <th className="px-4 py-3 font-mono">Batch</th>
                      <th className="px-4 py-3">Expiry Date</th>
                      <th className="px-4 py-3 text-right">Quantity</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-surface-150">
                    {selectedTransfer.items?.map((item) => (
                      <tr key={item.id}>
                        <td className="px-4 py-2.5 font-semibold text-surface-800">{item.product?.name}</td>
                        <td className="px-4 py-2.5 font-mono">{item.batchNumber}</td>
                        <td className="px-4 py-2.5 font-mono">{new Date(item.expiryDate).toLocaleDateString('en-IN')}</td>
                        <td className="px-4 py-2.5 text-right font-bold text-surface-800">{item.quantity}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Action Bar */}
            <div className="flex justify-between items-center pt-4 border-t border-surface-200">
              <div>
                {/* Cancel option for pharmacist/admin on PENDING */}
                {isPharmacistOrAdmin && selectedTransfer.status === 'PENDING' && (
                  <Button variant="danger" size="sm" onClick={() => handleCancelOrder(selectedTransfer.id)}>
                    <X className="w-4 h-4 mr-1.5" /> Cancel Order
                  </Button>
                )}
              </div>
              <div className="flex gap-2">
                <Button variant="secondary" size="sm" onClick={() => setIsDetailModalOpen(false)}>
                  Close
                </Button>
                {/* Confirm option for store workers on PENDING or IN_TRANSIT */}
                {(!isPharmacistOrAdmin || isSuperAdmin) && (selectedTransfer.status === 'PENDING' || selectedTransfer.status === 'IN_TRANSIT') && (
                  <Button size="sm" onClick={() => handleConfirmReceipt(selectedTransfer.id)}>
                    <Check className="w-4 h-4 mr-1.5" /> Confirm Receipt
                  </Button>
                )}
              </div>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}
