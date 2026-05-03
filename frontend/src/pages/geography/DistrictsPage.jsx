import { useState, useEffect, useCallback } from 'react';
import { Building2, Plus, Edit3, ToggleLeft, ToggleRight, MapPin } from 'lucide-react';
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

export default function DistrictsPage() {
  const [districts, setDistricts] = useState([]);
  const [states, setStates] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingDistrict, setEditingDistrict] = useState(null);
  const [formName, setFormName] = useState('');
  const [formStateId, setFormStateId] = useState('');
  const [saving, setSaving] = useState(false);
  const [toggleConfirm, setToggleConfirm] = useState(null);
  const [toggling, setToggling] = useState(false);
  const [filterStateId, setFilterStateId] = useState('');

  const fetchDistricts = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 20 };
      if (filterStateId) params.stateId = filterStateId;
      const res = await geographyApi.getDistricts(params);
      const data = res.data.data;
      setDistricts(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [page, filterStateId]);

  const fetchStates = useCallback(async () => {
    try {
      const res = await geographyApi.getActiveStates();
      const data = res.data.data;
      setStates(data.content || data || []);
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { fetchStates(); }, [fetchStates]);
  useEffect(() => { fetchDistricts(); }, [fetchDistricts]);

  const openCreate = () => {
    setEditingDistrict(null); setFormName(''); setFormStateId(''); setModalOpen(true);
  };

  const openEdit = (district) => {
    setEditingDistrict(district);
    setFormName(district.name);
    setFormStateId(district.stateId || '');
    setModalOpen(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!formName.trim()) { toast.warning('District name is required'); return; }
    if (!formStateId) { toast.warning('Please select a state'); return; }
    setSaving(true);
    try {
      if (editingDistrict) {
        await geographyApi.updateDistrict(editingDistrict.id, { name: formName.trim(), stateId: Number(formStateId) });
        toast.success('District updated successfully');
      } else {
        await geographyApi.createDistrict({ name: formName.trim(), stateId: Number(formStateId) });
        toast.success('District created successfully');
      }
      setModalOpen(false);
      fetchDistricts();
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
      await geographyApi.toggleDistrict(toggleConfirm.id);
      toast.success(`District ${toggleConfirm.status === 'ACTIVE' ? 'deactivated' : 'activated'}`);
      setToggleConfirm(null);
      fetchDistricts();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setToggling(false);
    }
  };

  return (
    <div className="animate-fade-in">
      <PageHeader title="Districts" description="Manage districts within states">
        <Button onClick={openCreate}><Plus className="w-4 h-4" /> Add District</Button>
      </PageHeader>

      {/* Filter */}
      <div className="mb-4 flex items-center gap-3">
        <div className="flex items-center gap-2">
          <MapPin className="w-4 h-4 text-surface-500" />
          <select
            value={filterStateId}
            onChange={(e) => { setFilterStateId(e.target.value); setPage(0); }}
            className="px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
          >
            <option value="">All States</option>
            {states.map((s) => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden">
        {loading ? (
          <Loader text="Loading districts..." />
        ) : districts.length === 0 ? (
          <EmptyState icon={Building2} title="No districts found" description="Add your first district to get started." action={<Button onClick={openCreate}><Plus className="w-4 h-4" /> Add District</Button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-surface-200 bg-surface-50/50">
                    <th className="text-left py-3 px-4 font-semibold text-surface-600">#</th>
                    <th className="text-left py-3 px-4 font-semibold text-surface-600">Name</th>
                    <th className="text-left py-3 px-4 font-semibold text-surface-600">State</th>
                    <th className="text-left py-3 px-4 font-semibold text-surface-600">Blocks</th>
                    <th className="text-left py-3 px-4 font-semibold text-surface-600">Status</th>
                    <th className="text-right py-3 px-4 font-semibold text-surface-600">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {districts.map((district, idx) => (
                    <tr key={district.id} className="border-b border-surface-100 hover:bg-surface-50/50 transition-colors">
                      <td className="py-3 px-4 text-surface-500">{page * 20 + idx + 1}</td>
                      <td className="py-3 px-4 font-medium text-surface-900">{district.name}</td>
                      <td className="py-3 px-4 text-surface-600">{district.stateName || '—'}</td>
                      <td className="py-3 px-4 text-surface-600">{district.blockCount ?? 0}</td>
                      <td className="py-3 px-4"><StatusBadge status={district.status} /></td>
                      <td className="py-3 px-4">
                        <div className="flex items-center justify-end gap-1">
                          <button onClick={() => openEdit(district)} className="p-2 rounded-lg text-surface-500 hover:text-primary-600 hover:bg-primary-50 transition-colors cursor-pointer" title="Edit">
                            <Edit3 className="w-4 h-4" />
                          </button>
                          <button onClick={() => setToggleConfirm(district)} className={cn('p-2 rounded-lg transition-colors cursor-pointer', district.status === 'ACTIVE' ? 'text-surface-500 hover:text-danger-600 hover:bg-danger-50' : 'text-surface-500 hover:text-emerald-600 hover:bg-emerald-50')} title={district.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}>
                            {district.status === 'ACTIVE' ? <ToggleRight className="w-4 h-4" /> : <ToggleLeft className="w-4 h-4" />}
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
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editingDistrict ? 'Edit District' : 'Create District'} size="sm">
        <form onSubmit={handleSave} className="space-y-4">
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-surface-700">State</label>
            <select
              value={formStateId}
              onChange={(e) => setFormStateId(e.target.value)}
              className="w-full px-3.5 py-2.5 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
            >
              <option value="">Select State</option>
              {states.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </div>
          <Input label="District Name" placeholder="e.g., Patna" value={formName} onChange={(e) => setFormName(e.target.value)} autoFocus />
          <div className="flex gap-3 pt-2">
            <Button variant="secondary" className="flex-1" onClick={() => setModalOpen(false)} type="button">Cancel</Button>
            <Button className="flex-1" type="submit" loading={saving}>{editingDistrict ? 'Update' : 'Create'}</Button>
          </div>
        </form>
      </Modal>

      {/* Toggle Confirm */}
      <ConfirmDialog
        isOpen={!!toggleConfirm}
        onClose={() => setToggleConfirm(null)}
        onConfirm={handleToggle}
        title={toggleConfirm?.status === 'ACTIVE' ? 'Deactivate District' : 'Activate District'}
        message={`Are you sure you want to ${toggleConfirm?.status === 'ACTIVE' ? 'deactivate' : 'activate'} "${toggleConfirm?.name}"?`}
        confirmText={toggleConfirm?.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
        loading={toggling}
        variant={toggleConfirm?.status === 'ACTIVE' ? 'danger' : 'primary'}
      />
    </div>
  );
}
