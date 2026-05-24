import { useState, useEffect } from 'react';
import { ClipboardList, Plus, FileText, CheckSquare, XSquare, AlertCircle } from 'lucide-react';
import { inventoryApi } from '@/api/inventoryApi';
import { productApi } from '@/api/productApi';
import { toast } from '@/store/useNotificationStore';
import { usePermission } from '@/hooks/usePermission';
import { Modal } from '@/components/common/Modal';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { Pagination } from '@/components/common/Pagination';
import { ROLES } from '@/constants/roles';

export default function StockRequestsPage() {
  const { hasPlatformScope, hasRole } = usePermission();
  const isPharmacistOrAdmin = hasPlatformScope || hasRole(ROLES.PHARMACIST);

  // Requests List State
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const size = 10;
  const [statusFilter, setStatusFilter] = useState('');

  // Modals
  const [isSubmitModalOpen, setIsSubmitModalOpen] = useState(false);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState(null);

  // Submit Request Form State
  const [productsList, setProductsList] = useState([]);
  const [submitForm, setSubmitForm] = useState({
    productId: '',
    quantityRequested: '',
    urgency: 'MEDIUM',
    notes: '',
  });
  const [submitting, setSubmitting] = useState(false);

  // Review Form State
  const [reviewForm, setReviewForm] = useState({
    status: 'APPROVED',
    notes: '',
  });
  const [submittingReview, setSubmittingReview] = useState(false);

  const fetchRequests = async (pageNumber = 0) => {
    setLoading(true);
    try {
      const params = {
        status: statusFilter || undefined,
        page: pageNumber,
        size,
      };
      const res = await inventoryApi.getStockRequests(params);
      setRequests(res.data.data?.content || []);
      setTotalElements(res.data.data?.totalElements || 0);
      setPage(pageNumber);
    } catch {
      toast.error('Failed to load stock requests list');
    } finally {
      setLoading(false);
    }
  };

  const loadProducts = async () => {
    try {
      const res = await productApi.getProducts({ page: 0, size: 500 });
      setProductsList(res.data.data?.content?.filter(p => p.status === 'ACTIVE') || []);
    } catch {}
  };

  useEffect(() => {
    fetchRequests(0);
    if (!isPharmacistOrAdmin) {
      loadProducts();
    }
  }, [isPharmacistOrAdmin, statusFilter]);

  const handleSubmitRequest = async (e) => {
    e.preventDefault();
    const qty = parseInt(submitForm.quantityRequested);
    if (!submitForm.productId || isNaN(qty) || qty <= 0) {
      toast.error('Please select a product and enter a quantity greater than 0');
      return;
    }

    setSubmitting(true);
    try {
      await inventoryApi.createStockRequest({
        productId: parseInt(submitForm.productId),
        quantityRequested: qty,
        urgency: submitForm.urgency,
        notes: submitForm.notes,
      });
      toast.success('Replenishment request submitted');
      setIsSubmitModalOpen(false);
      setSubmitForm({ productId: '', quantityRequested: '', urgency: 'MEDIUM', notes: '' });
      fetchRequests(0);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit request');
    } finally {
      setSubmitting(false);
    }
  };

  const openReviewModal = (req) => {
    setSelectedRequest(req);
    setReviewForm({ status: 'APPROVED', notes: '' });
    setIsReviewModalOpen(true);
  };

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    setSubmittingReview(true);
    try {
      await inventoryApi.reviewStockRequest(selectedRequest.id, {
        status: reviewForm.status,
        notes: reviewForm.notes,
      });
      toast.success(`Request ${reviewForm.status.toLowerCase()} successfully`);
      setIsReviewModalOpen(false);
      fetchRequests(page);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to review request');
    } finally {
      setSubmittingReview(false);
    }
  };

  const urgencyStyles = {
    LOW: 'bg-surface-100 text-surface-700',
    MEDIUM: 'bg-blue-50 text-blue-700 border-blue-150',
    HIGH: 'bg-amber-50 text-amber-700 border-amber-150',
    CRITICAL: 'bg-rose-50 text-rose-700 border-rose-150 font-bold animate-pulse',
  };

  return (
    <div className="space-y-4">
      {/* Filters & Actions Header */}
      <div className="bg-white rounded-xl border border-surface-200/60 p-4 shadow-sm flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div className="flex items-center gap-3">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
          >
            <option value="">All Statuses</option>
            <option value="PENDING">PENDING</option>
            <option value="APPROVED">APPROVED</option>
            <option value="FULFILLED">FULFILLED</option>
            <option value="REJECTED">REJECTED</option>
          </select>
        </div>

        {!isPharmacistOrAdmin && (
          <Button size="sm" onClick={() => setIsSubmitModalOpen(true)}>
            <Plus className="w-4 h-4 mr-1.5" /> Submit Request
          </Button>
        )}
      </div>

      {/* Requests Table */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-50 border-b border-surface-200 text-xs font-semibold text-surface-500 uppercase tracking-wider">
                <th className="px-6 py-4">Requested Item</th>
                <th className="px-6 py-4 text-center">Quantity</th>
                <th className="px-6 py-4 text-center">Urgency</th>
                <th className="px-6 py-4">Store / Requester</th>
                <th className="px-6 py-4 text-center">Status</th>
                {isPharmacistOrAdmin && <th className="px-6 py-4 text-right">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-200 text-sm">
              {loading ? (
                <tr>
                  <td colSpan={isPharmacistOrAdmin ? 6 : 5} className="px-6 py-10 text-center text-surface-400">
                    Loading replenishment requests...
                  </td>
                </tr>
              ) : requests.length === 0 ? (
                <tr>
                  <td colSpan={isPharmacistOrAdmin ? 6 : 5} className="px-6 py-10 text-center text-surface-400">
                    No stock requests found.
                  </td>
                </tr>
              ) : (
                requests.map((r) => (
                  <tr key={r.id} className="hover:bg-surface-50/50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="font-semibold text-surface-900">{r.product?.name}</div>
                      <div className="text-xs text-surface-500">{r.product?.brand}</div>
                    </td>
                    <td className="px-6 py-4 text-center font-bold text-surface-800">{r.quantityRequested}</td>
                    <td className="px-6 py-4 text-center">
                      <span className={`inline-flex px-2 py-0.5 rounded text-xs font-semibold border ${urgencyStyles[r.urgency]}`}>
                        {r.urgency}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="font-semibold text-surface-700">{r.store?.name}</div>
                      <div className="text-xs text-surface-500">By: {r.requestedBy?.fullName || r.requestedBy?.email}</div>
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                        r.status === 'PENDING'
                          ? 'bg-amber-100 text-amber-800'
                          : r.status === 'APPROVED'
                          ? 'bg-blue-100 text-blue-800'
                          : r.status === 'FULFILLED'
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-rose-100 text-rose-800'
                      }`}>
                        {r.status}
                      </span>
                      {r.notes && (
                        <div className="text-[10px] text-surface-500 mt-1 italic max-w-xs mx-auto truncate" title={r.notes}>
                          Notes: {r.notes}
                        </div>
                      )}
                    </td>
                    {isPharmacistOrAdmin && (
                      <td className="px-6 py-4 text-right">
                        {r.status === 'PENDING' ? (
                          <Button size="sm" onClick={() => openReviewModal(r)}>
                            Review
                          </Button>
                        ) : (
                          <span className="text-xs text-surface-400">—</span>
                        )}
                      </td>
                    )}
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
              onPageChange={(page) => fetchRequests(page - 1)}
            />
          </div>
        )}
      </div>

      {/* Submit Request Modal */}
      <Modal
        isOpen={isSubmitModalOpen}
        onClose={() => setIsSubmitModalOpen(false)}
        title="Submit Stock Replenishment Request"
      >
        <form onSubmit={handleSubmitRequest} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-surface-700 mb-1.5">
              Select Product *
            </label>
            <select
              value={submitForm.productId}
              required
              onChange={(e) => setSubmitForm({ ...submitForm, productId: e.target.value })}
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
            label="Quantity Requested *"
            type="number"
            min="1"
            required
            placeholder="e.g. 100"
            value={submitForm.quantityRequested}
            onChange={(e) => setSubmitForm({ ...submitForm, quantityRequested: e.target.value })}
          />

          <div>
            <label className="block text-sm font-medium text-surface-700 mb-1.5">
              Urgency Level *
            </label>
            <select
              value={submitForm.urgency}
              onChange={(e) => setSubmitForm({ ...submitForm, urgency: e.target.value })}
              className="w-full px-3 py-2.5 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            >
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
              <option value="CRITICAL">CRITICAL</option>
            </select>
          </div>

          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-surface-700">
              Notes / Justification
            </label>
            <textarea
              rows={3}
              placeholder="Provide context for urgency, special shipment instructions, etc."
              value={submitForm.notes}
              onChange={(e) => setSubmitForm({ ...submitForm, notes: e.target.value })}
              className="w-full px-3.5 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            />
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t border-surface-200">
            <Button variant="secondary" onClick={() => setIsSubmitModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={submitting}>
              Submit Request
            </Button>
          </div>
        </form>
      </Modal>

      {/* Review Request Modal */}
      {selectedRequest && (
        <Modal
          isOpen={isReviewModalOpen}
          onClose={() => setIsReviewModalOpen(false)}
          title={`Review Replenishment Request: ${selectedRequest.product?.name}`}
        >
          <form onSubmit={handleReviewSubmit} className="space-y-4">
            <div className="p-3 bg-surface-50 border border-surface-200 rounded-lg text-xs space-y-1">
              <div><span className="font-semibold">Store:</span> {selectedRequest.store?.name}</div>
              <div><span className="font-semibold">Requested By:</span> {selectedRequest.requestedBy?.fullName}</div>
              <div><span className="font-semibold">Quantity:</span> {selectedRequest.quantityRequested}</div>
              <div><span className="font-semibold">Urgency:</span> {selectedRequest.urgency}</div>
              {selectedRequest.notes && <div><span className="font-semibold">Requester Notes:</span> {selectedRequest.notes}</div>}
            </div>

            <div>
              <label className="block text-sm font-medium text-surface-700 mb-1.5">
                Action Decision *
              </label>
              <select
                value={reviewForm.status}
                onChange={(e) => setReviewForm({ ...reviewForm, status: e.target.value })}
                className="w-full px-3 py-2.5 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
              >
                <option value="APPROVED">APPROVE REQUEST</option>
                <option value="REJECTED">REJECT REQUEST</option>
              </select>
            </div>

            <div className="space-y-1.5">
              <label className="block text-sm font-medium text-surface-700">
                Reviewer Notes / Feedback *
              </label>
              <textarea
                rows={3}
                required
                placeholder="Provide feedback on the approval or rejection reason..."
                value={reviewForm.notes}
                onChange={(e) => setReviewForm({ ...reviewForm, notes: e.target.value })}
                className="w-full px-3.5 py-2 text-sm rounded-lg border border-surface-300 bg-white focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
              />
            </div>

            <div className="flex justify-end gap-3 pt-4 border-t border-surface-200">
              <Button variant="secondary" onClick={() => setIsReviewModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" loading={submittingReview}>
                Save Decision
              </Button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
