import { useState, useEffect, useCallback } from 'react';
import { Store, Plus, Edit3, ToggleLeft, ToggleRight, MapPin } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Modal } from '@/components/common/Modal';
import { StatusBadge } from '@/components/common/StatusBadge';
import { EmptyState } from '@/components/common/EmptyState';
import { Pagination } from '@/components/common/Pagination';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { Loader } from '@/components/common/Loader';
import { geographyApi } from '@/api/geographyApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { cn } from '@/utils/cn';

export default function StoresPage() {
  const [stores, setStores] = useState([]);
  const [states, setStates] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingStore, setEditingStore] = useState(null);
  const [form, setForm] = useState({ name: '', code: '', address: '', phone: '', operatingHours: '', blockId: '', stateId: '', districtId: '' });
  const [formDistricts, setFormDistricts] = useState([]);
  const [formBlocks, setFormBlocks] = useState([]);
  const [saving, setSaving] = useState(false);
  const [toggleConfirm, setToggleConfirm] = useState(null);
  const [toggling, setToggling] = useState(false);
  const [filterStateId, setFilterStateId] = useState('');

  const fetchStores = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 20 };
      if (filterStateId) params.stateId = filterStateId;
      const res = await geographyApi.getStores(params);
      const data = res.data.data;
      setStores(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (error) { toast.error(getErrorMessage(error)); }
    finally { setLoading(false); }
  }, [page, filterStateId]);

  useEffect(() => { geographyApi.getActiveStates().then(r => { const d = r.data.data; setStates(d.content || d || []); }).catch(() => {}); }, []);
  useEffect(() => { fetchStores(); }, [fetchStores]);

  useEffect(() => {
    if (!form.stateId) { setFormDistricts([]); return; }
    geographyApi.getActiveDistricts({ stateId: form.stateId }).then(r => { const d = r.data.data; setFormDistricts(d.content || d || []); }).catch(() => {});
  }, [form.stateId]);

  useEffect(() => {
    if (!form.districtId) { setFormBlocks([]); return; }
    geographyApi.getActiveBlocks({ districtId: form.districtId }).then(r => { const d = r.data.data; setFormBlocks(d.content || d || []); }).catch(() => {});
  }, [form.districtId]);

  const resetForm = () => setForm({ name: '', code: '', address: '', phone: '', operatingHours: '', blockId: '', stateId: '', districtId: '' });
  const openCreate = () => { setEditingStore(null); resetForm(); setModalOpen(true); };
  const openEdit = (s) => { setEditingStore(s); setForm({ name: s.name, code: s.code || '', address: s.address || '', phone: s.phone || '', operatingHours: s.operatingHours || '', blockId: s.blockId || '', stateId: s.stateId || '', districtId: s.districtId || '' }); setModalOpen(true); };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form.name.trim()) { toast.warning('Store name is required'); return; }
    if (!form.blockId) { toast.warning('Please select a block'); return; }
    setSaving(true);
    try {
      const payload = { name: form.name.trim(), code: form.code.trim(), address: form.address.trim(), phone: form.phone.trim(), operatingHours: form.operatingHours.trim(), blockId: Number(form.blockId) };
      if (editingStore) { await geographyApi.updateStore(editingStore.id, payload); toast.success('Store updated'); }
      else { await geographyApi.createStore(payload); toast.success('Store created'); }
      setModalOpen(false); fetchStores();
    } catch (error) { toast.error(getErrorMessage(error)); }
    finally { setSaving(false); }
  };

  const handleToggle = async () => {
    if (!toggleConfirm) return;
    setToggling(true);
    try { await geographyApi.toggleStore(toggleConfirm.id); toast.success(`Store ${toggleConfirm.status === 'ACTIVE' ? 'deactivated' : 'activated'}`); setToggleConfirm(null); fetchStores(); }
    catch (error) { toast.error(getErrorMessage(error)); }
    finally { setToggling(false); }
  };

  const selectCls = "px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500";
  const fullSelectCls = "w-full px-3.5 py-2.5 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500";
  const upd = (k, v) => setForm(p => ({ ...p, [k]: v }));

  return (
    <div className="animate-fade-in">
      <PageHeader title="Stores" description="Manage ASK stores across regions">
        <Button onClick={openCreate}><Plus className="w-4 h-4" /> Add Store</Button>
      </PageHeader>

      <div className="mb-4 flex items-center gap-3">
        <select value={filterStateId} onChange={(e) => { setFilterStateId(e.target.value); setPage(0); }} className={selectCls}>
          <option value="">All States</option>
          {states.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
        </select>
      </div>

      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden">
        {loading ? <Loader text="Loading stores..." /> : stores.length === 0 ? (
          <EmptyState icon={Store} title="No stores found" description="Add your first store." action={<Button onClick={openCreate}><Plus className="w-4 h-4" /> Add Store</Button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead><tr className="border-b border-surface-200 bg-surface-50/50">
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">#</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Name</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Code</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Block</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Phone</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Status</th>
                  <th className="text-right py-3 px-4 font-semibold text-surface-600">Actions</th>
                </tr></thead>
                <tbody>{stores.map((s, i) => (
                  <tr key={s.id} className="border-b border-surface-100 hover:bg-surface-50/50 transition-colors">
                    <td className="py-3 px-4 text-surface-500">{page * 20 + i + 1}</td>
                    <td className="py-3 px-4 font-medium text-surface-900">{s.name}</td>
                    <td className="py-3 px-4 text-surface-600 font-mono text-xs">{s.code || '—'}</td>
                    <td className="py-3 px-4 text-surface-600">{s.blockName || '—'}</td>
                    <td className="py-3 px-4 text-surface-600">{s.phone || '—'}</td>
                    <td className="py-3 px-4"><StatusBadge status={s.status} /></td>
                    <td className="py-3 px-4">
                      <div className="flex items-center justify-end gap-1">
                        <button onClick={() => openEdit(s)} className="p-2 rounded-lg text-surface-500 hover:text-primary-600 hover:bg-primary-50 transition-colors cursor-pointer"><Edit3 className="w-4 h-4" /></button>
                        <button onClick={() => setToggleConfirm(s)} className={cn('p-2 rounded-lg transition-colors cursor-pointer', s.status === 'ACTIVE' ? 'text-surface-500 hover:text-danger-600 hover:bg-danger-50' : 'text-surface-500 hover:text-emerald-600 hover:bg-emerald-50')}>
                          {s.status === 'ACTIVE' ? <ToggleRight className="w-4 h-4" /> : <ToggleLeft className="w-4 h-4" />}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}</tbody>
              </table>
            </div>
            <div className="px-4"><Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} /></div>
          </>
        )}
      </div>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editingStore ? 'Edit Store' : 'Create Store'} size="lg">
        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">State</label>
              <select value={form.stateId} onChange={(e) => { upd('stateId', e.target.value); upd('districtId', ''); upd('blockId', ''); }} className={fullSelectCls}>
                <option value="">Select State</option>{states.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select></div>
            <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">District</label>
              <select value={form.districtId} onChange={(e) => { upd('districtId', e.target.value); upd('blockId', ''); }} className={fullSelectCls} disabled={!form.stateId}>
                <option value="">Select District</option>{formDistricts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
              </select></div>
            <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">Block</label>
              <select value={form.blockId} onChange={(e) => upd('blockId', e.target.value)} className={fullSelectCls} disabled={!form.districtId}>
                <option value="">Select Block</option>{formBlocks.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
              </select></div>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input label="Store Name" placeholder="e.g., ASK Patna Central" value={form.name} onChange={(e) => upd('name', e.target.value)} autoFocus />
            <Input label="Store Code" placeholder="e.g., ASK-PAT-01" value={form.code} onChange={(e) => upd('code', e.target.value)} />
          </div>
          <Input label="Address" placeholder="Full store address" value={form.address} onChange={(e) => upd('address', e.target.value)} />
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input label="Phone" placeholder="Store phone number" value={form.phone} onChange={(e) => upd('phone', e.target.value)} />
            <Input label="Operating Hours" placeholder="e.g., 9 AM – 9 PM" value={form.operatingHours} onChange={(e) => upd('operatingHours', e.target.value)} />
          </div>
          <div className="flex gap-3 pt-2">
            <Button variant="secondary" className="flex-1" onClick={() => setModalOpen(false)} type="button">Cancel</Button>
            <Button className="flex-1" type="submit" loading={saving}>{editingStore ? 'Update' : 'Create'}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog isOpen={!!toggleConfirm} onClose={() => setToggleConfirm(null)} onConfirm={handleToggle}
        title={toggleConfirm?.status === 'ACTIVE' ? 'Deactivate Store' : 'Activate Store'}
        message={`Are you sure you want to ${toggleConfirm?.status === 'ACTIVE' ? 'deactivate' : 'activate'} "${toggleConfirm?.name}"?`}
        confirmText={toggleConfirm?.status === 'ACTIVE' ? 'Deactivate' : 'Activate'} loading={toggling}
        variant={toggleConfirm?.status === 'ACTIVE' ? 'danger' : 'primary'} />
    </div>
  );
}
