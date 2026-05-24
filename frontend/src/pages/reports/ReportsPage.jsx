import { useState, useEffect } from 'react';
import { useAuthStore } from '@/store/useAuthStore';
import { PageHeader } from '@/components/common/PageHeader';
import { reportApi } from '@/api/reportApi';
import { geographyApi } from '@/api/geographyApi';
import { productApi } from '@/api/productApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { Loader } from '@/components/common/Loader';
import { Button } from '@/components/common/Button';
import { ROLES } from '@/constants/roles';
import {
  BarChart3, FileText, TrendingUp, ShoppingCart, Users, Package,
  AlertTriangle, IndianRupee, Activity, Download, Filter, RefreshCw
} from 'lucide-react';

const reportTypes = [
  { id: 'sales', icon: ShoppingCart, title: 'Sales Report', desc: 'Daily sales summaries filterable by store and date range.', color: 'from-blue-500 to-indigo-600', roles: [] },
  { id: 'stock', icon: Package, title: 'Stock Report', desc: 'Current store stock catalog, batch info, and categorizations.', color: 'from-teal-500 to-cyan-600', roles: [] },
  { id: 'commission', icon: IndianRupee, title: 'Commission Report', desc: 'Calculated coordinator payouts by month and specific roles.', color: 'from-emerald-500 to-green-600', roles: [] },
  { id: 'patient', icon: Users, title: 'Patient Report', desc: 'Patient demographics, addresses, and registration dates.', color: 'from-rose-500 to-pink-600', roles: [] },
  { id: 'bill', icon: FileText, title: 'Bill Report', desc: 'Individual sale invoice lists with payment methods and statuses.', color: 'from-violet-500 to-purple-600', roles: [] },
  { id: 'expiry', icon: AlertTriangle, title: 'Expiry Report', desc: 'Warehouse and store batch stocks expiring within selected threshold.', color: 'from-amber-500 to-yellow-600', roles: [] },
  { id: 'activity', icon: Activity, title: 'User Activity Report', desc: 'Audit trails of operations, system changes, and platform logs.', color: 'from-indigo-500 to-blue-600', roles: [ROLES.SUPER_ADMIN, ROLES.SYSTEM_ADMIN] },
  { id: 'revenue', icon: TrendingUp, title: 'Revenue Report', desc: 'Geographic and regional revenue comparisons and invoice counts.', color: 'from-purple-500 to-fuchsia-600', roles: [] },
  { id: 'lowStock', icon: Package, title: 'Low Stock Report', desc: 'Items currently below minimum safety thresholds across locations.', color: 'from-orange-500 to-red-600', roles: [] },
];

