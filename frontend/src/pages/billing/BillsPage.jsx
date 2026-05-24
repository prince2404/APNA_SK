import React, { useState, useEffect } from 'react';
import { FileText, Search, Printer, ShieldAlert, CheckCircle2, XCircle, Eye } from 'lucide-react';
import { billApi } from '@/api/billApi';
import { useAuthStore } from '@/store/useAuthStore';
import { PageHeader } from '@/components/common/PageHeader';

export default function BillsPage() {
  const { user } = useAuthStore();
  const [bills, setBills] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedBill, setSelectedBill] = useState(null);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelReason, setCancelReason] = useState('');
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchBills();
  }, [search]);

  const fetchBills = async () => {
    setLoading(true);
    try {
      const params = { page: 0, size: 100 };
      if (search) params.billNumber = search;
      const res = await billApi.getBills(params);
      setBills(res.data.data?.content || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchBills();
  };

  const handlePrint = async (billId, billNum) => {
    try {
      const res = await billApi.downloadBillPdf(billId);
      const blob = new Blob([res.data], { type: 'application/pdf' });
      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = `invoice-${billNum}.pdf`;
      link.click();
    } catch (err) {
      console.error(err);
    }
  };

  const handleCancelBill = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const res = await billApi.cancelBill(selectedBill.id, cancelReason);
      setSelectedBill(res.data.data);
      setShowCancelModal(false);
      setCancelReason('');
      fetchBills();
      alert('Invoice cancelled and store inventory returned.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to cancel bill');
    }
  };

  const canCancel = (bill) => {
    if (!bill) return false;
    if (bill.status === 'CANCELLED') return false;
    // Standard role validation
    const canCancelRole = ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'RECEPTIONIST', 'VOLUNTEER'].includes(user?.roleName);
    if (!canCancelRole) return false;

    // Return window check (7 days default)
    const billDate = new Date(bill.billDate || bill.createdAt);
    const now = new Date();
    const diffTime = Math.abs(now - billDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 7;
  };

  return (
    <div className="animate-fade-in p-6">
      <PageHeader 
        title="Sales Invoices & Returns" 
        description="Search past transactions, print invoices and process returns"
      />

      {/* Search Bar */}
      <form onSubmit={handleSearch} className="relative flex-1 mb-6 max-w-md">
        <Search className="absolute left-3 top-3.5 w-4 h-4 text-surface-400" />
        <input 
          type="text" 
          placeholder="Search by invoice number..." 
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-10 pr-4 py-2.5 border border-surface-200 rounded-xl text-sm focus:ring-primary-500"
        />
      </form>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Bills List */}
        <div className="lg:col-span-2 space-y-4">
          {loading ? (
            <div className="flex justify-center items-center h-64">
              <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-600"></div>
            </div>
          ) : (
            <div className="bg-white rounded-2xl border border-surface-200/60 shadow-card overflow-hidden">
              <table className="min-w-full divide-y divide-surface-200">
                <thead className="bg-surface-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Invoice Number</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Customer</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Net Amount</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Status</th>
                    <th className="px-6 py-3 text-right text-xs font-semibold text-surface-500 uppercase">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-200 bg-white">
                  {bills.map((b) => (
                    <tr key={b.id} className={`hover:bg-surface-50/50 transition-colors ${
                      selectedBill?.id === b.id ? 'bg-primary-50/30' : ''
                    }`}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-surface-900">
                        {b.billNumber}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-semibold text-surface-900">{b.patientName}</div>
                        <div className="text-xs text-surface-500">{b.patientPhone}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-primary-600">
                        ₹{b.netAmount?.toFixed(2)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${
                          b.status === 'ACTIVE' ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
                        }`}>
                          {b.status === 'ACTIVE' ? <CheckCircle2 className="w-3.5 h-3.5" /> : <XCircle className="w-3.5 h-3.5" />}
                          {b.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <button 
                          onClick={() => setSelectedBill(b)}
                          className="flex items-center gap-1.5 ml-auto px-2.5 py-1.5 bg-surface-100 hover:bg-surface-200 text-surface-700 rounded-lg text-xs font-semibold cursor-pointer"
                        >
                          <Eye className="w-3.5 h-3.5" />
                          View Details
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Right Column: Invoice Details & Action Panel */}
        <div>
          {selectedBill ? (
            <div className="bg-white rounded-2xl border border-surface-200/60 p-5 shadow-card space-y-6">
              <div className="flex justify-between items-start border-b border-surface-150 pb-4">
                <div>
                  <h4 className="text-xs uppercase font-bold text-surface-400">Invoice Details</h4>
                  <h3 className="text-lg font-bold text-surface-900">{selectedBill.billNumber}</h3>
                  <p className="text-xs text-surface-500">{new Date(selectedBill.billDate || selectedBill.createdAt).toLocaleString()}</p>
                </div>
                <button 
                  onClick={() => handlePrint(selectedBill.id, selectedBill.billNumber)}
                  className="p-2 bg-surface-100 hover:bg-surface-200 rounded-xl text-surface-700 cursor-pointer"
                  title="Print Invoice"
                >
                  <Printer className="w-5 h-5" />
                </button>
              </div>

              <div className="space-y-4">
                <div className="text-xs text-surface-500 space-y-2">
                  <div className="flex justify-between">
                    <span>Patient Name:</span>
                    <span className="font-semibold text-surface-800">{selectedBill.patientName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Phone:</span>
                    <span className="font-semibold text-surface-800">{selectedBill.patientPhone}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Store:</span>
                    <span className="font-semibold text-surface-800">{selectedBill.storeName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Payment Mode:</span>
                    <span className="font-semibold text-surface-800">{selectedBill.paymentMode}</span>
                  </div>
                </div>

                <div className="border-t border-surface-150 pt-4 space-y-2">
                  <h4 className="text-xs font-bold text-surface-700">Items List</h4>
                  <div className="max-h-40 overflow-y-auto space-y-2 pr-1">
                    {selectedBill.items?.map((item, index) => (
                      <div key={index} className="text-xs flex justify-between">
                        <div>
                          <div className="font-bold text-surface-800">{item.productName}</div>
                          <div className="text-[10px] text-surface-400">Batch: {item.batchNumber} • Qty: {item.quantity}</div>
                        </div>
                        <span className="font-semibold text-surface-800">₹{item.subtotal?.toFixed(2)}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="border-t border-surface-150 pt-4 text-xs space-y-2 text-surface-500">
                  <div className="flex justify-between">
                    <span>Total MRP:</span>
                    <span>₹{selectedBill.totalMrp?.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Total Ask Price:</span>
                    <span>₹{selectedBill.totalAskPrice?.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Discount:</span>
                    <span className="text-green-600">-₹{selectedBill.totalDiscount?.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between font-bold text-sm text-surface-900 pt-2 border-t border-surface-100">
                    <span>Net Amount:</span>
                    <span className="text-primary-600">₹{selectedBill.netAmount?.toFixed(2)}</span>
                  </div>
                </div>

                {selectedBill.status === 'CANCELLED' && (
                  <div className="p-3 bg-red-50 text-red-700 rounded-xl text-xs space-y-1 border border-red-100">
                    <div className="font-bold">Cancellation Info:</div>
                    <div>Reason: {selectedBill.cancelReason}</div>
                    <div>Cancelled By: {selectedBill.cancelledByName}</div>
                  </div>
                )}

                {canCancel(selectedBill) && (
                  <button 
                    onClick={() => setShowCancelModal(true)}
                    className="w-full py-2.5 bg-red-50 hover:bg-red-100 text-red-600 rounded-xl text-xs font-bold transition-colors cursor-pointer border border-red-200"
                  >
                    Cancel Invoice & Return
                  </button>
                )}
              </div>
            </div>
          ) : (
            <div className="bg-surface-50 rounded-2xl border border-dashed border-surface-300 p-8 text-center text-xs text-surface-400">
              Select an invoice from the list to view billing items, print receipt, or process return.
            </div>
          )}
        </div>
      </div>

      {/* Cancel Modal */}
      {showCancelModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-2xl border border-surface-200 shadow-xl max-w-sm w-full p-6 animate-scale-in">
            <h3 className="text-lg font-bold text-surface-900 mb-2">Cancel Invoice</h3>
            <p className="text-xs text-surface-500 mb-4">
              This will mark the bill as CANCELLED, restore the quantities back to store inventory, and invalidate all associated upward commissions.
            </p>
            <form onSubmit={handleCancelBill} className="space-y-4">
              {error && (
                <div className="p-3 bg-red-50 text-red-700 rounded-lg text-xs flex items-center gap-2">
                  <ShieldAlert className="w-4 h-4" />
                  {error}
                </div>
              )}
              <div>
                <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Reason for Cancellation *</label>
                <textarea 
                  required 
                  value={cancelReason} 
                  onChange={(e) => setCancelReason(e.target.value)} 
                  placeholder="e.g. Medicine returned, wrong entry, incorrect patient"
                  className="w-full border border-surface-300 rounded-lg p-2.5 text-xs h-20" 
                />
              </div>
              <div className="flex justify-end gap-3 pt-4 border-t border-surface-100">
                <button type="button" onClick={() => setShowCancelModal(false)} className="px-4 py-2 border border-surface-300 rounded-lg text-xs hover:bg-surface-50 cursor-pointer">Close</button>
                <button type="submit" className="px-4 py-2 bg-red-600 text-white rounded-lg text-xs hover:bg-red-700 cursor-pointer">Confirm Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
