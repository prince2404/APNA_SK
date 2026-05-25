import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Edit3, Save, Shield, MapPin } from 'lucide-react';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { StatusBadge } from '@/components/common/StatusBadge';
import { Badge } from '@/components/common/Badge';
import { Loader } from '@/components/common/Loader';
import { userApi } from '@/api/userApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { ROUTES } from '@/constants/routePaths';
import { ROLE_DISPLAY_NAMES } from '@/constants/roles';
import { usePermission } from '@/hooks/usePermission';

export default function UserDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isSuperAdmin, hasPermission } = usePermission();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({});
  const [saving, setSaving] = useState(false);
  const [allPermissions, setAllPermissions] = useState([]);
  const [selectedPerms, setSelectedPerms] = useState(new Set());
  const [showPerms, setShowPerms] = useState(false);
  const [savingPerms, setSavingPerms] = useState(false);

  useEffect(() => {
    setLoading(true);
    userApi.getUser(id).then(r => {
      const u = r.data.data;
      setUser(u);
      setForm({ fullName: u.fullName, email: u.email, phone: u.phone, address: u.address || '', gender: u.gender || '', dateOfBirth: u.dateOfBirth || '' });
    }).catch(e => { toast.error(getErrorMessage(e)); navigate(ROUTES.USERS); })
    .finally(() => setLoading(false));
  }, [id, navigate]);

  useEffect(() => {
    if (user && allPermissions.length > 0) {
      const userPermStrings = new Set(user.permissions || []);
      const matchedIds = allPermissions
        .filter(p => userPermStrings.has(p.code))
        .map(p => p.id);
      setSelectedPerms(new Set(matchedIds));
    }
  }, [user, allPermissions]);

  useEffect(() => {
    userApi.getPermissions().then(r => setAllPermissions(r.data.data || [])).catch(() => {});
  }, []);

  const upd = (k, v) => setForm(p => ({ ...p, [k]: v }));

  const handleSave = async () => {
    setSaving(true);
    try {
      const res = await userApi.updateUser(id, form);
      setUser(res.data.data);
      setEditing(false);
      toast.success('User updated');
    } catch (e) { toast.error(getErrorMessage(e)); }
    finally { setSaving(false); }
  };

  const handleSavePermissions = async () => {
    setSavingPerms(true);
    try {
      const res = await userApi.assignPermissions(id, { permissionIds: [...selectedPerms] });
      setUser(res.data.data);
      setShowPerms(false);
      toast.success('Permissions updated');
    } catch (e) { toast.error(getErrorMessage(e)); }
    finally { setSavingPerms(false); }
  };

  const togglePerm = (pid) => {
    setSelectedPerms(prev => {
      const next = new Set(prev);
      if (next.has(pid)) next.delete(pid); else next.add(pid);
      return next;
    });
  };

  if (loading) return <Loader text="Loading user details..." />;
  if (!user) return null;

  const getInitials = (name) => name?.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() || 'U';

  // Group permissions by module
  const permsByModule = {};
  allPermissions.forEach(p => {
    if (!permsByModule[p.module]) permsByModule[p.module] = [];
    permsByModule[p.module].push(p);
  });

  const fullSelectCls = "w-full px-3.5 py-2.5 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500";

  return (
    <div className="animate-fade-in max-w-3xl mx-auto">
      <button onClick={() => navigate(ROUTES.USERS)} className="flex items-center gap-2 text-sm text-surface-500 hover:text-primary-600 transition-colors cursor-pointer mb-4">
        <ArrowLeft className="w-4 h-4" /> Back to Users
      </button>

      {/* Profile Card */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden mb-6">
        <div className="bg-gradient-to-r from-primary-600 to-primary-800 px-6 py-5">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-xl bg-white/20 backdrop-blur-sm flex items-center justify-center text-xl font-bold text-white">
              {getInitials(user.fullName)}
            </div>
            <div className="text-white">
              <h1 className="text-xl font-bold">{user.fullName}</h1>
              <div className="flex items-center gap-3 mt-1">
                <Badge>{ROLE_DISPLAY_NAMES[user.roleName] || user.roleName}</Badge>
                <StatusBadge status={user.status} />
              </div>
            </div>
          </div>
        </div>

        <div className="p-6">
          {!editing ? (
            <div className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div><p className="text-xs text-surface-500 mb-1">Email</p><p className="text-sm font-medium">{user.email}</p></div>
                <div><p className="text-xs text-surface-500 mb-1">Phone</p><p className="text-sm font-medium">{user.phone || '—'}</p></div>
                <div><p className="text-xs text-surface-500 mb-1">Gender</p><p className="text-sm font-medium">{user.gender || '—'}</p></div>
                <div><p className="text-xs text-surface-500 mb-1">Date of Birth</p><p className="text-sm font-medium">{user.dateOfBirth || '—'}</p></div>
              </div>
              <div><p className="text-xs text-surface-500 mb-1">Address</p><p className="text-sm font-medium">{user.address || '—'}</p></div>
              <div className="flex items-center gap-2 pt-2">
                <MapPin className="w-4 h-4 text-surface-400" />
                <span className="text-sm text-surface-600">
                  {[user.stateName, user.districtName, user.blockName, user.storeName].filter(Boolean).join(' → ') || 'Platform-wide access'}
                </span>
              </div>
              <div className="flex gap-3 pt-2">
                {(isSuperAdmin || hasPermission('USERS:EDIT_USER')) && (
                  <Button variant="secondary" onClick={() => setEditing(true)}><Edit3 className="w-4 h-4" /> Edit</Button>
                )}
                {(isSuperAdmin || hasPermission('USERS:EDIT_USER')) && (
                  <Button variant="outline" onClick={() => setShowPerms(!showPerms)}><Shield className="w-4 h-4" /> {showPerms ? 'Hide' : 'Manage'} Permissions</Button>
                )}
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              <Input label="Full Name" value={form.fullName} onChange={(e) => upd('fullName', e.target.value)} />
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input label="Email" type="email" value={form.email} onChange={(e) => upd('email', e.target.value)} />
                <Input label="Phone" value={form.phone} onChange={(e) => upd('phone', e.target.value)} />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">Gender</label>
                  <select value={form.gender} onChange={(e) => upd('gender', e.target.value)} className={fullSelectCls}>
                    <option value="">Select</option><option value="MALE">Male</option><option value="FEMALE">Female</option><option value="OTHER">Other</option>
                  </select></div>
                <Input label="Date of Birth" type="date" value={form.dateOfBirth} onChange={(e) => upd('dateOfBirth', e.target.value)} />
              </div>
              <Input label="Address" value={form.address} onChange={(e) => upd('address', e.target.value)} />
              <div className="flex gap-3 pt-2">
                <Button variant="secondary" onClick={() => setEditing(false)}>Cancel</Button>
                <Button onClick={handleSave} loading={saving}><Save className="w-4 h-4" /> Save Changes</Button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Permissions Panel */}
      {showPerms && (
        <div className="bg-white rounded-xl border border-surface-200/60 shadow-card p-6 animate-slide-up">
          <h3 className="text-lg font-semibold text-surface-900 mb-4 flex items-center gap-2"><Shield className="w-5 h-5 text-primary-600" /> Permission Management</h3>
          <div className="space-y-4">
            {Object.entries(permsByModule).map(([mod, perms]) => (
              <div key={mod} className="border border-surface-200 rounded-lg p-4">
                <h4 className="text-sm font-semibold text-surface-700 mb-2 uppercase tracking-wide">{mod}</h4>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                  {perms.map(p => (
                    <label key={p.id} className="flex items-center gap-2 text-sm cursor-pointer hover:bg-surface-50 px-2 py-1 rounded">
                      <input type="checkbox" checked={selectedPerms.has(p.id)} onChange={() => togglePerm(p.id)} className="rounded border-surface-300 text-primary-600 focus:ring-primary-500" />
                      <span className="text-surface-700">{p.description || p.action}</span>
                    </label>
                  ))}
                </div>
              </div>
            ))}
          </div>
          <div className="flex justify-end pt-4">
            <Button onClick={handleSavePermissions} loading={savingPerms}><Save className="w-4 h-4" /> Save Permissions</Button>
          </div>
        </div>
      )}
    </div>
  );
}
