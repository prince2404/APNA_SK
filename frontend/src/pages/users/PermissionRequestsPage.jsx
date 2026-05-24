import { useState, useEffect, useCallback } from 'react';
import { PageHeader } from '@/components/common/PageHeader';
import { Button } from '@/components/common/Button';
import { StatusBadge } from '@/components/common/StatusBadge';
import { EmptyState } from '@/components/common/EmptyState';
import { Pagination } from '@/components/common/Pagination';
import { Loader } from '@/components/common/Loader';
import { Modal } from '@/components/common/Modal';
import { Input } from '@/components/common/Input';
import { Badge } from '@/components/common/Badge';
import { permissionRequestApi } from '@/api/permissionRequestApi';
import { userApi } from '@/api/userApi';
import { usePermission } from '@/hooks/usePermission';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { FileKey, HelpCircle, Send, CheckCircle, XCircle } from 'lucide-react';

export default function PermissionRequestsPage() {
  const { hasPlatformScope } = usePermission();
  const [requests, setRequests] = useState([]);
  const [availablePermissions, setAvailablePermissions] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  // New Request Form
  const [selectedPermissionId, setSelectedPermissionId] = useState('');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Admin Review State
  const [statusFilter, setStatusFilter] = useState('');
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [reviewReason, setReviewReason] = useState('');
  const [reviewLoading, setReviewLoading] = useState(false);

  const fetchRequests = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 15 };
      if (statusFilter) params.status = statusFilter;
      const res = await permissionRequestApi.getRequests(params);
      const data = res.data.data;
      setRequests(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter]);

  const fetchAvailablePermissions = useCallback(async () => {
    if (hasPlatformScope) return; // Admins don't request permissions
    try {
      const res = await userApi.getPermissions();
      setAvailablePermissions(res.data.data || []);
    } catch (error) {
      toast.error('Failed to load permissions: ' + getErrorMessage(error));
    }
  }, [hasPlatformScope]);

  useEffect(() => {
    fetchRequests();
    fetchAvailablePermissions();
  }, [fetchRequests, fetchAvailablePermissions]);

  const handleSubmitRequest = async (e) => {
    e.preventDefault();
    if (!selectedPermissionId) {
      toast.warning('Please select a permission');
      return;
    }
    if (!reason.trim()) {
      toast.warning('Please provide a reason for the request');
      return;
    }

    setSubmitting(true);
    try {
      await permissionRequestApi.createRequest({
        permissionId: Number(selectedPermissionId),
        reason: reason.trim(),
      });
      toast.success('Permission request submitted successfully');
      setSelectedPermissionId('');
      setReason('');
      fetchRequests();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  const handleReview = async (id, status) => {
    if (status === 'REJECTED' && !reviewReason.trim()) {
      toast.warning('Please provide a reason for rejection');
      return;
    }

    setReviewLoading(true);
    try {
      const payload = { status };
      if (status === 'REJECTED') {
        payload.reason = reviewReason.trim();
      }
      await permissionRequestApi.reviewRequest(id, payload);
      toast.success(`Permission request ${status === 'APPROVED' ? 'approved' : 'rejected'} successfully`);
      setSelectedRequest(null);
      setReviewReason('');
      fetchRequests();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setReviewLoading(false);
    }
  };

  return (
    <div className="animate-fade-in">
      <PageHeader
        title="Permission Requests"
        description={
          hasPlatformScope
            ? "Review and approve/reject user requested system modules and action privileges."
            : "Request access permissions for advanced operations and track review status."
        }
      />

      {hasPlatformScope ? (
        // ADMIN DASHBOARD VIEW
        <div className="space-y-4">
          <div className="flex gap-2 border-b border-surface-200 pb-2">
            {[
              { label: 'All Requests', value: '' },
              { label: 'Pending', value: 'PENDING' },
              { label: 'Approved', value: 'APPROVED' },
              { label: 'Rejected', value: 'REJECTED' },
            ].map((tab) => (
              <button
                key={tab.value}
                onClick={() => {
                  setStatusFilter(tab.value);
                  setPage(0);
                }}
                className={`px-4 py-2 text-sm font-semibold rounded-lg transition-colors ${
                  statusFilter === tab.value
                    ? 'bg-primary-50 text-primary-700'
                    : 'text-surface-600 hover:bg-surface-50'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden">
            {loading ? (
              <Loader text="Loading requests..." />
            ) : requests.length === 0 ? (
              <EmptyState
                icon={FileKey}
                title="No Requests Found"
                description="There are no permission requests matching the filter."
              />
            ) : (
              <>
                <div className="overflow-x-auto">
                  <table className="w-full border-collapse text-left">
                    <thead>
                      <tr className="border-b border-surface-200 bg-surface-50 text-xs font-semibold text-surface-600 uppercase tracking-wider">
                        <th className="px-6 py-4">User</th>
                        <th className="px-6 py-4">Permission Requested</th>
                        <th className="px-6 py-4">Reason</th>
                        <th className="px-6 py-4">Created Date</th>
                        <th className="px-6 py-4">Status</th>
                        <th className="px-6 py-4 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-surface-100 text-sm text-surface-700">
                      {requests.map((req) => (
                        <tr key={req.id} className="hover:bg-surface-50/50 transition-colors">
                          <td className="px-6 py-4">
                            <div>
                              <p className="font-semibold text-surface-900">{req.userFullName}</p>
                              <p className="text-xs text-surface-500">{req.userEmail}</p>
                            </div>
                          </td>
                          <td className="px-6 py-4">
                            <Badge variant="warning">
                              {req.permissionModule}:{req.permissionAction}
                            </Badge>
                          </td>
                          <td className="px-6 py-4 max-w-xs truncate" title={req.reason}>
                            {req.reason}
                          </td>
                          <td className="px-6 py-4">
                            {new Date(req.createdAt).toLocaleDateString()}
                          </td>
                          <td className="px-6 py-4">
                            <StatusBadge status={req.status} />
                          </td>
                          <td className="px-6 py-4 text-right">
                            {req.status === 'PENDING' ? (
                              <div className="flex gap-2 justify-end">
                                <Button
                                  variant="danger"
                                  size="sm"
                                  onClick={() => {
                                    setSelectedRequest(req);
                                    setReviewReason('');
                                  }}
                                >
                                  Reject
                                </Button>
                                <Button
                                  variant="success"
                                  size="sm"
                                  onClick={() => handleReview(req.id, 'APPROVED')}
                                >
                                  Approve
                                </Button>
                              </div>
                            ) : (
                              <span className="text-xs text-surface-400">
                                Reviewed by {req.reviewedByEmail || 'Admin'}
                              </span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {totalPages > 1 && (
                  <div className="border-t border-surface-200 px-6 py-4">
                    <Pagination
                      page={page}
                      totalPages={totalPages}
                      totalElements={totalElements}
                      size={15}
                      onPageChange={setPage}
                    />
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      ) : (
        // USER SUBMISSION VIEW
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* New Request Card */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl border border-surface-200/60 shadow-card p-6">
              <h3 className="text-base font-semibold text-surface-900 mb-4 flex items-center gap-2">
                <FileKey className="w-5 h-5 text-primary-600" />
                Request Access
              </h3>
              <form onSubmit={handleSubmitRequest} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-surface-700 mb-1">
                    Select Permission
                  </label>
                  <select
                    value={selectedPermissionId}
                    onChange={(e) => setSelectedPermissionId(e.target.value)}
                    className="w-full px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
                  >
                    <option value="">-- Choose Permission --</option>
                    {availablePermissions.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.module} : {p.action} ({p.description})
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-surface-700 mb-1">
                    Justification / Reason
                  </label>
                  <textarea
                    rows={4}
                    placeholder="Provide details on why you need this permission..."
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    className="w-full px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
                  />
                </div>

                <Button type="submit" className="w-full" loading={submitting}>
                  <Send className="w-4 h-4 mr-1.5" /> Submit Request
                </Button>
              </form>
            </div>
          </div>

          {/* User Request History */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden">
              <div className="px-6 py-4 border-b border-surface-200">
                <h3 className="text-base font-semibold text-surface-900">Request History</h3>
              </div>
              {loading ? (
                <Loader text="Loading requests..." />
              ) : requests.length === 0 ? (
                <EmptyState
                  icon={HelpCircle}
                  title="No Requests Yet"
                  description="You have not requested any permissions yet."
                />
              ) : (
                <>
                  <div className="overflow-x-auto">
                    <table className="w-full border-collapse text-left">
                      <thead>
                        <tr className="border-b border-surface-200 bg-surface-50 text-xs font-semibold text-surface-600 uppercase tracking-wider">
                          <th className="px-6 py-4">Permission</th>
                          <th className="px-6 py-4">Justification</th>
                          <th className="px-6 py-4">Created Date</th>
                          <th className="px-6 py-4">Status</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-surface-100 text-sm text-surface-700">
                        {requests.map((req) => (
                          <tr key={req.id} className="hover:bg-surface-50/50 transition-colors">
                            <td className="px-6 py-4">
                              <Badge variant="warning">
                                {req.permissionModule}:{req.permissionAction}
                              </Badge>
                            </td>
                            <td className="px-6 py-4 max-w-xs truncate" title={req.reason}>
                              {req.reason}
                            </td>
                            <td className="px-6 py-4">
                              {new Date(req.createdAt).toLocaleDateString()}
                            </td>
                            <td className="px-6 py-4">
                              <div className="space-y-1">
                                <StatusBadge status={req.status} />
                                {req.status === 'REJECTED' && req.reviewedAt && (
                                  <p className="text-[10px] text-danger-600 italic">
                                    Rejected: User review reason not met.
                                  </p>
                                )}
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {totalPages > 1 && (
                    <div className="border-t border-surface-200 px-6 py-4">
                      <Pagination
                        page={page}
                        totalPages={totalPages}
                        totalElements={totalElements}
                        size={15}
                        onPageChange={setPage}
                      />
                    </div>
                  )}
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Reject Modal */}
      {selectedRequest && (
        <Modal
          isOpen={true}
          onClose={() => setSelectedRequest(null)}
          title="Reject Permission Request"
          size="sm"
        >
          <div className="space-y-4">
            <p className="text-sm text-surface-650">
              Please enter a reason for rejecting the permission request for{' '}
              <span className="font-semibold text-surface-900">
                {selectedRequest.permissionModule}:{selectedRequest.permissionAction}
              </span>{' '}
              requested by <span className="font-semibold text-surface-900">{selectedRequest.userFullName}</span>.
            </p>

            <Input
              label="Rejection Reason"
              placeholder="e.g. Justification insufficient..."
              value={reviewReason}
              onChange={(e) => setReviewReason(e.target.value)}
              required
              autoFocus
            />

            <div className="flex gap-3 justify-end pt-2 border-t border-surface-150">
              <Button
                variant="secondary"
                onClick={() => {
                  setSelectedRequest(null);
                  setReviewReason('');
                }}
                disabled={reviewLoading}
              >
                Cancel
              </Button>
              <Button
                variant="danger"
                loading={reviewLoading}
                onClick={() => handleReview(selectedRequest.id, 'REJECTED')}
              >
                Reject Request
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}
