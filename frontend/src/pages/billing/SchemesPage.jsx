import React, { useState, useEffect } from 'react';
import { Percent, Plus, ShieldAlert, CheckCircle2, XCircle } from 'lucide-react';
import { schemeApi } from '@/api/schemeApi';
import { productApi } from '@/api/productApi';
import { geographyApi } from '@/api/geographyApi';
import { useAuthStore } from '@/store/useAuthStore';
import { PageHeader } from '@/components/common/PageHeader';

export default function SchemesPage() {
  const { user } = useAuthStore();
  const [schemes, setSchemes] = useState([]);
  const [categories, setCategories] = useState([]);
  const [states, setStates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);

  // Form state
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [discountType, setDiscountType] = useState('PERCENTAGE');
  const [discountValue, setDiscountValue] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [stateId, setStateId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [error, setError] = useState('');

  const isAdmin = ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN'].includes(user?.roleName);

  useEffect(() => {
    fetchSchemes();
    if (isAdmin) {
      fetchCategories();
      fetchStates();
    }
  }, []);

  const fetchSchemes = async () => {
    setLoading(true);
    try {
      const res = await schemeApi.getSchemes();
      setSchemes(res.data.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const res = await productApi.getCategories();
      setCategories(res.data.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchStates = async () => {
    try {
      const res = await geographyApi.getActiveStates({ page: 0, size: 100 });
      setStates(res.data.data?.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    if (new Date(startDate) > new Date(endDate)) {
      setError('Start date cannot be after end date.');
      return;
    }

    const payload = {
      name,
      description,
      discountType,
      discountValue: Number(discountValue),
      categoryId: categoryId ? Number(categoryId) : null,
      stateId: stateId ? Number(stateId) : null,
      startDate,
      endDate
    };

    try {
      await schemeApi.createScheme(payload);
      setShowModal(false);
      resetForm();
      fetchSchemes();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create scheme');
    }
  };

  const handleToggle = async (id) => {
    try {
      await schemeApi.toggleSchemeStatus(id);
      fetchSchemes();
    } catch (err) {
      console.error(err);
    }
  };

  const resetForm = () => {
    setName('');
    setDescription('');
    setDiscountType('PERCENTAGE');
    setDiscountValue('');
    setCategoryId('');
    setStateId('');
    setStartDate('');
    setEndDate('');
    setError('');
  };

  return (
    <div className="animate-fade-in p-6">
      <PageHeader 
        title="Promotions & Discount Schemes" 
        description="Configure category-wide and region-wide patient discounts"
        action={isAdmin ? {
          label: 'Create Scheme',
          icon: Plus,
          onClick: () => { resetForm(); setShowModal(true); }
        } : null}
      />

      {loading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-600"></div>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-surface-200/60 shadow-card overflow-hidden">
          <table className="min-w-full divide-y divide-surface-200">
            <thead className="bg-surface-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Scheme Name</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Discount</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Applicability</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Duration</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Status</th>
                {isAdmin && <th className="px-6 py-3 text-right text-xs font-semibold text-surface-500 uppercase">Action</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-200 bg-white">
              {schemes.map((s) => (
                <tr key={s.id} className="hover:bg-surface-50/50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-lg bg-pink-50 flex items-center justify-center text-pink-600">
                        <Percent className="w-5 h-5" />
                      </div>
                      <div>
                        <div className="text-sm font-semibold text-surface-900">{s.name}</div>
                        <div className="text-xs text-surface-500 max-w-xs truncate">{s.description || 'No description'}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-surface-900">
                    {s.discountType === 'PERCENTAGE' ? `${s.discountValue}% Off` : `Flat ₹${s.discountValue} Off`}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-surface-600">
                    <div>Category: {s.categoryName || 'All Categories'}</div>
                    <div className="text-xs text-surface-400">State: {s.stateName || 'All India'}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-surface-600">
                    <div className="text-xs font-semibold">{s.startDate} to {s.endDate}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${
                      s.status === 'ACTIVE' ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
                    }`}>
                      {s.status === 'ACTIVE' ? <CheckCircle2 className="w-3.5 h-3.5" /> : <XCircle className="w-3.5 h-3.5" />}
                      {s.status}
                    </span>
                  </td>
                  {isAdmin && (
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button 
                        onClick={() => handleToggle(s.id)}
                        className={`px-2.5 py-1 rounded text-xs font-semibold cursor-pointer ${
                          s.status === 'ACTIVE' ? 'bg-red-50 text-red-600 hover:bg-red-100' : 'bg-green-50 text-green-600 hover:bg-green-100'
                        }`}
                      >
                        {s.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-2xl border border-surface-200 shadow-xl max-w-md w-full p-6 animate-scale-in">
            <h3 className="text-lg font-bold text-surface-900 mb-4">Create Scheme</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <div className="p-3 bg-red-50 text-red-700 rounded-lg text-sm flex items-center gap-2">
                  <ShieldAlert className="w-4 h-4" />
                  {error}
                </div>
              )}
              <div>
                <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Scheme Name *</label>
                <input type="text" required value={name} onChange={(e) => setName(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
              </div>
              <div>
                <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Description</label>
                <input type="text" value={description} onChange={(e) => setDescription(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Discount Type *</label>
                  <select value={discountType} onChange={(e) => setDiscountType(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="PERCENTAGE">Percentage (%)</option>
                    <option value="FLAT">Flat (₹)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Discount Value *</label>
                  <input type="number" required step="0.01" min="0.01" value={discountValue} onChange={(e) => setDiscountValue(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Product Category</label>
                  <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="">All Categories</option>
                    {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Target State</label>
                  <select value={stateId} onChange={(e) => setStateId(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="">All India</option>
                    {states.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Start Date *</label>
                  <input type="date" required value={startDate} onChange={(e) => setStartDate(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">End Date *</label>
                  <input type="date" required value={endDate} onChange={(e) => setEndDate(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
                </div>
              </div>
              <div className="flex justify-end gap-3 pt-4 border-t border-surface-100">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 border border-surface-300 rounded-lg text-sm hover:bg-surface-50 cursor-pointer">Cancel</button>
                <button type="submit" className="px-4 py-2 bg-primary-600 text-white rounded-lg text-sm hover:bg-primary-700 cursor-pointer">Create</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
