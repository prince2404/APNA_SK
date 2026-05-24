import React, { useState, useEffect } from 'react';
import { Search, Plus, Trash2, ShoppingCart, ShieldAlert, CreditCard, CheckCircle, FileText, Printer, ArrowLeft } from 'lucide-react';
import { patientApi } from '@/api/patientApi';
import { inventoryApi } from '@/api/inventoryApi';
import { billApi } from '@/api/billApi';
import { schemeApi } from '@/api/schemeApi';
import { useAuthStore } from '@/store/useAuthStore';
import { PageHeader } from '@/components/common/PageHeader';

export default function BillingPage() {
  const { user } = useAuthStore();
  const [patientSearch, setPatientSearch] = useState('');
  const [patientsList, setPatientsList] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);

  const [productSearch, setProductSearch] = useState('');
  const [productsList, setProductsList] = useState([]);
  const [selectedItems, setSelectedItems] = useState([]);
  const [schemes, setSchemes] = useState([]);

  // Payment
  const [paymentMode, setPaymentMode] = useState('CASH');
  const [loading, setLoading] = useState(false);
  const [createdBill, setCreatedBill] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchSchemes();
  }, []);

  const fetchSchemes = async () => {
    try {
      const res = await schemeApi.getSchemes();
      setSchemes(res.data.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handlePatientSearch = async (val) => {
    setPatientSearch(val);
    if (!val.trim()) {
      setPatientsList([]);
      return;
    }
    try {
      const res = await patientApi.getPatients({ search: val, page: 0, size: 5 });
      setPatientsList(res.data.data?.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleProductSearch = async (val) => {
    setProductSearch(val);
    if (!val.trim()) {
      setProductsList([]);
      return;
    }
    try {
      // Search stock in current store
      const res = await inventoryApi.getStoreStock({ 
        name: val, 
        storeId: user?.storeId || user?.store?.id, 
        page: 0, 
        size: 8 
      });
      setProductsList(res.data.data?.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const addItemToBill = (stock) => {
    const existing = selectedItems.find(item => item.product.id === stock.product.id && item.batchNumber === stock.batchNumber);
    if (existing) {
      if (existing.quantity >= stock.quantity) {
        alert('Cannot add more. Insufficient store stock.');
        return;
      }
      setSelectedItems(selectedItems.map(item => 
        (item.product.id === stock.product.id && item.batchNumber === stock.batchNumber) 
          ? { ...item, quantity: item.quantity + 1 }
          : item
      ));
    } else {
      setSelectedItems([...selectedItems, {
        product: stock.product,
        batchNumber: stock.batchNumber,
        availableStock: stock.quantity,
        quantity: 1
      }]);
    }
    setProductSearch('');
    setProductsList([]);
  };

  const updateItemQty = (index, val) => {
    const qty = Number(val);
    const item = selectedItems[index];
    if (qty > item.availableStock) {
      alert(`Only ${item.availableStock} units available in stock.`);
      return;
    }
    if (qty <= 0) return;
    const updated = [...selectedItems];
    updated[index].quantity = qty;
    setSelectedItems(updated);
  };

  const removeItem = (index) => {
    setSelectedItems(selectedItems.filter((_, i) => i !== index));
  };

  // Best scheme calculation per item
  const calculateItemDiscount = (item) => {
    if (!selectedPatient) return { discountVal: 0, schemeName: 'None' };
    let bestDiscount = 0;
    let bestSchemeName = 'None';

    const activeSchemes = schemes.filter(s => s.status === 'ACTIVE');

    for (const scheme of activeSchemes) {
      const categoryMatch = !scheme.categoryId || scheme.categoryId === item.product.category?.id;
      const stateMatch = !scheme.stateId || scheme.stateId === selectedPatient.stateId;

      if (categoryMatch && stateMatch) {
        let disc = 0;
        if (scheme.discountType === 'PERCENTAGE') {
          disc = item.product.askPrice * (scheme.discountValue / 100);
        } else {
          disc = scheme.discountValue;
        }
        if (disc > item.product.askPrice) {
          disc = item.product.askPrice;
        }
        if (disc > bestDiscount) {
          bestDiscount = disc;
          bestSchemeName = scheme.name;
        }
      }
    }
    return { discountVal: bestDiscount, schemeName: bestSchemeName };
  };

  // Billing Totals
  const calculateTotals = () => {
    let mrpTotal = 0;
    let askTotal = 0;
    let discountTotal = 0;
    let netTotal = 0;
    let gstTotal = 0;

    selectedItems.forEach(item => {
      const { discountVal } = calculateItemDiscount(item);
      const qty = item.quantity;
      const mrp = item.product.mrp;
      const askPrice = item.product.askPrice;

      mrpTotal += mrp * qty;
      askTotal += askPrice * qty;
      
      const itemDisc = discountVal * qty;
      discountTotal += itemDisc;

      const subtotal = (askPrice - discountVal) * qty;
      netTotal += subtotal;

      // Inclusive GST extraction
      const gstPercent = item.product.gstPercentage || 0;
      const itemGst = subtotal * (gstPercent / (100 + gstPercent));
      gstTotal += itemGst;
    });

    return {
      mrpTotal,
      askTotal,
      discountTotal,
      netTotal,
      gstTotal,
      savingsTotal: mrpTotal - netTotal
    };
  };

  const totals = calculateTotals();

  const handleCheckout = async () => {
    if (!selectedPatient) {
      setError('Please select a patient.');
      return;
    }
    if (selectedItems.length === 0) {
      setError('Please add at least one product.');
      return;
    }
    setLoading(true);
    setError('');

    const payload = {
      patientId: selectedPatient.id,
      paymentMode,
      items: selectedItems.map(item => ({
        productId: item.product.id,
        batchNumber: item.batchNumber,
        quantity: item.quantity
      }))
    };

    try {
      const res = await billApi.createBill(payload);
      setCreatedBill(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Checkout failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadPdf = async () => {
    if (!createdBill) return;
    try {
      const res = await billApi.downloadBillPdf(createdBill.id);
      const blob = new Blob([res.data], { type: 'application/pdf' });
      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = `invoice-${createdBill.billNumber}.pdf`;
      link.click();
    } catch (err) {
      console.error(err);
    }
  };

  const resetPos = () => {
    setSelectedPatient(null);
    setPatientSearch('');
    setSelectedItems([]);
    setPaymentMode('CASH');
    setCreatedBill(null);
    setError('');
  };

  if (createdBill) {
    return (
      <div className="animate-fade-in max-w-lg mx-auto p-6 bg-white rounded-2xl border border-surface-200 shadow-xl mt-12 text-center space-y-6">
        <div className="w-16 h-16 rounded-full bg-green-50 flex items-center justify-center text-green-600 mx-auto">
          <CheckCircle className="w-10 h-10" />
        </div>
        <div>
          <h2 className="text-xl font-bold text-surface-900">Checkout Successful</h2>
          <p className="text-sm text-surface-500 mt-1">Invoice generated: <strong>{createdBill.billNumber}</strong></p>
        </div>

        <div className="bg-surface-50 rounded-xl p-4 border border-surface-150 text-left text-sm space-y-2">
          <div className="flex justify-between">
            <span className="text-surface-500">Patient Name:</span>
            <span className="font-semibold text-surface-900">{selectedPatient.fullName}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-surface-500">Net Payable:</span>
            <span className="font-bold text-primary-600">₹{createdBill.netAmount?.toFixed(2)}</span>
          </div>
        </div>

        <div className="flex gap-4 pt-4 border-t border-surface-100">
          <button 
            onClick={resetPos}
            className="flex-1 py-2.5 border border-surface-300 rounded-xl text-sm font-semibold hover:bg-surface-50 cursor-pointer"
          >
            New Sale
          </button>
          <button 
            onClick={handleDownloadPdf}
            className="flex-1 py-2.5 bg-primary-600 hover:bg-primary-700 text-white rounded-xl text-sm font-semibold flex items-center justify-center gap-2 cursor-pointer shadow-lg shadow-primary-600/15"
          >
            <Printer className="w-4 h-4" /> Print Receipt
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in p-6 grid grid-cols-1 lg:grid-cols-3 gap-6">
      {/* Left Column: POS terminal */}
      <div className="lg:col-span-2 space-y-6">
        <PageHeader title="POS Billing Desk" description="Select patient and scan products to checkout" />

        {/* Patient Selection Card */}
        <div className="bg-white rounded-2xl border border-surface-200/60 p-5 shadow-card space-y-4">
          <h3 className="text-sm font-bold text-surface-900">1. Customer / Patient Details</h3>
          
          {!selectedPatient ? (
            <div className="relative">
              <Search className="absolute left-3 top-3.5 w-4 h-4 text-surface-400" />
              <input 
                type="text" 
                placeholder="Search patient by name or phone..." 
                value={patientSearch}
                onChange={(e) => handlePatientSearch(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 border border-surface-200 rounded-xl text-sm focus:ring-primary-500"
              />
              
              {patientsList.length > 0 && (
                <div className="absolute left-0 right-0 mt-1 bg-white border border-surface-200 rounded-xl shadow-lg z-20 overflow-hidden divide-y divide-surface-100">
                  {patientsList.map(p => (
                    <div 
                      key={p.id}
                      onClick={() => { setSelectedPatient(p); setPatientsList([]); }}
                      className="p-3 hover:bg-surface-50 cursor-pointer text-sm flex justify-between items-center"
                    >
                      <div>
                        <span className="font-semibold text-surface-900">{p.fullName}</span>
                        <span className="text-xs text-surface-400 ml-2">({p.gender}, {p.age} yrs)</span>
                      </div>
                      <span className="text-xs text-surface-500 font-semibold">{p.phone}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <div className="p-4 bg-primary-50/30 rounded-xl border border-primary-100 flex justify-between items-center">
              <div>
                <div className="text-sm font-bold text-surface-900">{selectedPatient.fullName}</div>
                <div className="text-xs text-surface-500">Phone: {selectedPatient.phone} • State: {selectedPatient.stateName}</div>
              </div>
              <button 
                onClick={() => setSelectedPatient(null)} 
                className="text-xs text-red-600 hover:text-red-700 font-semibold cursor-pointer"
              >
                Change Patient
              </button>
            </div>
          )}
        </div>

        {/* Product Scan / Add Card */}
        <div className="bg-white rounded-2xl border border-surface-200/60 p-5 shadow-card space-y-4">
          <h3 className="text-sm font-bold text-surface-900">2. Add Medicines / Products</h3>
          
          <div className="relative">
            <Search className="absolute left-3 top-3.5 w-4 h-4 text-surface-400" />
            <input 
              type="text" 
              placeholder="Search product from store inventory..." 
              value={productSearch}
              onChange={(e) => handleProductSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 border border-surface-200 rounded-xl text-sm focus:ring-primary-500"
            />

            {productsList.length > 0 && (
              <div className="absolute left-0 right-0 mt-1 bg-white border border-surface-200 rounded-xl shadow-lg z-20 overflow-hidden divide-y divide-surface-100 max-h-60 overflow-y-auto">
                {productsList.map(stock => (
                  <div 
                    key={`${stock.product.id}-${stock.batchNumber}`}
                    onClick={() => addItemToBill(stock)}
                    className="p-3 hover:bg-surface-50 cursor-pointer text-sm flex justify-between items-center"
                  >
                    <div>
                      <span className="font-semibold text-surface-900">{stock.product.name}</span>
                      <span className="text-xs text-surface-500 ml-2">Batch: {stock.batchNumber} • Exp: {stock.expiryDate}</span>
                    </div>
                    <div className="text-right">
                      <div className="font-bold text-primary-600">₹{stock.product.askPrice}</div>
                      <div className="text-[10px] text-surface-400">Stock: {stock.quantity} left</div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Cart Table */}
          {selectedItems.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-surface-200">
                <thead>
                  <tr className="border-b border-surface-200">
                    <th className="py-2 text-left text-xs font-semibold text-surface-500 uppercase">Product</th>
                    <th className="py-2 text-left text-xs font-semibold text-surface-500 uppercase">Batch</th>
                    <th className="py-2 text-center text-xs font-semibold text-surface-500 uppercase">Qty</th>
                    <th className="py-2 text-right text-xs font-semibold text-surface-500 uppercase">Discount</th>
                    <th className="py-2 text-right text-xs font-semibold text-surface-500 uppercase">Subtotal</th>
                    <th className="py-2 text-right text-xs font-semibold text-surface-500 uppercase"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-200 bg-white">
                  {selectedItems.map((item, idx) => {
                    const { discountVal, schemeName } = calculateItemDiscount(item);
                    const subtotal = (item.product.askPrice - discountVal) * item.quantity;
                    return (
                      <tr key={idx} className="align-middle">
                        <td className="py-3">
                          <div className="text-xs font-bold text-surface-900">{item.product.name}</div>
                          <div className="text-[10px] text-surface-400">ASK Price: ₹{item.product.askPrice} • MRP: ₹{item.product.mrp}</div>
                        </td>
                        <td className="py-3 text-xs text-surface-600">{item.batchNumber}</td>
                        <td className="py-3 text-center">
                          <input 
                            type="number" 
                            min="1" 
                            max={item.availableStock}
                            value={item.quantity}
                            onChange={(e) => updateItemQty(idx, e.target.value)}
                            className="w-12 text-center border border-surface-300 rounded text-xs p-1"
                          />
                        </td>
                        <td className="py-3 text-right text-xs text-surface-600">
                          <div className="font-semibold text-green-600">₹{(discountVal * item.quantity).toFixed(2)}</div>
                          <div className="text-[9px] text-surface-400">{schemeName}</div>
                        </td>
                        <td className="py-3 text-right text-xs font-bold text-surface-900">
                          ₹{subtotal.toFixed(2)}
                        </td>
                        <td className="py-3 text-right">
                          <button onClick={() => removeItem(idx)} className="p-1 text-surface-400 hover:text-red-600 cursor-pointer">
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-12 text-xs text-surface-400 flex flex-col items-center justify-center gap-2">
              <ShoppingCart className="w-8 h-8 text-surface-300" />
              Your POS terminal cart is empty. Scan products to add.
            </div>
          )}
        </div>
      </div>

      {/* Right Column: Checkout & Totals Summary */}
      <div className="space-y-6">
        <div className="bg-white rounded-2xl border border-surface-200/60 p-5 shadow-card space-y-6 sticky top-6">
          <h3 className="text-sm font-bold text-surface-900">Checkout Summary</h3>

          <div className="space-y-3 border-b border-surface-150 pb-4 text-sm text-surface-600">
            <div className="flex justify-between">
              <span>Total MRP:</span>
              <span className="font-semibold">₹{totals.mrpTotal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between">
              <span>Total Ask Price:</span>
              <span className="font-semibold">₹{totals.askTotal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between">
              <span>Active Promotion Discount:</span>
              <span className="font-semibold text-green-600">-₹{totals.discountTotal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-xs text-surface-400">
              <span>GST Included:</span>
              <span>₹{totals.gstTotal.toFixed(2)}</span>
            </div>
          </div>

          <div className="space-y-2">
            <div className="flex justify-between items-baseline">
              <span className="text-sm font-bold text-surface-900">Net Payable:</span>
              <span className="text-2xl font-black text-primary-600">₹{totals.netTotal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center text-xs bg-green-50 text-green-700 px-3 py-1.5 rounded-lg border border-green-100 font-semibold">
              <span>Total Savings:</span>
              <span>₹{totals.savingsTotal.toFixed(2)}</span>
            </div>
          </div>

          {/* Payment Mode Selection */}
          <div className="space-y-2 pt-4 border-t border-surface-150">
            <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Payment Mode</label>
            <div className="grid grid-cols-3 gap-2">
              {['CASH', 'UPI', 'CARD'].map(mode => (
                <button
                  key={mode}
                  type="button"
                  onClick={() => setPaymentMode(mode)}
                  className={`py-2 rounded-xl text-xs font-bold border transition-all cursor-pointer ${
                    paymentMode === mode 
                      ? 'bg-primary-600 border-primary-600 text-white shadow-md' 
                      : 'border-surface-200 text-surface-600 hover:bg-surface-50'
                  }`}
                >
                  {mode}
                </button>
              ))}
            </div>
          </div>

          {error && (
            <div className="p-3 bg-red-50 text-red-700 rounded-lg text-xs flex items-center gap-2 border border-red-100">
              <ShieldAlert className="w-4 h-4 shrink-0" />
              <div>{error}</div>
            </div>
          )}

          <button 
            onClick={handleCheckout}
            disabled={loading || selectedItems.length === 0 || !selectedPatient}
            className="w-full py-3 bg-primary-600 hover:bg-primary-700 disabled:bg-surface-200 disabled:text-surface-400 text-white rounded-xl text-sm font-bold shadow-lg shadow-primary-600/15 cursor-pointer flex items-center justify-center gap-2"
          >
            {loading ? 'Processing Checkout...' : 'Confirm checkout & print'}
          </button>
        </div>
      </div>
    </div>
  );
}
