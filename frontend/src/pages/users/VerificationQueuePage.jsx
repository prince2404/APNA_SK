import { useState, useEffect, useCallback } from 'react';
import { ShieldCheck, UserCheck, UserX, Download, Eye, FileText, Landmark, FileSpreadsheet } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';
import { Button } from '@/components/common/Button';
import { StatusBadge } from '@/components/common/StatusBadge';
import { EmptyState } from '@/components/common/EmptyState';
import { Pagination } from '@/components/common/Pagination';
import { Loader } from '@/components/common/Loader';
import { Modal } from '@/components/common/Modal';
import { Input } from '@/components/common/Input';
import { Badge } from '@/components/common/Badge';
import { userApi } from '@/api/userApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { ROLE_DISPLAY_NAMES } from '@/constants/roles';

export default function VerificationQueuePage() {
  const [queue, setQueue] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  // Modal details
  const [selectedUser, setSelectedUser] = useState(null);
  const [rejecting, setRejecting] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const fetchQueue = useCallback(async () => {
    setLoading(true);
    try {
      const res = await userApi.getVerificationQueue({ page, size: 20 });
      const data = res.data.data;
      setQueue(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchQueue();
  }, [fetchQueue]);

  const handleDownloadDoc = async (user) => {
    try {
      const res = await userApi.getUserKycDocument(user.id);
      const blob = new Blob([res.data], { type: res.headers['content-type'] });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `aadhaar_${user.fullName.replace(/\s+/g, '_')}_${user.id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      toast.success('Document downloaded successfully');
    } catch (error) {
      toast.error('Failed to download document: ' + getErrorMessage(error));
    }
  };

  const handleVerify = async (status) => {
    if (status === 'REJECTED' && !rejectReason.trim()) {
      toast.warning('Please provide a reason for rejection');
      return;
    }

    setActionLoading(true);
    try {
      const payload = { status };
      if (status === 'REJECTED') {
        payload.reason = rejectReason;
      }
      await userApi.verifyUser(selectedUser.id, payload);
      toast.success(`User KYC ${status === 'VERIFIED' ? 'approved' : 'rejected'} successfully`);
      setSelectedUser(null);
      setRejecting(false);
      setRejectReason('');
      fetchQueue();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setActionLoading(false);
    }
  };

  const getInitials = (name) => name?.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() || 'U';

  return (
    <div className="animate-fade-in">
      <PageHeader
        title="KYC Verification Queue"
        description="Verify pending user identity details, encrypted bank accounts, and Aadhaar documents."
      />

      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden">
        {loading ? (
          <Loader text="Loading queue..." />
        ) : queue.length === 0 ? (
          <EmptyState
            icon={ShieldCheck}
            title="Verification Queue Clear"
            description="There are no pending user profile submissions requiring review."
          />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full border-collapse text-left">
                <thead>
                  <tr className="border-b border-surface-200 bg-surface-50 text-xs font-semibold text-surface-600 uppercase tracking-wider">
                    <th className="px-6 py-4">User</th>
                    <th className="px-6 py-4">Role</th>
                    <th className="px-6 py-4">Submitted Date</th>
                    <th className="px-6 py-4">Geography</th>
                    <th className="px-6 py-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-100 text-sm text-surface-700">
                  {queue.map((u) => {
                    const geo = [u.stateName, u.districtName, u.blockName, u.storeName].filter(Boolean).join(' → ');
                    return (
                      <tr key={u.id} className="hover:bg-surface-50/50 transition-colors">
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-xl bg-primary-50 flex items-center justify-center font-semibold text-primary-700 uppercase shrink-0">
                              {getInitials(u.fullName)}
                            </div>
                            <div>
                              <p className="font-semibold text-surface-900">{u.fullName}</p>
                              <p className="text-xs text-surface-500">{u.email}</p>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <Badge variant="info">
                            {ROLE_DISPLAY_NAMES[u.roleName] || u.roleName}
                          </Badge>
                        </td>
                        <td className="px-6 py-4">
                          {new Date(u.updatedAt || u.createdAt).toLocaleDateString()}
                        </td>
                        <td className="px-6 py-4 text-xs max-w-xs truncate">
                          {geo || 'Platform-wide'}
                        </td>
                        <td className="px-6 py-4 text-right">
                          <Button
                            variant="secondary"
                            size="sm"
                            onClick={() => {
                              setSelectedUser(u);
                              setRejecting(false);
                              setRejectReason('');
                            }}
                          >
                            <Eye className="w-4 h-4 mr-1" /> Review KYC
                          </Button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {totalPages > 1 && (
              <div className="border-t border-surface-200 px-6 py-4">
                <Pagination
                  page={page}
                  totalPages={totalPages}
                  totalElements={totalElements}
                  size={20}
                  onPageChange={setPage}
                />
              </div>
            )}
          </>
        )}
      </div>

      {/* Review Modal */}
      {selectedUser && (
        <Modal
          isOpen={true}
          onClose={() => setSelectedUser(null)}
          title="Review KYC Details"
          size="lg"
        >
          <div className="space-y-6">
            {/* Header User info */}
            <div className="flex items-center gap-4 p-4 rounded-xl bg-slate-50 border border-slate-100">
              <div className="w-12 h-12 rounded-xl bg-primary-100 flex items-center justify-center font-bold text-primary-800 text-lg uppercase">
                {getInitials(selectedUser.fullName)}
              </div>
              <div>
                <h4 className="font-semibold text-surface-900">{selectedUser.fullName}</h4>
                <p className="text-xs text-surface-500">{selectedUser.email} • {selectedUser.phone}</p>
                <div className="flex items-center gap-2 mt-1.5">
                  <Badge>{ROLE_DISPLAY_NAMES[selectedUser.roleName] || selectedUser.roleName}</Badge>
                  <span className="text-xs text-surface-400">
                    {[selectedUser.stateName, selectedUser.districtName, selectedUser.blockName, selectedUser.storeName].filter(Boolean).join(' → ') || 'Platform-wide'}
                  </span>
                </div>
              </div>
            </div>

            {/* KYC details grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="p-4 rounded-xl border border-surface-200 flex flex-col gap-2 bg-white">
                <div className="flex items-center gap-2 text-primary-700 font-semibold mb-2">
                  <Landmark className="w-5 h-5" />
                  <span>Bank & Financial Details</span>
                </div>
                <div className="space-y-2.5 text-sm">
                  <div>
                    <span className="text-xs text-surface-500 block">Bank Name</span>
                    <span className="font-medium text-surface-900">{selectedUser.bankName || '—'}</span>
                  </div>
                  <div>
                    <span className="text-xs text-surface-500 block">IFSC Code</span>
                    <span className="font-medium text-surface-900 font-mono">{selectedUser.bankIfsc || '—'}</span>
                  </div>
                  <div>
                    <span className="text-xs text-surface-500 block">Bank Account Number</span>
                    <span className="font-medium text-surface-900 font-mono">{selectedUser.bankAccount || '—'}</span>
                  </div>
                </div>
              </div>

              <div className="p-4 rounded-xl border border-surface-200 flex flex-col gap-2 bg-white">
                <div className="flex items-center gap-2 text-primary-700 font-semibold mb-2">
                  <FileText className="w-5 h-5" />
                  <span>Identity Documents</span>
                </div>
                <div className="space-y-2.5 text-sm">
                  <div>
                    <span className="text-xs text-surface-500 block">PAN Number</span>
                    <span className="font-medium text-surface-900 font-mono">{selectedUser.panNumber || '—'}</span>
                  </div>
                  <div>
                    <span className="text-xs text-surface-500 block">Aadhaar Last 4 Digits</span>
                    <span className="font-medium text-surface-900 font-mono">XXXX XXXX {selectedUser.aadhaarLastFour || '—'}</span>
                  </div>
                  <div className="pt-1">
                    <Button
                      variant="secondary"
                      size="sm"
                      className="w-full flex justify-center items-center gap-2 mt-2"
                      onClick={() => handleDownloadDoc(selectedUser)}
                    >
                      <Download className="w-4 h-4" /> Download Aadhaar Doc
                    </Button>
                  </div>
                </div>
              </div>
            </div>

            {/* Approve/Reject Controls */}
            {!rejecting ? (
              <div className="flex items-center gap-3 pt-4 border-t border-surface-150">
                <Button
                  variant="secondary"
                  className="flex-1"
                  onClick={() => setSelectedUser(null)}
                >
                  Close
                </Button>
                <Button
                  variant="danger"
                  className="flex-1 flex items-center justify-center gap-1.5"
                  onClick={() => setRejecting(true)}
                >
                  <UserX className="w-4 h-4" /> Reject KYC
                </Button>
                <Button
                  variant="success"
                  className="flex-1 flex items-center justify-center gap-1.5"
                  loading={actionLoading}
                  onClick={() => handleVerify('VERIFIED')}
                >
                  <UserCheck className="w-4 h-4" /> Approve KYC
                </Button>
              </div>
            ) : (
              <div className="space-y-4 pt-4 border-t border-surface-150">
                <Input
                  label="Rejection Reason"
                  placeholder="Explain why the KYC details were rejected (e.g. invalid document, name mismatch)..."
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  required
                  autoFocus
                />
                <div className="flex gap-3 justify-end">
                  <Button
                    variant="secondary"
                    onClick={() => {
                      setRejecting(false);
                      setRejectReason('');
                    }}
                    disabled={actionLoading}
                  >
                    Cancel
                  </Button>
                  <Button
                    variant="danger"
                    loading={actionLoading}
                    onClick={() => handleVerify('REJECTED')}
                  >
                    Submit Rejection
                  </Button>
                </div>
              </div>
            )}
          </div>
        </Modal>
      )}
    </div>
  );
}
