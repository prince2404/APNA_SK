import React, { useState, useEffect } from 'react';
import { Building2, Plus, Edit2, ShieldAlert, CheckCircle2, XCircle } from 'lucide-react';
import { hospitalApi } from '@/api/hospitalApi';
import { geographyApi } from '@/api/geographyApi';
import { useAuthStore } from '@/store/useAuthStore';
import { PageHeader } from '@/components/common/PageHeader';

export default function HospitalsPage() {
  const { user } = useAuthStore();
  const [hospitals, setHospitals] = useState([]);
  const [states, setStates] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState(null);

  // Form State
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [phone, setPhone] = useState('');
  const [contactPerson, setContactPerson] = useState('');
  const [stateId, setStateId] = useState('');
  const [districtId, setDistrictId] = useState('');
  const [error, setError] = useState('');

  const isAdmin = ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN'].includes(user?.roleName);

  useEffect(() => {
    fetchHospitals();
    if (isAdmin) {
      fetchStates();
    }
  }, []);

  const fetchHospitals = async () => {
    setLoading(true);
    try {
      const res = await hospitalApi.getHospitals({ page: 0, size: 100 });
      setHospitals(res.data.data?.content || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
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

  const handleStateChange = async (sId) => {
    setStateId(sId);
    setDistrictId('');
    if (!sId) {
      setDistricts([]);
      return;
    }
    try {
      const res = await geographyApi.getActiveDistrictsByState(sId, { page: 0, size: 100 });
      setDistricts(res.data.data?.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const payload = { name, address, phone, contactPerson, stateId: Number(stateId), districtId: Number(districtId) };
    try {
      if (editId) {
        await hospitalApi.updateHospital(editId, payload);
      } else {
        await hospitalApi.createHospital(payload);
      }
      setShowModal(false);
      resetForm();
      fetchHospitals();
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong');
    }
  };

  const handleEdit = (h) => {
    setEditId(h.id);
    setName(h.name);
    setAddress(h.address || '');
    setPhone(h.phone || '');
    setContactPerson(h.contactPerson || '');
    setStateId(h.stateId || '');
    handleStateChange(h.stateId).then(() => {
      setDistrictId(h.districtId || '');
    });
    setShowModal(true);
  };

  const handleToggle = async (id) => {
    try {
      await hospitalApi.toggleHospitalStatus(id);
      fetchHospitals();
    } catch (err) {
      console.error(err);
    }
  };

  const resetForm = () => {
    setEditId(null);
    setName('');
    setAddress('');
    setPhone('');
    setContactPerson('');
    setStateId('');
    setDistrictId('');
    setDistricts([]);
    setError('');
  };

  return (
    <div className="animate-fade-in p-6">
      <PageHeader 
        title="Hospital Partners" 
        description="Manage partner clinics and referral hospitals"
        action={isAdmin ? {
          label: 'Add Hospital',
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
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Hospital Name</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Location</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Contact Details</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Status</th>
                {isAdmin && <th className="px-6 py-3 text-right text-xs font-semibold text-surface-500 uppercase">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-200 bg-white">
              {hospitals.map((h) => (
                <tr key={h.id} className="hover:bg-surface-50/50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center text-primary-600">
                        <Building2 className="w-5 h-5" />
                      </div>
                      <div>
                        <div className="text-sm font-semibold text-surface-900">{h.name}</div>
                        <div className="text-xs text-surface-500">Contact: {h.contactPerson || 'N/A'}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-surface-600">
                    <div>{h.districtName}, {h.stateName}</div>
                    <div className="text-xs text-surface-400 max-w-xs truncate">{h.address}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-surface-600">
                    {h.phone || 'N/A'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${
                      h.status === 'ACTIVE' ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
                    }`}>
                      {h.status === 'ACTIVE' ? <CheckCircle2 className="w-3.5 h-3.5" /> : <XCircle className="w-3.5 h-3.5" />}
                      {h.status}
                    </span>
                  </td>
                  {isAdmin && (
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center justify-end gap-2">
                        <button onClick={() => handleEdit(h)} className="p-1 text-surface-400 hover:text-primary-600 cursor-pointer">
                          <Edit2 className="w-4 h-4" />
                        </button>
                        <button onClick={() => handleToggle(h.id)} className={`px-2.5 py-1 rounded text-xs font-semibold cursor-pointer ${
                          h.status === 'ACTIVE' ? 'bg-red-50 text-red-600 hover:bg-red-100' : 'bg-green-50 text-green-600 hover:bg-green-100'
                        }`}>
                          {h.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                        </button>
                      </div>
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
            <h3 className="text-lg font-bold text-surface-900 mb-4">{editId ? 'Edit Hospital' : 'Add New Hospital'}</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <div className="p-3 bg-red-50 text-red-700 rounded-lg text-sm flex items-center gap-2">
                  <ShieldAlert className="w-4 h-4" />
                  {error}
                </div>
              )}
              <div>
                <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Hospital Name *</label>
                <input type="text" required value={name} onChange={(e) => setName(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm focus:ring-primary-500 focus:border-primary-500" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">State *</label>
                  <select required value={stateId} onChange={(e) => handleStateChange(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="">Select State</option>
                    {states.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">District *</label>
                  <select required value={districtId} onChange={(e) => setDistrictId(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" disabled={!stateId}>
                    <option value="">Select District</option>
                    {districts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                  </select>
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Contact Person</label>
                <input type="text" value={contactPerson} onChange={(e) => setContactPerson(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
              </div>
              <div>
                <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Phone Number</label>
                <input type="text" value={phone} onChange={(e) => setPhone(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
              </div>
              <div>
                <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Address</label>
                <textarea value={address} onChange={(e) => setAddress(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm h-20" />
              </div>
              <div className="flex justify-end gap-3 pt-4 border-t border-surface-100">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 border border-surface-300 rounded-lg text-sm hover:bg-surface-50 cursor-pointer">Cancel</button>
                <button type="submit" className="px-4 py-2 bg-primary-600 text-white rounded-lg text-sm hover:bg-primary-700 cursor-pointer">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