export default function ReportsPage() {
  const currentUser = useAuthStore((s) => s.user);
  const userRole = currentUser?.roleName;

  const [activeReport, setActiveReport] = useState('sales');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);

  // Geographic / Categories lists for filters
  const [stores, setStores] = useState([]);
  const [states, setStates] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [blocks, setBlocks] = useState([]);
  const [categories, setCategories] = useState([]);

  // Filter values
  const [filters, setFilters] = useState({
    storeId: '',
    stateId: '',
    districtId: '',
    blockId: '',
    categoryId: '',
    startDate: '',
    endDate: '',
    month: new Date().toISOString().substring(0, 7),
    roleId: '',
    status: '',
    days: '30',
    userId: '',
    action: ''
  });

  // Allowed reports based on role
  const allowedReports = reportTypes.filter(
    (r) => r.roles.length === 0 || r.roles.includes(userRole)
  );

  useEffect(() => {
    // Load metadata for filters
    const loadMetadata = async () => {
      try {
        const storeRes = await geographyApi.getStores({ size: 100 });
        setStores(storeRes.data.data?.content || []);
        
        if (userRole === ROLES.SUPER_ADMIN || userRole === ROLES.SYSTEM_ADMIN) {
          const stateRes = await geographyApi.getStates({ size: 100 });
          setStates(stateRes.data.data?.content || []);
          const distRes = await geographyApi.getDistricts({ size: 100 });
          setDistricts(distRes.data.data?.content || []);
          const blockRes = await geographyApi.getBlocks({ size: 100 });
          setBlocks(blockRes.data.data?.content || []);
        }

        const catRes = await productApi.getCategories();
        setCategories(catRes.data.data || []);
      } catch (err) {
        toast.error('Failed to load filter metadata: ' + getErrorMessage(err));
      }
    };
    loadMetadata();
  }, [userRole]);

  // Run active report query
  const runReport = async () => {
    setLoading(true);
    try {
      let res;
      const params = cleanParams();
      switch (activeReport) {
        case 'sales':
          res = await reportApi.getSalesReport(params);
          break;
        case 'stock':
          res = await reportApi.getStockReport(params);
          break;
        case 'commission':
          res = await reportApi.getCommissionReport(params);
          break;
        case 'patient':
          res = await reportApi.getPatientReport(params);
          break;
        case 'bill':
          res = await reportApi.getBillReport(params);
          break;
        case 'expiry':
          res = await reportApi.getExpiryReport(params);
          break;
        case 'activity':
          res = await reportApi.getUserActivityReport(params);
          break;
        case 'revenue':
          res = await reportApi.getRevenueReport(params);
          break;
        case 'lowStock':
          res = await reportApi.getLowStockReport(params);
          break;
        default:
          break;
      }
      setData(res?.data?.data || []);
    } catch (err) {
      toast.error('Failed to run report: ' + getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    setExporting(true);
    try {
      let res;
      const params = cleanParams();
      switch (activeReport) {
        case 'sales':
          res = await reportApi.exportSalesReport(params);
          break;
        case 'stock':
          res = await reportApi.exportStockReport(params);
          break;
        case 'commission':
          res = await reportApi.exportCommissionReport(params);
          break;
        case 'patient':
          res = await reportApi.exportPatientReport(params);
          break;
        case 'bill':
          res = await reportApi.exportBillReport(params);
          break;
        case 'expiry':
          res = await reportApi.exportExpiryReport(params);
          break;
        case 'activity':
          res = await reportApi.exportUserActivityReport(params);
          break;
        case 'revenue':
          res = await reportApi.exportRevenueReport(params);
          break;
        case 'lowStock':
          res = await reportApi.exportLowStockReport(params);
          break;
        default:
          break;
      }
      
      if (res && res.data) {
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `${activeReport}-report-${new Date().toISOString().substring(0, 10)}.csv`);
        document.body.appendChild(link);
        link.click();
        link.remove();
        toast.success('Report exported successfully');
      }
    } catch (err) {
      toast.error('Failed to export report: ' + getErrorMessage(err));
    } finally {
      setExporting(false);
    }
  };

  const cleanParams = () => {
    const p = {};
    if (filters.storeId) p.storeId = filters.storeId;
    if (filters.stateId) p.stateId = filters.stateId;
    if (filters.districtId) p.districtId = filters.districtId;
    if (filters.blockId) p.blockId = filters.blockId;
    if (filters.categoryId) p.categoryId = filters.categoryId;
    if (filters.startDate) p.startDate = filters.startDate;
    if (filters.endDate) p.endDate = filters.endDate;
    if (filters.month) p.month = filters.month;
    if (filters.roleId) p.roleId = filters.roleId;
    if (filters.status) p.status = filters.status;
    if (filters.days) p.days = parseInt(filters.days, 10);
    if (filters.userId) p.userId = filters.userId;
    if (filters.action) p.action = filters.action;
    return p;
  };

  useEffect(() => {
    setData([]);
    runReport();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeReport]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  // Render Table Columns dynamically based on activeReport
  const getTableColumns = () => {
    switch (activeReport) {
      case 'sales':
        return [
          { key: 'date', label: 'Date' },
          { key: 'storeName', label: 'Store Name' },
          { key: 'storeCode', label: 'Store Code' },
          { key: 'totalBills', label: 'Total Bills' },
          { key: 'totalMrp', label: 'Total MRP (₹)', num: true },
          { key: 'totalAskPrice', label: 'Total ASK Price (₹)', num: true },
          { key: 'totalDiscount', label: 'Discount (₹)', num: true },
          { key: 'netAmount', label: 'Net Revenue (₹)', num: true }
        ];
      case 'stock':
        return [
          { key: 'storeName', label: 'Store Name' },
          { key: 'productName', label: 'Product' },
          { key: 'brand', label: 'Brand' },
          { key: 'category', label: 'Category' },
          { key: 'batchNumber', label: 'Batch No' },
          { key: 'expiryDate', label: 'Expiry Date' },
          { key: 'quantity', label: 'Qty' }
        ];
      case 'commission':
        return [
          { key: 'month', label: 'Month' },
          { key: 'userName', label: 'User Full Name' },
          { key: 'role', label: 'Role Level' },
          { key: 'amount', label: 'Earned Amount (₹)', num: true },
          { key: 'status', label: 'Status' }
        ];
      case 'patient':
        return [
          { key: 'patientId', label: 'Patient ID' },
          { key: 'fullName', label: 'Name' },
          { key: 'age', label: 'Age' },
          { key: 'gender', label: 'Gender' },
          { key: 'phone', label: 'Phone' },
          { key: 'registeredDate', label: 'Reg Date' },
          { key: 'storeName', label: 'Store Location' },
          { key: 'block', label: 'Block' }
        ];
      case 'bill':
        return [
          { key: 'billNumber', label: 'Invoice No' },
          { key: 'date', label: 'Date Time' },
          { key: 'patientName', label: 'Patient' },
          { key: 'storeName', label: 'Store Name' },
          { key: 'netAmount', label: 'Amount (₹)', num: true },
          { key: 'paymentMode', label: 'Payment' },
          { key: 'status', label: 'Status' }
        ];
      case 'expiry':
        return [
          { key: 'storeName', label: 'Store' },
          { key: 'productName', label: 'Product Name' },
          { key: 'batchNumber', label: 'Batch Number' },
          { key: 'expiryDate', label: 'Expiry Date' },
          { key: 'quantity', label: 'Stock Qty' }
        ];
      case 'activity':
        return [
          { key: 'timestamp', label: 'Timestamp' },
          { key: 'userName', label: 'User' },
          { key: 'role', label: 'Role' },
          { key: 'action', label: 'Action' },
          { key: 'entityType', label: 'Module' },
          { key: 'entityId', label: 'Entity ID' },
          { key: 'description', label: 'Description' }
        ];
      case 'revenue':
        return [
          { key: 'state', label: 'State' },
          { key: 'district', label: 'District' },
          { key: 'totalRevenue', label: 'Total Revenue (₹)', num: true },
          { key: 'totalInvoices', label: 'Invoice Count' }
        ];
      case 'lowStock':
        return [
          { key: 'storeName', label: 'Store' },
          { key: 'productName', label: 'Product' },
          { key: 'currentStock', label: 'Current Qty' },
          { key: 'minThreshold', label: 'Min Safety Qty' }
        ];
      default:
        return [];
    }
  };

  const columns = getTableColumns();

  return (
    <div className="space-y-6 animate-fade-in text-slate-800">
      <PageHeader title="Analytics & Reports" description="Generate system-wide metrics and geographic reports." />

      {/* Grid of Report Types */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {allowedReports.map((r) => (
          <button
            key={r.id}
            onClick={() => setActiveReport(r.id)}
            className={`p-4 rounded-xl border transition-all text-left shadow-sm cursor-pointer flex flex-col justify-between group ${
              activeReport === r.id
                ? 'border-primary-600 ring-2 ring-primary-500/20 bg-primary-50/20'
                : 'border-surface-200 bg-white hover:shadow-md'
            }`}
          >
            <div className="flex items-center gap-3 mb-2">
              <div className={`w-9 h-9 rounded-lg bg-gradient-to-br ${r.color} flex items-center justify-center shadow-sm`}>
                <r.icon className="w-4 h-4 text-white" />
              </div>
              <h4 className="font-bold text-surface-900 text-sm group-hover:text-primary-600 transition-colors">{r.title}</h4>
            </div>
            <p className="text-xs text-surface-500 leading-relaxed">{r.desc}</p>
          </button>
        ))}
      </div>

      {/* Dynamic Filters Form */}
      <div className="bg-white p-6 rounded-xl border border-surface-200 shadow-sm space-y-4">
        <div className="flex items-center justify-between border-b pb-3 mb-2">
          <div className="flex items-center gap-2">
            <Filter className="w-4 h-4 text-primary-600" />
            <h3 className="font-bold text-surface-900 text-sm">Filter Criteria</h3>
          </div>
          <div className="flex items-center gap-2">
            <Button size="sm" variant="secondary" onClick={runReport} className="flex items-center gap-1.5">
              <RefreshCw className="w-3.5 h-3.5" /> Reload
            </Button>
            <Button size="sm" onClick={handleExport} disabled={exporting || data.length === 0} className="flex items-center gap-1.5">
              <Download className="w-3.5 h-3.5" /> {exporting ? 'Exporting...' : 'Export CSV'}
            </Button>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Store Selector */}
          {['sales', 'stock', 'patient', 'bill', 'lowStock'].includes(activeReport) && (
            <div>
              <label className="text-xs font-semibold text-surface-500 block mb-1">Store Location</label>
              <select
                name="storeId"
                value={filters.storeId}
                onChange={handleFilterChange}
                className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
              >
                <option value="">All Store Locations</option>
                {stores.map((s) => (
                  <option key={s.id} value={s.id}>{s.name} ({s.code})</option>
                ))}
              </select>
            </div>
          )}

          {/* Category Selector */}
          {activeReport === 'stock' && (
            <div>
              <label className="text-xs font-semibold text-surface-500 block mb-1">Category</label>
              <select
                name="categoryId"
                value={filters.categoryId}
                onChange={handleFilterChange}
                className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
              >
                <option value="">All Categories</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
          )}

          {/* Regional selectors for Revenue / Patients */}
          {['revenue', 'patient'].includes(activeReport) && (userRole === ROLES.SUPER_ADMIN || userRole === ROLES.SYSTEM_ADMIN) && (
            <>
              <div>
                <label className="text-xs font-semibold text-surface-500 block mb-1">State</label>
                <select
                  name="stateId"
                  value={filters.stateId}
                  onChange={handleFilterChange}
                  className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
                >
                  <option value="">All States</option>
                  {states.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                </select>
              </div>
              <div>
                <label className="text-xs font-semibold text-surface-500 block mb-1">District</label>
                <select
                  name="districtId"
                  value={filters.districtId}
                  onChange={handleFilterChange}
                  className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
                >
                  <option value="">All Districts</option>
                  {districts.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
              </div>
            </>
          )}

          {activeReport === 'patient' && (userRole === ROLES.SUPER_ADMIN || userRole === ROLES.SYSTEM_ADMIN) && (
            <div>
              <label className="text-xs font-semibold text-surface-500 block mb-1">Block</label>
              <select
                name="blockId"
                value={filters.blockId}
                onChange={handleFilterChange}
                className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
              >
                <option value="">All Blocks</option>
                {blocks.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
              </select>
            </div>
          )}

          {/* Date Pickers */}
          {['sales', 'patient', 'bill', 'revenue', 'activity'].includes(activeReport) && (
            <>
              <div>
                <label className="text-xs font-semibold text-surface-500 block mb-1">Start Date</label>
                <input
                  type="date"
                  name="startDate"
                  value={filters.startDate}
                  onChange={handleFilterChange}
                  className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
                />
              </div>
              <div>
                <label className="text-xs font-semibold text-surface-500 block mb-1">End Date</label>
                <input
                  type="date"
                  name="endDate"
                  value={filters.endDate}
                  onChange={handleFilterChange}
                  className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
                />
              </div>
            </>
          )}

          {/* Commission specific */}
          {activeReport === 'commission' && (
            <>
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
                <label className="text-xs font-semibold text-surface-500 block mb-1">Role Level</label>
                <select
                  name="roleId"
                  value={filters.roleId}
                  onChange={handleFilterChange}
                  className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
                >
                  <option value="">All Roles</option>
                  <option value="5">Block Admin</option>
                  <option value="4">District Admin</option>
                  <option value="3">State Admin</option>
                  <option value="6">Pharmacist</option>
                  <option value="7">Receptionist</option>
                </select>
              </div>
            </>
          )}

          {/* Bill Status */}
          {activeReport === 'bill' && (
            <div>
              <label className="text-xs font-semibold text-surface-500 block mb-1">Bill Status</label>
              <select
                name="status"
                value={filters.status}
                onChange={handleFilterChange}
                className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
              >
                <option value="">All Statuses</option>
                <option value="ACTIVE">Active</option>
                <option value="CANCELLED">Cancelled</option>
                <option value="RETURNED">Returned</option>
              </select>
            </div>
          )}

          {/* Expiry threshold */}
          {activeReport === 'expiry' && (
            <div>
              <label className="text-xs font-semibold text-surface-500 block mb-1">Expiring within (Days)</label>
              <select
                name="days"
                value={filters.days}
                onChange={handleFilterChange}
                className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
              >
                <option value="30">30 Days</option>
                <option value="60">60 Days</option>
                <option value="90">90 Days</option>
                <option value="180">180 Days</option>
              </select>
            </div>
          )}

          {/* Audit Logs Specific */}
          {activeReport === 'activity' && (
            <>
              <div>
                <label className="text-xs font-semibold text-surface-500 block mb-1">User ID</label>
                <input
                  type="number"
                  name="userId"
                  value={filters.userId}
                  onChange={handleFilterChange}
                  placeholder="Filter User ID"
                  className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
                />
              </div>
              <div>
                <label className="text-xs font-semibold text-surface-500 block mb-1">Action Name</label>
                <input
                  type="text"
                  name="action"
                  value={filters.action}
                  onChange={handleFilterChange}
                  placeholder="e.g. LOGIN_SUCCESS"
                  className="w-full text-sm border border-surface-200 rounded-lg p-2 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500"
                />
              </div>
            </>
          )}
        </div>
      </div>

      {/* Data Preview Table */}
      {loading ? (
        <div className="flex justify-center py-12">
          <Loader className="w-8 h-8 text-primary-600 animate-spin" />
        </div>
      ) : data.length === 0 ? (
        <div className="bg-white p-12 text-center border border-surface-200 rounded-xl shadow-sm">
          <AlertTriangle className="w-12 h-12 text-surface-300 mx-auto mb-3" />
          <p className="text-surface-500 font-medium">No records found matching the specified parameters.</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-surface-200 shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-surface-100 flex items-center justify-between bg-surface-50/50">
            <h4 className="font-bold text-surface-900 text-sm">Data Preview ({data.length} records)</h4>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm border-collapse">
              <thead>
                <tr className="bg-surface-50 border-b border-surface-200">
                  {columns.map((c) => (
                    <th key={c.key} className={`p-4 font-semibold text-surface-600 text-xs uppercase tracking-wider ${c.num ? 'text-right' : ''}`}>
                      {c.label}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-100">
                {data.map((row, idx) => (
                  <tr key={idx} className="hover:bg-surface-50/50 transition-colors">
                    {columns.map((c) => {
                      const val = row[c.key];
                      return (
                        <td key={c.key} className={`p-4 text-surface-700 ${c.num ? 'text-right font-bold' : ''}`}>
                          {val === null || val === undefined ? '—' : typeof val === 'number' && c.num ? val.toFixed(2) : val.toString()}
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
