import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Heart, Search, Plus, Edit2, Upload, FileSpreadsheet, ShieldAlert, CreditCard } from 'lucide-react';
import { patientApi } from '@/api/patientApi';
import { hospitalApi } from '@/api/hospitalApi';
import { geographyApi } from '@/api/geographyApi';
import { healthCardApi } from '@/api/healthCardApi';
import { useAuthStore } from '@/store/useAuthStore';
import { PageHeader } from '@/components/common/PageHeader';
import { ROUTES } from '@/constants/routePaths';

export default function PatientsPage() {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [patients, setPatients] = useState([]);
  const [hospitals, setHospitals] = useState([]);
  const [states, setStates] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [blocks, setBlocks] = useState([]);
  const [stores, setStores] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState(null);

  // Search & Filter
  const [search, setSearch] = useState('');
  const [storeFilterId, setStoreFilterId] = useState('');

  // Form State
  const [fullName, setFullName] = useState('');
  const [age, setAge] = useState('');
  const [gender, setGender] = useState('MALE');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [stateId, setStateId] = useState('');
  const [districtId, setDistrictId] = useState('');
  const [blockId, setBlockId] = useState('');
  const [storeId, setStoreId] = useState('');
  const [hospitalId, setHospitalId] = useState('');
  const [messagingPref, setMessagingPref] = useState('ALL');
  const [error, setError] = useState('');

  const isStoreUser = !!user?.storeId || !!user?.store?.id;

  useEffect(() => {
    fetchPatients();
    fetchHospitals();
    fetchGeography();
  }, [search, storeFilterId]);

  const fetchPatients = async () => {
    setLoading(true);
    try {
      const params = { page: 0, size: 100 };
      if (search) params.search = search;
      if (storeFilterId) params.storeId = storeFilterId;
      const res = await patientApi.getPatients(params);
      setPatients(res.data?.content || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchHospitals = async () => {
    try {
      const res = await hospitalApi.getHospitals({ page: 0, size: 100 });
      setHospitals(res.data?.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchGeography = async () => {
    try {
      const res = await geographyApi.getActiveStates({ page: 0, size: 100 });
      setStates(res.data?.content || []);
      if (isStoreUser) {
        // If receptionist/volunteer, geographic details can be locked.
        const sId = user?.store?.block?.district?.state?.id || user?.store?.block?.district?.stateId;
        const dId = user?.store?.block?.district?.id || user?.store?.block?.districtId;
        const bId = user?.store?.block?.id || user?.store?.blockId;
        const stId = user?.store?.id || user?.storeId;
        
        setStateId(sId || '');
        setDistrictId(dId || '');
        setBlockId(bId || '');
        setStoreId(stId || '');
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleStateChange = async (sId) => {
    setStateId(sId);
    setDistrictId('');
    setBlockId('');
    setStoreId('');
    setDistricts([]);
    setBlocks([]);
    setStores([]);
    if (!sId) return;

    try {
      const res = await geographyApi.getActiveDistrictsByState(sId, { page: 0, size: 100 });
      setDistricts(res.data?.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleDistrictChange = async (dId) => {
    setDistrictId(dId);
    setBlockId('');
    setStoreId('');
    setBlocks([]);
    setStores([]);
    if (!dId) return;

    try {
      const res = await geographyApi.getActiveBlocksByDistrict(dId, { page: 0, size: 100 });
      setBlocks(res.data?.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleBlockChange = async (bId) => {
    setBlockId(bId);
    setStoreId('');
    setStores([]);
    if (!bId) return;

    try {
      const res = await geographyApi.getStoresByBlock(bId, { page: 0, size: 100 });
      setStores(res.data?.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const payload = {
      fullName,
      age: age ? Number(age) : null,
      gender,
      phone,
      email: email || null,
      address: address || null,
      stateId: Number(stateId),
      districtId: Number(districtId),
      blockId: Number(blockId),
      storeId: storeId ? Number(storeId) : null,
      hospitalId: hospitalId ? Number(hospitalId) : null,
      messagingPref
    };

    try {
      if (editId) {
        await patientApi.updatePatient(editId, payload);
      } else {
        await patientApi.registerPatient(payload);
      }
      setShowModal(false);
      resetForm();
      fetchPatients();
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong');
    }
  };

  const handleEdit = (p) => {
    setEditId(p.id);
    setFullName(p.fullName);
    setAge(p.age || '');
    setGender(p.gender);
    setPhone(p.phone);
    setEmail(p.email || '');
    setAddress(p.address || '');
    setMessagingPref(p.messagingPref || 'ALL');
    setHospitalId(p.hospitalId || '');
    
    // Load dropdown hierarchies
    setStateId(p.stateId || '');
    geographyApi.getActiveDistrictsByState(p.stateId, { page: 0, size: 100 }).then(dRes => {
      setDistricts(dRes.data?.content || []);
      setDistrictId(p.districtId || '');
      return geographyApi.getActiveBlocksByDistrict(p.districtId, { page: 0, size: 100 });
    }).then(bRes => {
      setBlocks(bRes.data?.content || []);
      setBlockId(p.blockId || '');
      return geographyApi.getStoresByBlock(p.blockId, { page: 0, size: 100 });
    }).then(sRes => {
      setStores(sRes.data?.content || []);
      setStoreId(p.storeId || '');
    }).catch(err => console.error(err));

    setShowModal(true);
  };

  const handleIssueHealthCard = async (pId) => {
    try {
      await healthCardApi.issueHealthCard({ patientId: pId });
      alert('Health card issued successfully!');
      fetchPatients();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to issue health card');
    }
  };

  const resetForm = () => {
    setEditId(null);
    setFullName('');
    setAge('');
    setGender('MALE');
    setPhone('');
    setEmail('');
    setAddress('');
    setHospitalId('');
    setMessagingPref('ALL');
    setError('');
    
    if (!isStoreUser) {
      setStateId('');
      setDistrictId('');
      setBlockId('');
      setStoreId('');
      setDistricts([]);
      setBlocks([]);
      setStores([]);
    }
  };

  return (
    <div className="animate-fade-in p-6">
      <PageHeader 
        title="Patients Profiles" 
        description="Search, view and register patients"
        action={{
          label: 'Register Patient',
          icon: Plus,
          onClick: () => { resetForm(); setShowModal(true); }
        }}
      />

      {/* Control bar */}
      <div className="flex flex-col sm:flex-row gap-4 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-3.5 w-4 h-4 text-surface-400" />
          <input 
            type="text" 
            placeholder="Search by name or phone..." 
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 border border-surface-200 rounded-xl text-sm focus:ring-primary-500"
          />
        </div>
        <button 
          onClick={() => navigate(ROUTES.PATIENTS_BULK_UPLOAD)}
          className="flex items-center justify-center gap-2 px-4 py-2.5 bg-surface-100 hover:bg-surface-200 text-surface-700 rounded-xl text-sm font-semibold cursor-pointer"
        >
          <Upload className="w-4 h-4" />
          Bulk CSV Import
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-600"></div>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-surface-200/60 shadow-card overflow-hidden">
          <table className="min-w-full divide-y divide-surface-200">
            <thead className="bg-surface-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Patient</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Contact</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Geography</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Hospital Partner</th>
                <th className="px-6 py-3 text-right text-xs font-semibold text-surface-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-200 bg-white">
              {patients.map((p) => (
                <tr key={p.id} className="hover:bg-surface-50/50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-full bg-rose-50 flex items-center justify-center text-rose-600 font-bold text-sm">
                        {p.fullName[0].toUpperCase()}
                      </div>
                      <div>
                        <div className="text-sm font-semibold text-surface-900">{p.fullName}</div>
                        <div className="text-xs text-surface-500">{p.gender}, {p.age || 'N/A'} yrs</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-surface-600">
                    <div>{p.phone}</div>
                    <div className="text-xs text-surface-400">{p.email || 'No Email'}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-surface-600">
                    <div>{p.blockName}, {p.districtName}</div>
                    <div className="text-xs text-surface-400">Store: {p.storeName || 'N/A'}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-surface-500">
                    {p.hospitalName || 'None'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex items-center justify-end gap-2">
                      <button 
                        onClick={() => handleIssueHealthCard(p.id)}
                        className="flex items-center gap-1 px-2.5 py-1 bg-primary-50 text-primary-700 hover:bg-primary-100 rounded text-xs font-semibold cursor-pointer"
                        title="Issue Digital Health Card"
                      >
                        <CreditCard className="w-3.5 h-3.5" />
                        Issue Card
                      </button>
                      <button onClick={() => handleEdit(p)} className="p-1 text-surface-400 hover:text-primary-600 cursor-pointer">
                        <Edit2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-2xl border border-surface-200 shadow-xl max-w-lg w-full p-6 animate-scale-in max-h-[90vh] overflow-y-auto">
            <h3 className="text-lg font-bold text-surface-900 mb-4">{editId ? 'Edit Patient Profile' : 'Register Patient'}</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <div className="p-3 bg-red-50 text-red-700 rounded-lg text-sm flex items-center gap-2">
                  <ShieldAlert className="w-4 h-4" />
                  {error}
                </div>
              )}
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Full Name *</label>
                  <input type="text" required value={fullName} onChange={(e) => setFullName(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Phone Number *</label>
                  <input type="text" required pattern="[0-9]{10,12}" value={phone} onChange={(e) => setPhone(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Age</label>
                  <input type="number" min="0" max="120" value={age} onChange={(e) => setAge(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Gender *</label>
                  <select value={gender} onChange={(e) => setGender(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Messaging Preference</label>
                  <select value={messagingPref} onChange={(e) => setMessagingPref(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="ALL">All</option>
                    <option value="SMS">SMS</option>
                    <option value="EMAIL">Email</option>
                    <option value="WHATSAPP">WhatsApp</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Email</label>
                <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
              </div>

              {/* Geographic Dropdowns */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">State *</label>
                  <select required value={stateId} onChange={(e) => handleStateChange(e.target.value)} disabled={isStoreUser} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="">Select State</option>
                    {states.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">District *</label>
                  <select required value={districtId} onChange={(e) => handleDistrictChange(e.target.value)} disabled={isStoreUser || !stateId} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="">Select District</option>
                    {districts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Block *</label>
                  <select required value={blockId} onChange={(e) => handleBlockChange(e.target.value)} disabled={isStoreUser || !districtId} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="">Select Block</option>
                    {blocks.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Store / Centre</label>
                  <select value={storeId} onChange={(e) => setStoreId(e.target.value)} disabled={isStoreUser || !blockId} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="">Select Store</option>
                    {stores.map(st => <option key={st.id} value={st.id}>{st.name}</option>)}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Hospital Partner Partner</label>
                  <select value={hospitalId} onChange={(e) => setHospitalId(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="">None</option>
                    {hospitals.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Residential Address</label>
                <textarea value={address} onChange={(e) => setAddress(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm h-16" />
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-surface-100">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 border border-surface-300 rounded-lg text-sm hover:bg-surface-50 cursor-pointer">Cancel</button>
                <button type="submit" className="px-4 py-2 bg-rose-600 text-white rounded-lg text-sm hover:bg-rose-700 cursor-pointer">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
