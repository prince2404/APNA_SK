import { useState, useEffect, useCallback } from 'react';
import { MapPin, Plus, Edit3, ToggleLeft, ToggleRight, Search } from 'lucide-react';
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

export default function StatesPage() {
  const [states, setStates] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingState, setEditingState] = useState(null);
  const [formName, setFormName] = useState('');
  const [formCode, setFormCode] = useState('');
  const [saving, setSaving] = useState(false);
  const [toggleConfirm, setToggleConfirm] = useState(null);
  const [toggling, setToggling] = useState(false);

  const fetchStates = useCallback(async () => {
    setLoading(true);
    try {
      const res = await geographyApi.getStates({ page, size: 20 });
      const data = res.data.data;
      setStates(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { fetchStates(); }, [fetchStates]);

  const openCreate = () => {
    setEditingState(null); setFormName(''); setFormCode(''); setModalOpen(true);
  };

  const openEdit = (state) => {
    setEditingState(state); setFormName(state.name); setFormCode(state.code || ''); setModalOpen(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!formName.trim()) { toast.warning('State name is required'); return; }
    setSaving(true);
    try {
      if (editingState) {
        await geographyApi.updateState(editingState.id, { name: formName.trim(), code: formCode.trim() });
        toast.success('State updated successfully');
      } else {
        await geographyApi.createState({ name: formName.trim(), code: formCode.trim() });
        toast.success('State created successfully');
      }
      setModalOpen(false);
      fetchStates();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  const handleToggle = async () => {
    if (!toggleConfirm) return;
    setToggling(true);
    try {
      await geographyApi.toggleState(toggleConfirm.id);
      toast.success(`State ${toggleConfirm.status === 'ACTIVE' ? 'deactivated' : 'activated'}`);
      setToggleConfirm(null);
      fetchStates();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setToggling(false);
    }
  };

  return (
    <div className="animate-fade-in">
      <PageHeader title="States" description="Manage geographic states in the system">
        <Button onClick={openCreate}><Plus className="w-4 h-4" /> Add State</Button>
      </PageHeader>

      {/* Table */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden">
        {loading ? (
          <Loader text="Loading states..." />
        ) : states.length === 0 ? (
          <EmptyState icon={MapPin} title="No states found" description="Add your first state to get started." action={<Button onClick={openCreate}><Plus className="w-4 h-4" /> Add State</Button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-surface-200 bg-surface-50/50">
                    <th className="text-left py-3 px-4 font-semibold text-surface-600">#</th>
                    <th className="text-left py-3 px-4 font-semibold text-surface-600">Name</th>
                    <th className="text-left py-3 px-4 font-semibold text-surface-600">Code</th>
                    <th className="text-left py-3 px-4 font-semibold text-surface-600">Districts</th>
                    <th className="text-left py-3 px-4 font-semibold text-surface-600">Status</th>
                    <th className="text-right py-3 px-4 font-semibold text-surface-600">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {states.map((state, idx) => (
                    <tr key={state.id} className="border-b border-surface-100 hover:bg-surface-50/50 transition-colors">
                      <td className="py-3 px-4 text-surface-500">{page * 20 + idx + 1}</td>
                      <td className="py-3 px-4 font-medium text-surface-900">{state.name}</td>
                      <td className="py-3 px-4 text-surface-600 font-mono text-xs">{state.code || '—'}</td>
                      <td className="py-3 px-4 text-surface-600">{state.districtCount ?? 0}</td>
                      <td className="py-3 px-4"><StatusBadge status={state.status} /></td>
                      <td className="py-3 px-4">
                        <div className="flex items-center justify-end gap-1">
                          <button onClick={() => openEdit(state)} className="p-2 rounded-lg text-surface-500 hover:text-primary-600 hover:bg-primary-50 transition-colors cursor-pointer" title="Edit">
                            <Edit3 className="w-4 h-4" />
                          </button>
                          <button onClick={() => setToggleConfirm(state)} className={cn('p-2 rounded-lg transition-colors cursor-pointer', state.status === 'ACTIVE' ? 'text-surface-500 hover:text-danger-600 hover:bg-danger-50' : 'text-surface-500 hover:text-emerald-600 hover:bg-emerald-50')} title={state.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}>
                            {state.status === 'ACTIVE' ? <ToggleRight className="w-4 h-4" /> : <ToggleLeft className="w-4 h-4" />}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="px-4">
              <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
            </div>
          </>
        )}
      </div>

      {/* Create/Edit Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editingState ? 'Edit State' : 'Create State'} size="sm">
        <form onSubmit={handleSave} className="space-y-4">
          <Input label="State Name" placeholder="e.g., Maharashtra" value={formName} onChange={(e) => setFormName(e.target.value)} autoFocus />
          <Input label="State Code" placeholder="e.g., MH" value={formCode} onChange={(e) => setFormCode(e.target.value)} />
          <div className="flex gap-3 pt-2">
            <Button variant="secondary" className="flex-1" onClick={() => setModalOpen(false)} type="button">Cancel</Button>
            <Button className="flex-1" type="submit" loading={saving}>{editingState ? 'Update' : 'Create'}</Button>
          </div>
        </form>
      </Modal>

      {/* Toggle Confirm */}
      <ConfirmDialog
        isOpen={!!toggleConfirm}
        onClose={() => setToggleConfirm(null)}
        onConfirm={handleToggle}
        title={toggleConfirm?.status === 'ACTIVE' ? 'Deactivate State' : 'Activate State'}
        message={`Are you sure you want to ${toggleConfirm?.status === 'ACTIVE' ? 'deactivate' : 'activate'} "${toggleConfirm?.name}"?`}
        confirmText={toggleConfirm?.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
        loading={toggling}
        variant={toggleConfirm?.status === 'ACTIVE' ? 'danger' : 'primary'}
      />
    </div>
  );
}
