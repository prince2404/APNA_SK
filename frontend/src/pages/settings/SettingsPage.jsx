import { useState, useEffect } from 'react';
import { Settings, Save, RefreshCw } from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Loader } from '@/components/common/Loader';
import { EmptyState } from '@/components/common/EmptyState';
import axiosInstance from '@/api/axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';

export default function SettingsPage() {
  const [configs, setConfigs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editValues, setEditValues] = useState({});
  const [error, setError] = useState(null);

  const fetchConfigs = () => {
    setLoading(true);
    setError(null);
    axiosInstance.get(API_PATHS.SYSTEM_CONFIG)
      .then(res => {
        const data = res.data.data || [];
        setConfigs(data);
        const vals = {};
        data.forEach(c => { vals[c.configKey] = c.configValue; });
        setEditValues(vals);
      })
      .catch(e => {
        const msg = getErrorMessage(e);
        setError(msg);
        // Only toast on non-404 errors (404 means endpoint missing)
        if (e.response?.status !== 404) {
          toast.error(msg);
        }
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchConfigs(); }, []);

  const handleSave = async () => {
    setSaving(true);
    try {
      const entries = Object.entries(editValues).map(([key, value]) => ({ configKey: key, configValue: value }));
      await axiosInstance.put(API_PATHS.SYSTEM_CONFIG, { configs: entries });
      toast.success('Settings saved successfully');
    } catch (e) {
      toast.error(getErrorMessage(e));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <Loader text="Loading settings..." />;

  if (error) {
    return (
      <div className="animate-fade-in max-w-2xl mx-auto">
        <PageHeader title="System Settings" description="Configure system-wide settings (Super Admin only)" />
        <div className="bg-white rounded-xl border border-surface-200/60 shadow-card p-8 text-center">
          <Settings className="w-12 h-12 text-surface-300 mx-auto mb-3" />
          <h3 className="text-sm font-semibold text-surface-700 mb-1">Unable to load settings</h3>
          <p className="text-xs text-surface-400 mb-4">{error}</p>
          <Button variant="secondary" size="sm" onClick={fetchConfigs}>
            <RefreshCw className="w-3 h-3" /> Retry
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in max-w-2xl mx-auto">
      <PageHeader title="System Settings" description="Configure system-wide settings (Super Admin only)" />

      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card p-6">
        <div className="flex items-center gap-3 mb-6">
          <div className="w-10 h-10 rounded-xl bg-primary-50 flex items-center justify-center">
            <Settings className="w-5 h-5 text-primary-600" />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-surface-900">Configuration</h3>
            <p className="text-xs text-surface-500">Manage configurable system values</p>
          </div>
        </div>

        {configs.length === 0 ? (
          <EmptyState icon={Settings} title="No configurations" description="No system configurations found. Settings will appear here once configured." />
        ) : (
          <div className="space-y-4">
            {configs.map(config => (
              <div key={config.configKey} className="border border-surface-200 rounded-lg p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1">
                    <p className="text-sm font-medium text-surface-900 font-mono">{config.configKey}</p>
                    {config.description && (
                      <p className="text-xs text-surface-500 mt-0.5">{config.description}</p>
                    )}
                  </div>
                  <div className="w-48 shrink-0">
                    <Input
                      value={editValues[config.configKey] || ''}
                      onChange={(e) => setEditValues(prev => ({ ...prev, [config.configKey]: e.target.value }))}
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="flex justify-end pt-6">
          <Button onClick={handleSave} loading={saving}><Save className="w-4 h-4" /> Save Settings</Button>
        </div>
      </div>
    </div>
  );
}
