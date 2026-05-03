import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, UserPlus } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { userApi } from '@/api/userApi';
import { geographyApi } from '@/api/geographyApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { ROUTES } from '@/constants/routePaths';
import { ROLES, ROLE_DISPLAY_NAMES, ROLE_HIERARCHY } from '@/constants/roles';
import { GENDERS } from '@/constants/appConstants';
import { usePermission } from '@/hooks/usePermission';

const ROLE_LIST = Object.keys(ROLES).map(k => ({ value: ROLES[k], label: ROLE_DISPLAY_NAMES[ROLES[k]], level: ROLE_HIERARCHY[ROLES[k]] }));

export default function UserCreatePage() {
  const navigate = useNavigate();
  const { roleName: myRole } = usePermission();
  const [step, setStep] = useState(1);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '', gender: '', dateOfBirth: '', address: '', roleId: '', roleName: '', stateId: '', districtId: '', blockId: '', storeId: '' });
  const [roles, setRoles] = useState([]);
  const [states, setStates] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [blocks, setBlocks] = useState([]);
  const [stores, setStores] = useState([]);

  const upd = (k, v) => setForm(p => ({ ...p, [k]: v }));

  useEffect(() => { geographyApi.getActiveStates().then(r => { const d = r.data.data; setStates(d.content || d || []); }).catch(() => {}); }, []);

  useEffect(() => {
    const myLevel = ROLE_HIERARCHY[myRole] || 0;
    setRoles(ROLE_LIST.filter(r => r.level > myLevel));
  }, [myRole]);

  useEffect(() => {
    if (!form.stateId) { setDistricts([]); return; }
    geographyApi.getActiveDistricts({ stateId: form.stateId }).then(r => { const d = r.data.data; setDistricts(d.content || d || []); }).catch(() => {});
  }, [form.stateId]);

  useEffect(() => {
    if (!form.districtId) { setBlocks([]); return; }
    geographyApi.getActiveBlocks({ districtId: form.districtId }).then(r => { const d = r.data.data; setBlocks(d.content || d || []); }).catch(() => {});
  }, [form.districtId]);

  useEffect(() => {
    if (!form.blockId) { setStores([]); return; }
    geographyApi.getStores({ blockId: form.blockId, size: 100 }).then(r => { const d = r.data.data; setStores(d.content || d || []); }).catch(() => {});
  }, [form.blockId]);

  const needsGeo = (rn) => ![ROLES.SUPER_ADMIN, ROLES.SYSTEM_ADMIN].includes(rn);
  const needsStore = (rn) => [ROLES.RECEPTIONIST, ROLES.VOLUNTEER].includes(rn);

  const validateStep1 = () => {
    if (!form.fullName.trim() || !form.email.trim() || !form.phone.trim() || !form.password.trim()) { toast.warning('Please fill all required fields'); return false; }
    return true;
  };
  const validateStep2 = () => {
    if (!form.roleName) { toast.warning('Please select a role'); return false; }
    if (needsGeo(form.roleName) && !form.stateId) { toast.warning('Please select a state'); return false; }
    return true;
  };

  const handleSubmit = async () => {
    if (!validateStep2()) return;
    setSaving(true);
    try {
      const payload = { fullName: form.fullName.trim(), email: form.email.trim(), phone: form.phone.trim(), password: form.password, gender: form.gender || null, dateOfBirth: form.dateOfBirth || null, address: form.address.trim() || null, roleName: form.roleName, stateId: form.stateId ? Number(form.stateId) : null, districtId: form.districtId ? Number(form.districtId) : null, blockId: form.blockId ? Number(form.blockId) : null, storeId: form.storeId ? Number(form.storeId) : null };
      await userApi.createUser(payload);
      toast.success('User created successfully');
      navigate(ROUTES.USERS);
    } catch (error) { toast.error(getErrorMessage(error)); }
    finally { setSaving(false); }
  };

  const fullSelectCls = "w-full px-3.5 py-2.5 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500";

  return (
    <div className="animate-fade-in max-w-2xl mx-auto">
      <div className="mb-6">
        <button onClick={() => navigate(ROUTES.USERS)} className="flex items-center gap-2 text-sm text-surface-500 hover:text-primary-600 transition-colors cursor-pointer mb-4">
          <ArrowLeft className="w-4 h-4" /> Back to Users
        </button>
        <PageHeader title="Create User" description="Add a new user to the system" />
      </div>

      {/* Steps indicator */}
      <div className="flex items-center gap-2 mb-6">
        {[1, 2].map(s => (
          <div key={s} className="flex items-center gap-2">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold ${step >= s ? 'bg-primary-600 text-white' : 'bg-surface-200 text-surface-500'}`}>{s}</div>
            <span className={`text-sm font-medium ${step >= s ? 'text-surface-900' : 'text-surface-400'}`}>{s === 1 ? 'Personal Details' : 'Role & Geography'}</span>
            {s < 2 && <div className={`w-12 h-0.5 ${step > s ? 'bg-primary-600' : 'bg-surface-200'}`} />}
          </div>
        ))}
      </div>

      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card p-6">
        {step === 1 && (
          <div className="space-y-4">
            <Input label="Full Name *" placeholder="e.g., Ramesh Kumar" value={form.fullName} onChange={(e) => upd('fullName', e.target.value)} autoFocus />
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input label="Email *" type="email" placeholder="user@example.com" value={form.email} onChange={(e) => upd('email', e.target.value)} />
              <Input label="Phone *" placeholder="9876543210" value={form.phone} onChange={(e) => upd('phone', e.target.value)} />
            </div>
            <Input label="Temporary Password *" type="password" placeholder="Min 8 characters" value={form.password} onChange={(e) => upd('password', e.target.value)} />
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">Gender</label>
                <select value={form.gender} onChange={(e) => upd('gender', e.target.value)} className={fullSelectCls}>
                  <option value="">Select Gender</option>
                  {GENDERS.map(g => <option key={g.value} value={g.value}>{g.label}</option>)}
                </select></div>
              <Input label="Date of Birth" type="date" value={form.dateOfBirth} onChange={(e) => upd('dateOfBirth', e.target.value)} />
            </div>
            <Input label="Address" placeholder="Full address" value={form.address} onChange={(e) => upd('address', e.target.value)} />
            <div className="flex justify-end pt-2">
              <Button onClick={() => { if (validateStep1()) setStep(2); }}>Next Step →</Button>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="space-y-4">
            <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">Role *</label>
              <select value={form.roleName} onChange={(e) => { upd('roleName', e.target.value); upd('stateId', ''); upd('districtId', ''); upd('blockId', ''); upd('storeId', ''); }} className={fullSelectCls}>
                <option value="">Select Role</option>
                {roles.map(r => <option key={r.value} value={r.value}>{r.label}</option>)}
              </select></div>

            {form.roleName && needsGeo(form.roleName) && (
              <>
                <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">State *</label>
                  <select value={form.stateId} onChange={(e) => { upd('stateId', e.target.value); upd('districtId', ''); upd('blockId', ''); upd('storeId', ''); }} className={fullSelectCls}>
                    <option value="">Select State</option>{states.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                  </select></div>
                {form.stateId && form.roleName !== ROLES.STATE_ADMIN && (
                  <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">District</label>
                    <select value={form.districtId} onChange={(e) => { upd('districtId', e.target.value); upd('blockId', ''); upd('storeId', ''); }} className={fullSelectCls}>
                      <option value="">Select District</option>{districts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                    </select></div>
                )}
                {form.districtId && ![ROLES.STATE_ADMIN, ROLES.DISTRICT_ADMIN].includes(form.roleName) && (
                  <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">Block</label>
                    <select value={form.blockId} onChange={(e) => { upd('blockId', e.target.value); upd('storeId', ''); }} className={fullSelectCls}>
                      <option value="">Select Block</option>{blocks.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
                    </select></div>
                )}
                {form.blockId && needsStore(form.roleName) && (
                  <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">Store</label>
                    <select value={form.storeId} onChange={(e) => upd('storeId', e.target.value)} className={fullSelectCls}>
                      <option value="">Select Store</option>{stores.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                    </select></div>
                )}
              </>
            )}

            <div className="flex gap-3 pt-2">
              <Button variant="secondary" onClick={() => setStep(1)}>← Back</Button>
              <Button onClick={handleSubmit} loading={saving} className="flex-1"><UserPlus className="w-4 h-4" /> Create User</Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
