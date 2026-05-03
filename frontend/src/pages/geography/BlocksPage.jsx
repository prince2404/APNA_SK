import { useState, useEffect, useCallback } from 'react';
import { MapPin, Plus, Edit3, ToggleLeft, ToggleRight, Building2 } from 'lucide-react';
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

export default function BlocksPage() {
  const [blocks, setBlocks] = useState([]);
  const [states, setStates] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingBlock, setEditingBlock] = useState(null);
  const [formName, setFormName] = useState('');
  const [formDistrictId, setFormDistrictId] = useState('');
  const [formStateId, setFormStateId] = useState('');
  const [formDistricts, setFormDistricts] = useState([]);
  const [saving, setSaving] = useState(false);
  const [toggleConfirm, setToggleConfirm] = useState(null);
  const [toggling, setToggling] = useState(false);
  const [filterStateId, setFilterStateId] = useState('');
  const [filterDistrictId, setFilterDistrictId] = useState('');

  const fetchBlocks = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 20 };
      if (filterDistrictId) params.districtId = filterDistrictId;
      else if (filterStateId) params.stateId = filterStateId;
      const res = await geographyApi.getBlocks(params);
      const data = res.data.data;
      setBlocks(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [page, filterStateId, filterDistrictId]);

  useEffect(() => {
    geographyApi.getActiveStates().then(r => { const d = r.data.data; setStates(d.content || d || []); }).catch(() => {});
  }, []);

  useEffect(() => { fetchBlocks(); }, [fetchBlocks]);

  useEffect(() => {
    if (!filterStateId) { setDistricts([]); return; }
    geographyApi.getActiveDistricts({ stateId: filterStateId }).then(r => { const d = r.data.data; setDistricts(d.content || d || []); }).catch(() => {});
  }, [filterStateId]);

  useEffect(() => {
    if (!formStateId) { setFormDistricts([]); return; }
    geographyApi.getActiveDistricts({ stateId: formStateId }).then(r => { const d = r.data.data; setFormDistricts(d.content || d || []); }).catch(() => setFormDistricts([]));
  }, [formStateId]);

  const openCreate = () => { setEditingBlock(null); setFormName(''); setFormDistrictId(''); setFormStateId(''); setModalOpen(true); };
  const openEdit = (b) => { setEditingBlock(b); setFormName(b.name); setFormStateId(b.stateId || ''); setFormDistrictId(b.districtId || ''); setModalOpen(true); };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!formName.trim()) { toast.warning('Block name is required'); return; }
    if (!formDistrictId) { toast.warning('Please select a district'); return; }
    setSaving(true);
    try {
      const payload = { name: formName.trim(), districtId: Number(formDistrictId) };
      if (editingBlock) { await geographyApi.updateBlock(editingBlock.id, payload); toast.success('Block updated'); }
      else { await geographyApi.createBlock(payload); toast.success('Block created'); }
      setModalOpen(false); fetchBlocks();
    } catch (error) { toast.error(getErrorMessage(error)); }
    finally { setSaving(false); }
  };

  const handleToggle = async () => {
    if (!toggleConfirm) return;
    setToggling(true);
    try { await geographyApi.toggleBlock(toggleConfirm.id); toast.success(`Block ${toggleConfirm.status === 'ACTIVE' ? 'deactivated' : 'activated'}`); setToggleConfirm(null); fetchBlocks(); }
    catch (error) { toast.error(getErrorMessage(error)); }
    finally { setToggling(false); }
  };

  const selectCls = "px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500";
  const fullSelectCls = "w-full px-3.5 py-2.5 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500";

  return (
    <div className="animate-fade-in">
      <PageHeader title="Blocks" description="Manage blocks within districts">
        <Button onClick={openCreate}><Plus className="w-4 h-4" /> Add Block</Button>
      </PageHeader>

      <div className="mb-4 flex flex-wrap items-center gap-3">
        <select value={filterStateId} onChange={(e) => { setFilterStateId(e.target.value); setFilterDistrictId(''); setPage(0); }} className={selectCls}>
          <option value="">All States</option>
          {states.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
        </select>
        {filterStateId && (
          <select value={filterDistrictId} onChange={(e) => { setFilterDistrictId(e.target.value); setPage(0); }} className={selectCls}>
            <option value="">All Districts</option>
            {districts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
          </select>
        )}
      </div>

      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden">
        {loading ? <Loader text="Loading blocks..." /> : blocks.length === 0 ? (
          <EmptyState icon={MapPin} title="No blocks found" description="Add your first block." action={<Button onClick={openCreate}><Plus className="w-4 h-4" /> Add Block</Button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead><tr className="border-b border-surface-200 bg-surface-50/50">
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">#</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Name</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">District</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">State</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Stores</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Status</th>
                  <th className="text-right py-3 px-4 font-semibold text-surface-600">Actions</th>
                </tr></thead>
                <tbody>{blocks.map((b, i) => (
                  <tr key={b.id} className="border-b border-surface-100 hover:bg-surface-50/50 transition-colors">
                    <td className="py-3 px-4 text-surface-500">{page * 20 + i + 1}</td>
                    <td className="py-3 px-4 font-medium text-surface-900">{b.name}</td>
                    <td className="py-3 px-4 text-surface-600">{b.districtName || '—'}</td>
                    <td className="py-3 px-4 text-surface-600">{b.stateName || '—'}</td>
                    <td className="py-3 px-4 text-surface-600">{b.storeCount ?? 0}</td>
                    <td className="py-3 px-4"><StatusBadge status={b.status} /></td>
                    <td className="py-3 px-4">
                      <div className="flex items-center justify-end gap-1">
                        <button onClick={() => openEdit(b)} className="p-2 rounded-lg text-surface-500 hover:text-primary-600 hover:bg-primary-50 transition-colors cursor-pointer"><Edit3 className="w-4 h-4" /></button>
                        <button onClick={() => setToggleConfirm(b)} className={cn('p-2 rounded-lg transition-colors cursor-pointer', b.status === 'ACTIVE' ? 'text-surface-500 hover:text-danger-600 hover:bg-danger-50' : 'text-surface-500 hover:text-emerald-600 hover:bg-emerald-50')}>
                          {b.status === 'ACTIVE' ? <ToggleRight className="w-4 h-4" /> : <ToggleLeft className="w-4 h-4" />}
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

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editingBlock ? 'Edit Block' : 'Create Block'} size="sm">
        <form onSubmit={handleSave} className="space-y-4">
          <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">State</label>
            <select value={formStateId} onChange={(e) => { setFormStateId(e.target.value); setFormDistrictId(''); }} className={fullSelectCls}>
              <option value="">Select State</option>{states.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select></div>
          <div className="space-y-1.5"><label className="block text-sm font-medium text-surface-700">District</label>
            <select value={formDistrictId} onChange={(e) => setFormDistrictId(e.target.value)} className={fullSelectCls} disabled={!formStateId}>
              <option value="">Select District</option>{formDistricts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select></div>
          <Input label="Block Name" placeholder="e.g., Phulwari Sharif" value={formName} onChange={(e) => setFormName(e.target.value)} autoFocus />
          <div className="flex gap-3 pt-2">
            <Button variant="secondary" className="flex-1" onClick={() => setModalOpen(false)} type="button">Cancel</Button>
            <Button className="flex-1" type="submit" loading={saving}>{editingBlock ? 'Update' : 'Create'}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog isOpen={!!toggleConfirm} onClose={() => setToggleConfirm(null)} onConfirm={handleToggle}
        title={toggleConfirm?.status === 'ACTIVE' ? 'Deactivate Block' : 'Activate Block'}
        message={`Are you sure you want to ${toggleConfirm?.status === 'ACTIVE' ? 'deactivate' : 'activate'} "${toggleConfirm?.name}"?`}
        confirmText={toggleConfirm?.status === 'ACTIVE' ? 'Deactivate' : 'Activate'} loading={toggling}
        variant={toggleConfirm?.status === 'ACTIVE' ? 'danger' : 'primary'} />
    </div>
  );
}
