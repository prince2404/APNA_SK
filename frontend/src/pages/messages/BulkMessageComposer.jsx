import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import { messageApi } from '@/api/messageApi';
import { geographyApi } from '@/api/geographyApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { Loader } from '@/components/common/Loader';
import { Button } from '@/components/common/Button';
import { ROUTES } from '@/constants/routePaths';
import { Send, Users, Filter, FileText, ArrowLeft, History } from 'lucide-react';

export default function BulkMessageComposer() {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);

  // Metadata lists
  const [templates, setTemplates] = useState([]);
  const [stores, setStores] = useState([]);
  const [states, setStates] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [blocks, setBlocks] = useState([]);

  // Form selections
  const [channel, setChannel] = useState('EMAIL');
  const [targetRole, setTargetRole] = useState('');
  const [stateId, setStateId] = useState('');
  const [districtId, setDistrictId] = useState('');
  const [blockId, setBlockId] = useState('');
  const [storeId, setStoreId] = useState('');

  const [useTemplate, setUseTemplate] = useState(true);
  const [selectedTemplateId, setSelectedTemplateId] = useState('');
  const [customText, setCustomText] = useState('');

  useEffect(() => {
    const loadMetadata = async () => {
      setLoading(true);
      try {
        const tempRes = await messageApi.getTemplates();
        setTemplates(tempRes.data.data || []);

        const stateRes = await geographyApi.getStates({ size: 100 });
        setStates(stateRes.data.data?.content || []);

        const distRes = await geographyApi.getDistricts({ size: 100 });
        setDistricts(distRes.data.data?.content || []);

        const blockRes = await geographyApi.getBlocks({ size: 100 });
        setBlocks(blockRes.data.data?.content || []);

        const storeRes = await geographyApi.getStores({ size: 100 });
        setStores(storeRes.data.data?.content || []);
      } catch (err) {
        toast.error('Failed to load filter parameters: ' + getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    loadMetadata();
  }, []);

  const handleSend = async (e) => {
    e.preventDefault();
    if (useTemplate && !selectedTemplateId) {
      toast.warning('Please select a template to send');
      return;
    }
    if (!useTemplate && !customText.trim()) {
      toast.warning('Please compose custom content');
      return;
    }

    setSending(true);
    try {
      const payload = {
        channel,
        targetRole: targetRole || undefined,
        stateId: stateId ? parseInt(stateId, 10) : undefined,
        districtId: districtId ? parseInt(districtId, 10) : undefined,
        blockId: blockId ? parseInt(blockId, 10) : undefined,
        storeId: storeId ? parseInt(storeId, 10) : undefined,
        templateId: useTemplate ? parseInt(selectedTemplateId, 10) : undefined,
        customText: useTemplate ? undefined : customText.trim(),
      };

      const res = await messageApi.sendBulkMessage(payload);
      toast.success(`Dispatched broadcast successfully to ${res.data.data?.sentCount || 0} users`);
      navigate(ROUTES.MESSAGES + '/history');
    } catch (err) {
      toast.error('Failed to send bulk message: ' + getErrorMessage(err));
    } finally {
      setSending(false);
    }
  };

  const activeTemplates = templates.filter(t => t.channel === channel);

  return (
    <div className="space-y-6 animate-fade-in text-slate-800">
      <PageHeader title="Bulk Message Composer" description="Broadcast dynamic templates or custom dispatches to coordinates.">
        <div className="flex flex-wrap gap-2">
          <Button variant="secondary" onClick={() => navigate(ROUTES.MESSAGES + '/history')} className="flex items-center gap-1.5 cursor-pointer">
            <History className="w-4 h-4" /> Dispatch History
          </Button>
          <Button variant="secondary" onClick={() => navigate(ROUTES.MESSAGES + '/templates')} className="flex items-center gap-1.5 cursor-pointer">
            <FileText className="w-4 h-4" /> Manage Templates
          </Button>
        </div>
      </PageHeader>

      {loading ? (
        <div className="flex justify-center py-12">
          <Loader className="w-8 h-8 text-primary-600 animate-spin" />
        </div>
      ) : (
        <form onSubmit={handleSend} className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Target Filters panel */}
          <div className="lg:col-span-1 bg-white p-5 rounded-xl border border-surface-200 shadow-sm space-y-4">
            <h3 className="font-bold text-sm text-surface-900 border-b pb-2 flex items-center gap-2">
              <Filter className="w-4 h-4 text-primary-600" /> Target Filters
            </h3>

            <div>
              <label className="text-xs font-semibold text-slate-600 block mb-1">State Scope</label>
              <select
                value={stateId}
                onChange={(e) => { setStateId(e.target.value); setDistrictId(''); setBlockId(''); setStoreId(''); }}
                className="w-full text-sm border border-surface-200 rounded-lg p-2.5 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500 font-semibold"
              >
                <option value="">All States</option>
                {states.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-600 block mb-1">District Scope</label>
              <select
                value={districtId}
                onChange={(e) => { setDistrictId(e.target.value); setBlockId(''); setStoreId(''); }}
                disabled={!stateId}
                className="w-full text-sm border border-surface-200 rounded-lg p-2.5 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500 font-semibold"
              >
                <option value="">All Districts</option>
                {districts.filter(d => d.state?.id === parseInt(stateId, 10)).map(d => (
                  <option key={d.id} value={d.id}>{d.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-600 block mb-1">Block Scope</label>
              <select
                value={blockId}
                onChange={(e) => { setBlockId(e.target.value); setStoreId(''); }}
                disabled={!districtId}
                className="w-full text-sm border border-surface-200 rounded-lg p-2.5 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500 font-semibold"
              >
                <option value="">All Blocks</option>
                {blocks.filter(b => b.district?.id === parseInt(districtId, 10)).map(b => (
                  <option key={b.id} value={b.id}>{b.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-600 block mb-1">Store / Centre Scope</label>
              <select
                value={storeId}
                onChange={(e) => setStoreId(e.target.value)}
                disabled={!blockId}
                className="w-full text-sm border border-surface-200 rounded-lg p-2.5 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500 font-semibold"
              >
                <option value="">All Stores</option>
                {stores.filter(s => s.block?.id === parseInt(blockId, 10)).map(s => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-600 block mb-1">Role Level Filter</label>
              <select
                value={targetRole}
                onChange={(e) => setTargetRole(e.target.value)}
                className="w-full text-sm border border-surface-200 rounded-lg p-2.5 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500 font-semibold"
              >
                <option value="">All Scoped Users</option>
                <option value="STATE_ADMIN">State Admins</option>
                <option value="DISTRICT_ADMIN">District Admins</option>
                <option value="BLOCK_ADMIN">Block Admins</option>
                <option value="PHARMACIST">Pharmacists</option>
                <option value="RECEPTIONIST">Receptionists</option>
              </select>
            </div>
          </div>

          {/* Composer body */}
          <div className="lg:col-span-2 bg-white p-6 rounded-xl border border-surface-200 shadow-sm space-y-6">
            <h3 className="font-bold text-sm text-surface-900 border-b pb-2 flex items-center gap-2">
              <Users className="w-4 h-4 text-primary-600" /> Channel & Content Composer
            </h3>

            {/* Channel Selection */}
            <div className="grid grid-cols-2 gap-4">
              <button
                type="button"
                onClick={() => { setChannel('EMAIL'); setSelectedTemplateId(''); }}
                className={`p-4 rounded-xl border font-bold text-sm text-center transition-all cursor-pointer ${
                  channel === 'EMAIL' ? 'border-primary-600 bg-primary-50/20 text-primary-600' : 'border-surface-200 bg-white'
                }`}
              >
                Email Broadcast Channel
              </button>
              <button
                type="button"
                onClick={() => { setChannel('SMS'); setSelectedTemplateId(''); }}
                className={`p-4 rounded-xl border font-bold text-sm text-center transition-all cursor-pointer ${
                  channel === 'SMS' ? 'border-primary-600 bg-primary-50/20 text-primary-600' : 'border-surface-200 bg-white'
                }`}
              >
                SMS Channel (MSG91)
              </button>
            </div>

            {/* Template or Custom toggler */}
            <div className="flex gap-4 border-b pb-4">
              <button
                type="button"
                onClick={() => setUseTemplate(true)}
                className={`text-xs font-semibold pb-2 border-b-2 transition-all cursor-pointer ${
                  useTemplate ? 'border-primary-600 text-primary-600 font-bold' : 'border-transparent text-slate-500'
                }`}
              >
                Use Preconfigured Template
              </button>
              <button
                type="button"
                onClick={() => setUseTemplate(false)}
                className={`text-xs font-semibold pb-2 border-b-2 transition-all cursor-pointer ${
                  !useTemplate ? 'border-primary-600 text-primary-600 font-bold' : 'border-transparent text-slate-500'
                }`}
              >
                Compose Custom Broadcast
              </button>
            </div>

            {useTemplate ? (
              <div className="space-y-4">
                <div>
                  <label className="text-xs font-semibold text-slate-600 block mb-1">Select {channel} Template</label>
                  <select
                    value={selectedTemplateId}
                    onChange={(e) => setSelectedTemplateId(e.target.value)}
                    className="w-full text-sm border border-surface-200 rounded-lg p-2.5 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500 font-semibold"
                  >
                    <option value="">Choose a Template</option>
                    {activeTemplates.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                  </select>
                </div>
                {selectedTemplateId && (
                  <div className="bg-slate-50 p-4 border border-slate-200 rounded-lg">
                    <p className="text-[10px] font-bold text-slate-400 uppercase mb-2">Template Preview</p>
                    <div className="text-xs font-mono text-slate-600 whitespace-pre-wrap leading-relaxed">
                      {templates.find(t => t.id === parseInt(selectedTemplateId, 10))?.content}
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div>
                <label className="text-xs font-semibold text-slate-600 block mb-1">Custom Message Text</label>
                <textarea
                  rows="6"
                  placeholder="Type message content. Interpolate user values using {{name}} or {{role}}."
                  value={customText}
                  onChange={(e) => setCustomText(e.target.value)}
                  className="w-full text-sm border border-surface-200 rounded-lg p-2.5 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500 font-mono leading-relaxed"
                />
              </div>
            )}

            <div className="flex justify-end gap-3 pt-4 border-t">
              <Button
                type="button"
                variant="secondary"
                onClick={() => navigate(-1)}
              >
                Back
              </Button>
              <Button type="submit" disabled={sending} className="flex items-center gap-1.5">
                {sending ? <Loader className="w-4 h-4 text-white animate-spin" /> : <Send className="w-4 h-4" />}
                {sending ? 'Dispatching...' : 'Dispatch Broadcast'}
              </Button>
            </div>
          </div>
        </form>
      )}
    </div>
  );
}
