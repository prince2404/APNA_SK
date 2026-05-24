import { useState, useEffect } from 'react';
import { useAuthStore } from '@/store/useAuthStore';
import { PageHeader } from '@/components/common/PageHeader';
import { commissionApi } from '@/api/commissionApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { ROLES } from '@/constants/roles';
import { Loader } from '@/components/common/Loader';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Pagination } from '@/components/common/Pagination';
import { Settings, Coins, TrendingUp, User, Calendar, Save, Percent, DollarSign, ListFilter, AlertCircle } from 'lucide-react';

export default function CommissionsPage() {
  const currentUser = useAuthStore((s) => s.user);
  const isAdmin = currentUser?.roleName === ROLES.SUPER_ADMIN || currentUser?.roleName === ROLES.SYSTEM_ADMIN;

  const [activeTab, setActiveTab] = useState('history'); // history, config, summary
  const [loading, setLoading] = useState(false);

  // 1. Commission Entries state
  const [entries, setEntries] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [filters, setFilters] = useState({
    userId: '',
    roleId: '',
    month: new Date().toISOString().substring(0, 7), // YYYY-MM
    status: '',
  });

  // 2. Commission Configs state
  const [configs, setConfigs] = useState([]);
  const [editingConfigId, setEditingConfigId] = useState(null);
  const [editingPercentage, setEditingPercentage] = useState('');

  // 3. Commission Payout Summary state
  const [summaryMonth, setSummaryMonth] = useState(new Date().toISOString().substring(0, 7));
  const [summaries, setSummaries] = useState([]);

  // Fetch Commission Entries
  const fetchEntries = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size,
        month: filters.month || undefined,
        status: filters.status || undefined,
        userId: filters.userId || undefined,
        roleId: filters.roleId || undefined,
      };
      const res = await commissionApi.getCommissions(params);
      setEntries(res.data.data?.content || []);
      setTotalElements(res.data.data?.totalElements || 0);
    } catch (err) {
      toast.error('Failed to load commissions: ' + getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  // Fetch Configurations (Admin only)
  const fetchConfigs = async () => {
    if (!isAdmin) return;
    setLoading(true);
    try {
      const res = await commissionApi.getConfigs();
      setConfigs(res.data.data || []);
    } catch (err) {
      toast.error('Failed to load config: ' + getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  // Fetch Payout Summary (Admin only)
  const fetchSummaries = async () => {
    if (!isAdmin) return;
    setLoading(true);
    try {
      const res = await commissionApi.getCommissionSummary({ month: summaryMonth });
      setSummaries(res.data.data || []);
    } catch (err) {
      toast.error('Failed to load payout summary: ' + getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'history') {
      fetchEntries();
    } else if (activeTab === 'config') {
      fetchConfigs();
    } else if (activeTab === 'summary') {
      fetchSummaries();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab, page, filters.month, filters.status, summaryMonth]);

  const handleUpdateConfig = async (roleId, percentage) => {
    try {
      await commissionApi.updateConfig({
        roleId,
        percentage: parseFloat(percentage),
      });
      toast.success('Commission percentage updated successfully');
      setEditingConfigId(null);
      fetchConfigs();
    } catch (err) {
      toast.error('Failed to update config: ' + getErrorMessage(err));
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
    setPage(0);
  };

  const clearFilters = () => {
    setFilters({
      userId: '',
      roleId: '',
      month: new Date().toISOString().substring(0, 7),
      status: '',
    });
    setPage(0);
  };

  return (
    <div className="space-y-6 animate-fade-in text-slate-800">
      <PageHeader title="Commissions & Payouts" description="View automatic payouts & configuration values." />

      {/* Tabs */}
      <div className="flex border-b border-surface-200">
        <button
          onClick={() => { setActiveTab('history'); setPage(0); }}
          className={`flex items-center gap-2 px-5 py-3 border-b-2 font-medium text-sm transition-all cursor-pointer ${
            activeTab === 'history'
              ? 'border-primary-600 text-primary-600 font-semibold'
              : 'border-transparent text-surface-500 hover:text-surface-700'
          }`}
        >
          <Coins className="w-4 h-4" />
          Payout History
        </button>

        {isAdmin && (
          <>
            <button
              onClick={() => setActiveTab('config')}
              className={`flex items-center gap-2 px-5 py-3 border-b-2 font-medium text-sm transition-all cursor-pointer ${
                activeTab === 'config'
                  ? 'border-primary-600 text-primary-600 font-semibold'
                  : 'border-transparent text-surface-500 hover:text-surface-700'
              }`}
            >
              <Settings className="w-4 h-4" />
              Payout Configurations
            </button>

            <button
              onClick={() => setActiveTab('summary')}
              className={`flex items-center gap-2 px-5 py-3 border-b-2 font-medium text-sm transition-all cursor-pointer ${
                activeTab === 'summary'
                  ? 'border-primary-600 text-primary-600 font-semibold'
                  : 'border-transparent text-surface-500 hover:text-surface-700'
              }`}
            >
              <TrendingUp className="w-4 h-4" />
              Monthly Summaries
            </button>
          </>
        )}
      </div>

      {loading && (
        <div className="flex justify-center py-12">
          <Loader className="w-8 h-8 text-primary-600" />
        </div>
      )}

      {!loading && activeTab === 'history' && (
        <div className="space-y-4">
          {/* Filters card */}
          <div className="bg-white p-5 rounded-xl border border-surface-200 shadow-sm grid grid-cols-1 sm:grid-cols-4 gap-4 items-end">
            <div>
              <label className="text-xs font-semibold text-surface-500 block mb-1">Month</label>
              <input
                type="month"
                name="month"
                value={filters.month}
                onChange={handleFilterChange}
                className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
              />
            </div>
            <div>
              <label className="text-xs font-semibold text-surface-500 block mb-1">Status</label>
              <select
                name="status"
                value={filters.status}
                onChange={handleFilterChange}
                className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
              >
                <option value="">All Statuses</option>
                <option value="CALCULATED">Calculated</option>
                <option value="DISTRIBUTED">Distributed</option>
                <option value="PAID">Paid</option>
              </select>
            </div>
            <div>
              <label className="text-xs font-semibold text-surface-500 block mb-1">User ID</label>
              <Input
                placeholder="Search User ID"
                name="userId"
                type="number"
                value={filters.userId}
                onChange={handleFilterChange}
              />
            </div>
            <div className="flex gap-2">
              <Button variant="secondary" onClick={clearFilters} className="w-full flex items-center justify-center gap-2">
                <ListFilter className="w-4 h-4" /> Clear
              </Button>
            </div>
          </div>

          {/* Table */}
          {entries.length === 0 ? (
            <div className="bg-white p-12 text-center border border-surface-200 rounded-xl">
              <AlertCircle className="w-12 h-12 text-surface-400 mx-auto mb-3" />
              <p className="text-surface-500 font-medium">No commission entries found for the current filter criteria.</p>
            </div>
          ) : (
            <div className="bg-white rounded-xl border border-surface-200 shadow-sm overflow-x-auto">
              <table className="w-full text-left text-sm border-collapse">
                <thead>
                  <tr className="bg-surface-50 border-b border-surface-200">
                    <th className="p-4 font-semibold text-surface-600">ID</th>
                    <th className="p-4 font-semibold text-surface-600">Recipient</th>
                    <th className="p-4 font-semibold text-surface-600">Role</th>
                    <th className="p-4 font-semibold text-surface-600">Bill Number</th>
                    <th className="p-4 font-semibold text-surface-600">Month</th>
                    <th className="p-4 font-semibold text-surface-600 text-right">Commission (₹)</th>
                    <th className="p-4 font-semibold text-surface-600 text-center">Status</th>
                    <th className="p-4 font-semibold text-surface-600">Date</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-100">
                  {entries.map((entry) => (
                    <tr key={entry.id} className="hover:bg-surface-50/50 transition-colors">
                      <td className="p-4 font-medium">#{entry.id}</td>
                      <td className="p-4">
                        <div>
                          <p className="font-semibold text-surface-900">{entry.user?.fullName}</p>
                          <p className="text-xs text-surface-400">{entry.user?.email}</p>
                        </div>
                      </td>
                      <td className="p-4">
                        <span className="px-2 py-0.5 text-xs rounded-full bg-slate-100 text-slate-700 border border-slate-200">
                          {entry.role?.displayName || entry.role?.name}
                        </span>
                      </td>
                      <td className="p-4 font-mono text-xs">{entry.bill?.billNumber}</td>
                      <td className="p-4">{entry.month}</td>
                      <td className="p-4 text-right font-bold text-emerald-600">₹{entry.amount.toFixed(2)}</td>
                      <td className="p-4 text-center">
                        <span className={`px-2 py-0.5 text-xs font-semibold rounded-full ${
                          entry.status === 'PAID'
                            ? 'bg-green-50 text-green-700 border border-green-200'
                            : entry.status === 'DISTRIBUTED'
                            ? 'bg-blue-50 text-blue-700 border border-blue-200'
                            : 'bg-amber-50 text-amber-700 border border-amber-200'
                        }`}>
                          {entry.status}
                        </span>
                      </td>
                      <td className="p-4 text-surface-500 text-xs">
                        {new Date(entry.createdAt).toLocaleDateString('en-IN', {
                          day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
                        })}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <div className="p-4 border-t border-surface-200">
                <Pagination
                  currentPage={page}
                  totalItems={totalElements}
                  itemsPerPage={size}
                  onPageChange={setPage}
                />
              </div>
            </div>
          )}
        </div>
      )}

      {!loading && activeTab === 'config' && (
        <div className="bg-white rounded-xl border border-surface-200 shadow-sm p-6 space-y-6">
          <div className="flex items-center gap-3 border-b pb-4">
            <Percent className="w-6 h-6 text-primary-600" />
            <div>
              <h3 className="text-lg font-bold text-surface-900">Commission Percentages by Role</h3>
              <p className="text-sm text-surface-500">Configure payouts distributed up the hierarchical line on each store sale invoice.</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {configs.map((cfg) => (
              <div key={cfg.id} className="p-4 border border-surface-200 rounded-xl hover:shadow-sm transition-all flex items-center justify-between">
                <div>
                  <h4 className="font-bold text-surface-900">{cfg.roleName}</h4>
                  <p className="text-xs text-surface-400 mt-1">
                    Last updated by <span className="font-semibold">{cfg.updatedByName}</span> on{' '}
                    {cfg.updatedAt ? new Date(cfg.updatedAt).toLocaleDateString('en-IN') : 'N/A'}
                  </p>
                </div>
                <div className="flex items-center gap-3">
                  {editingConfigId === cfg.id ? (
                    <div className="flex items-center gap-2">
                      <input
                        type="number"
                        step="0.01"
                        value={editingPercentage}
                        onChange={(e) => setEditingPercentage(e.target.value)}
                        className="w-20 text-sm border border-surface-200 rounded-lg p-1.5 text-center bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500 font-bold"
                      />
                      <span className="text-sm font-semibold">%</span>
                      <Button size="sm" onClick={() => handleUpdateConfig(cfg.roleId, editingPercentage)}>
                        <Save className="w-3.5 h-3.5" />
                      </Button>
                      <Button size="sm" variant="secondary" onClick={() => setEditingConfigId(null)}>
                        Cancel
                      </Button>
                    </div>
                  ) : (
                    <div className="flex items-center gap-3">
                      <span className="text-lg font-black text-primary-600">{cfg.percentage.toFixed(2)}%</span>
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => {
                          setEditingConfigId(cfg.id);
                          setEditingPercentage(cfg.percentage.toString());
                        }}
                      >
                        Edit
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {!loading && activeTab === 'summary' && (
        <div className="space-y-6">
          <div className="bg-white p-5 rounded-xl border border-surface-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h3 className="text-md font-bold text-surface-900">Monthly Aggregated Payout Summary</h3>
              <p className="text-xs text-surface-500">Calculates cumulative earned values grouped by user profile for processing banks.</p>
            </div>
            <div className="flex items-center gap-2">
              <Calendar className="w-4 h-4 text-surface-400" />
              <input
                type="month"
                value={summaryMonth}
                onChange={(e) => setSummaryMonth(e.target.value)}
                className="text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500 font-semibold"
              />
            </div>
          </div>

          {summaries.length === 0 ? (
            <div className="bg-white p-12 text-center border border-surface-200 rounded-xl">
              <AlertCircle className="w-12 h-12 text-surface-400 mx-auto mb-3" />
              <p className="text-surface-500 font-medium">No payouts calculated for {summaryMonth}.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Leaderboard list */}
              <div className="lg:col-span-2 bg-white rounded-xl border border-surface-200 shadow-sm overflow-hidden">
                <table className="w-full text-left text-sm border-collapse">
                  <thead>
                    <tr className="bg-surface-50 border-b border-surface-200">
                      <th className="p-4 font-semibold text-surface-600">User Payout</th>
                      <th className="p-4 font-semibold text-surface-600">Hierarchy Level</th>
                      <th className="p-4 font-semibold text-surface-600 text-right">Total Commission</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-surface-100">
                    {summaries.map((s) => (
                      <tr key={s.userId} className="hover:bg-surface-50/50">
                        <td className="p-4">
                          <div className="flex items-center gap-3">
                            <div className="w-8 h-8 rounded-full bg-primary-50 flex items-center justify-center font-bold text-primary-600 text-xs">
                              {s.fullName.split(' ').map(n => n[0]).join('')}
                            </div>
                            <div>
                              <p className="font-semibold text-surface-900">{s.fullName}</p>
                              <p className="text-xs text-surface-400">ID #{s.userId}</p>
                            </div>
                          </div>
                        </td>
                        <td className="p-4">
                          <span className="px-2 py-0.5 text-xs rounded-full bg-slate-100 text-slate-700">
                            {s.roleName}
                          </span>
                        </td>
                        <td className="p-4 text-right font-extrabold text-emerald-600">₹{s.totalAmount.toFixed(2)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Total Aggregate Summary Card */}
              <div className="bg-gradient-to-br from-primary-600 via-primary-700 to-primary-800 rounded-xl p-6 text-white shadow-lg relative overflow-hidden flex flex-col justify-between">
                <div className="absolute right-0 top-0 w-32 h-32 bg-white/5 rounded-full -translate-y-1/3 translate-x-1/3" />
                <div className="relative z-10 space-y-4">
                  <div className="w-12 h-12 rounded-xl bg-white/10 flex items-center justify-center">
                    <DollarSign className="w-6 h-6" />
                  </div>
                  <div>
                    <h4 className="text-sm font-medium text-primary-200 uppercase tracking-wider">Total Monthly Distribution</h4>
                    <p className="text-3xl font-black mt-1">
                      ₹{summaries.reduce((sum, item) => sum + item.totalAmount, 0).toFixed(2)}
                    </p>
                  </div>
                  <div className="text-xs text-primary-200/90 leading-relaxed border-t border-white/10 pt-4">
                    This represents the aggregate commission payouts calculated for all registered coordinators during{' '}
                    <span className="font-bold text-white">{summaryMonth}</span>. All figures are computed on successful store invoices.
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
