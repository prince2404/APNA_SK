import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, Plus, Search, Eye, ToggleLeft, ToggleRight } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';
import { Button } from '@/components/common/Button';
import { StatusBadge } from '@/components/common/StatusBadge';
import { EmptyState } from '@/components/common/EmptyState';
import { Pagination } from '@/components/common/Pagination';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { Loader } from '@/components/common/Loader';
import { Badge } from '@/components/common/Badge';
import { userApi } from '@/api/userApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { ROUTES } from '@/constants/routePaths';
import { ROLE_DISPLAY_NAMES } from '@/constants/roles';
import { useDebounce } from '@/hooks/useDebounce';
import { usePermission } from '@/hooks/usePermission';
import { cn } from '@/utils/cn';

export default function UserListPage() {
  const navigate = useNavigate();
  const { isSuperAdmin } = usePermission();
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [toggleConfirm, setToggleConfirm] = useState(null);
  const [toggling, setToggling] = useState(false);
  const debouncedSearch = useDebounce(search, 400);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 20 };
      if (debouncedSearch) params.search = debouncedSearch;
      if (statusFilter) params.status = statusFilter;
      const res = await userApi.getUsers(params);
      const data = res.data.data;
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (error) { toast.error(getErrorMessage(error)); }
    finally { setLoading(false); }
  }, [page, debouncedSearch, statusFilter]);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);
  useEffect(() => { setPage(0); }, [debouncedSearch, statusFilter]);

  const handleToggle = async () => {
    if (!toggleConfirm) return;
    setToggling(true);
    try {
      if (toggleConfirm.status === 'ACTIVE') {
        await userApi.deactivateUser(toggleConfirm.id);
        toast.success('User deactivated');
      } else {
        await userApi.reactivateUser(toggleConfirm.id);
        toast.success('User reactivated');
      }
      setToggleConfirm(null); fetchUsers();
    } catch (error) { toast.error(getErrorMessage(error)); }
    finally { setToggling(false); }
  };

  const getInitials = (name) => name?.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() || 'U';

  const selectCls = "px-3 py-2 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500";

  return (
    <div className="animate-fade-in">
      <PageHeader title="Users" description="Manage system users and permissions">
        <Button onClick={() => navigate(ROUTES.USER_CREATE)}><Plus className="w-4 h-4" /> Add User</Button>
      </PageHeader>

      <div className="mb-4 flex flex-wrap items-center gap-3">
        <div className="relative flex-1 max-w-xs">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-surface-400" />
          <input
            type="text" placeholder="Search by name, email, phone..."
            value={search} onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-2 text-sm rounded-lg border border-surface-300 bg-white text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500"
          />
        </div>
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className={selectCls}>
          <option value="">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
          <option value="LOCKED">Locked</option>
        </select>
      </div>

      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden">
        {loading ? <Loader text="Loading users..." /> : users.length === 0 ? (
          <EmptyState icon={Users} title="No users found" description="Create the first user to get started." action={<Button onClick={() => navigate(ROUTES.USER_CREATE)}><Plus className="w-4 h-4" /> Add User</Button>} />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead><tr className="border-b border-surface-200 bg-surface-50/50">
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">#</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">User</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Role</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Phone</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Geography</th>
                  <th className="text-left py-3 px-4 font-semibold text-surface-600">Status</th>
                  <th className="text-right py-3 px-4 font-semibold text-surface-600">Actions</th>
                </tr></thead>
                <tbody>{users.map((u, i) => (
                  <tr key={u.id} className="border-b border-surface-100 hover:bg-surface-50/50 transition-colors">
                    <td className="py-3 px-4 text-surface-500">{page * 20 + i + 1}</td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center text-xs font-bold text-white shrink-0">
                          {getInitials(u.fullName)}
                        </div>
                        <div>
                          <p className="font-medium text-surface-900">{u.fullName}</p>
                          <p className="text-xs text-surface-500">{u.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="py-3 px-4"><Badge>{ROLE_DISPLAY_NAMES[u.roleName] || u.roleName}</Badge></td>
                    <td className="py-3 px-4 text-surface-600">{u.phone || '—'}</td>
                    <td className="py-3 px-4 text-surface-600 text-xs">
                      {[u.stateName, u.districtName, u.blockName, u.storeName].filter(Boolean).join(' → ') || 'All'}
                    </td>
                    <td className="py-3 px-4"><StatusBadge status={u.status} /></td>
                    <td className="py-3 px-4">
                      <div className="flex items-center justify-end gap-1">
                        <button onClick={() => navigate(`/users/${u.id}`)} className="p-2 rounded-lg text-surface-500 hover:text-primary-600 hover:bg-primary-50 transition-colors cursor-pointer" title="View"><Eye className="w-4 h-4" /></button>
                        {isSuperAdmin && (
                          <button onClick={() => setToggleConfirm(u)} className={cn('p-2 rounded-lg transition-colors cursor-pointer', u.status === 'ACTIVE' ? 'text-surface-500 hover:text-danger-600 hover:bg-danger-50' : 'text-surface-500 hover:text-emerald-600 hover:bg-emerald-50')}>
                            {u.status === 'ACTIVE' ? <ToggleRight className="w-4 h-4" /> : <ToggleLeft className="w-4 h-4" />}
                          </button>
                        )}
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

      <ConfirmDialog isOpen={!!toggleConfirm} onClose={() => setToggleConfirm(null)} onConfirm={handleToggle}
        title={toggleConfirm?.status === 'ACTIVE' ? 'Deactivate User' : 'Reactivate User'}
        message={`Are you sure you want to ${toggleConfirm?.status === 'ACTIVE' ? 'deactivate' : 'reactivate'} "${toggleConfirm?.fullName}"?`}
        confirmText={toggleConfirm?.status === 'ACTIVE' ? 'Deactivate' : 'Reactivate'} loading={toggling}
        variant={toggleConfirm?.status === 'ACTIVE' ? 'danger' : 'primary'} />
    </div>
  );
}
