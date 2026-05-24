import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import { messageApi } from '@/api/messageApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { Loader } from '@/components/common/Loader';
import { Button } from '@/components/common/Button';
import { Pagination } from '@/components/common/Pagination';
import { ROUTES } from '@/constants/routePaths';
import { FileText, Plus, History, Mail, MessageSquare, AlertCircle } from 'lucide-react';

export default function MessageHistoryPage() {
  const navigate = useNavigate();

  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [size] = useState(10);

  const fetchHistory = async () => {
    setLoading(true);
    try {
      const res = await messageApi.getBulkMessageHistory({ page, size });
      setLogs(res.data.data?.content || []);
      setTotalElements(res.data.data?.totalElements || 0);
    } catch (err) {
      toast.error('Failed to load history logs: ' + getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  return (
    <div className="space-y-6 animate-fade-in text-slate-800">
      <PageHeader title="Broadcast Dispatch Logs" description="Review system broadcast message history, counts, and dispatch channels.">
        <div className="flex flex-wrap gap-2">
          <Button onClick={() => navigate(ROUTES.MESSAGES)} className="flex items-center gap-1.5 cursor-pointer">
            <Plus className="w-4 h-4" /> Send Message
          </Button>
          <Button variant="secondary" onClick={() => navigate(ROUTES.MESSAGES + '/templates')} className="flex items-center gap-1.5 cursor-pointer">
            <FileText className="w-4 h-4" /> Message Templates
          </Button>
        </div>
      </PageHeader>

      {loading ? (
        <div className="flex justify-center py-12">
          <Loader className="w-8 h-8 text-primary-600 animate-spin" />
        </div>
      ) : logs.length === 0 ? (
        <div className="bg-white p-12 text-center border border-surface-200 rounded-xl shadow-sm">
          <History className="w-12 h-12 text-surface-300 mx-auto mb-3" />
          <p className="text-surface-500 font-medium">No messages dispatched yet.</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-surface-200 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm border-collapse">
              <thead>
                <tr className="bg-surface-50 border-b border-surface-200">
                  <th className="p-4 font-semibold text-surface-600">Sender</th>
                  <th className="p-4 font-semibold text-surface-600">Channel</th>
                  <th className="p-4 font-semibold text-surface-600">Target Criteria</th>
                  <th className="p-4 font-semibold text-surface-600">Message Content</th>
                  <th className="p-4 font-semibold text-surface-600 text-center">Dispatched Count</th>
                  <th className="p-4 font-semibold text-surface-600 text-center">Status</th>
                  <th className="p-4 font-semibold text-surface-600">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-100">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-surface-50/50 transition-colors">
                    <td className="p-4 font-semibold text-surface-900">{log.senderName}</td>
                    <td className="p-4">
                      <span className={`px-2 py-0.5 text-xs font-semibold rounded-full flex items-center gap-1 w-fit ${
                        log.channel === 'EMAIL' ? 'bg-blue-50 text-blue-600 border border-blue-200' : 'bg-green-50 text-green-600 border border-green-200'
                      }`}>
                        {log.channel === 'EMAIL' ? <Mail className="w-3 h-3" /> : <MessageSquare className="w-3 h-3" />}
                        {log.channel}
                      </span>
                    </td>
                    <td className="p-4">
                      <p className="text-xs text-surface-600 font-semibold">{log.targetCriteria}</p>
                    </td>
                    <td className="p-4 max-w-xs truncate font-mono text-xs text-surface-500" title={log.content}>
                      {log.content}
                    </td>
                    <td className="p-4 text-center font-bold text-surface-900">{log.sentCount}</td>
                    <td className="p-4 text-center">
                      <span className={`px-2.5 py-0.5 text-xs font-bold rounded-full ${
                        log.status === 'SUCCESS'
                          ? 'bg-green-50 text-green-700 border border-green-200'
                          : 'bg-red-50 text-red-700 border border-red-200'
                      }`}>
                        {log.status}
                      </span>
                    </td>
                    <td className="p-4 text-xs text-surface-400">
                      {new Date(log.createdAt).toLocaleString('en-IN', {
                        day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
                      })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
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
  );
}
