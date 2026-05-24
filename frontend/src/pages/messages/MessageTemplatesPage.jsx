import { useState, useEffect } from 'react';
import { PageHeader } from '@/components/common/PageHeader';
import { messageApi } from '@/api/messageApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { Loader } from '@/components/common/Loader';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Modal } from '@/components/common/Modal';
import { FileCode, Plus, Edit2, Trash2, Mail, MessageSquare, AlertCircle } from 'lucide-react';

export default function MessageTemplatesPage() {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState(null);
  
  const [form, setForm] = useState({
    name: '',
    channel: 'EMAIL',
    content: ''
  });

  const fetchTemplates = async () => {
    setLoading(true);
    try {
      const res = await messageApi.getTemplates();
      setTemplates(res.data.data || []);
    } catch (err) {
      toast.error('Failed to load templates: ' + getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTemplates();
  }, []);

  const handleOpenCreate = () => {
    setEditingTemplate(null);
    setForm({ name: '', channel: 'EMAIL', content: '' });
    setModalOpen(true);
  };

  const handleOpenEdit = (t) => {
    setEditingTemplate(t);
    setForm({ name: t.name, channel: t.channel, content: t.content });
    setModalOpen(true);
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form.name.trim() || !form.content.trim()) {
      toast.warning('Please fill in all required fields');
      return;
    }

    try {
      if (editingTemplate) {
        await messageApi.updateTemplate(editingTemplate.id, form);
        toast.success('Template updated successfully');
      } else {
        await messageApi.createTemplate(form);
        toast.success('Template created successfully');
      }
      setModalOpen(false);
      fetchTemplates();
    } catch (err) {
      toast.error('Failed to save template: ' + getErrorMessage(err));
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this template?')) return;
    try {
      await messageApi.deleteTemplate(id);
      toast.success('Template deleted successfully');
      fetchTemplates();
    } catch (err) {
      toast.error('Failed to delete template: ' + getErrorMessage(err));
    }
  };

  return (
    <div className="space-y-6 animate-fade-in text-slate-800">
      <PageHeader title="Message Templates" description="Manage pre-approved communication templates for broadcast dispatches.">
        <Button onClick={handleOpenCreate} className="flex items-center gap-1.5 cursor-pointer">
          <Plus className="w-4 h-4" /> Create Template
        </Button>
      </PageHeader>

      {loading ? (
        <div className="flex justify-center py-12">
          <Loader className="w-8 h-8 text-primary-600 animate-spin" />
        </div>
      ) : templates.length === 0 ? (
        <div className="bg-white p-12 text-center border border-surface-200 rounded-xl shadow-sm">
          <FileCode className="w-12 h-12 text-surface-300 mx-auto mb-3" />
          <p className="text-surface-500 font-medium">No templates configured yet. Click above to add your first template.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {templates.map((t) => (
            <div key={t.id} className="bg-white rounded-xl border border-surface-200 shadow-sm p-5 hover:shadow-md transition-all flex flex-col justify-between space-y-4">
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <span className={`px-2 py-0.5 text-xs font-semibold rounded-full flex items-center gap-1 ${
                    t.channel === 'EMAIL' ? 'bg-blue-50 text-blue-600 border border-blue-200' : 'bg-green-50 text-green-600 border border-green-200'
                  }`}>
                    {t.channel === 'EMAIL' ? <Mail className="w-3 h-3" /> : <MessageSquare className="w-3 h-3" />}
                    {t.channel}
                  </span>
                  <div className="flex items-center gap-1">
                    <button onClick={() => handleOpenEdit(t)} className="p-1 hover:bg-slate-50 rounded-lg text-slate-500 hover:text-slate-800 transition-colors cursor-pointer">
                      <Edit2 className="w-4 h-4" />
                    </button>
                    <button onClick={() => handleDelete(t.id)} className="p-1 hover:bg-red-50 rounded-lg text-red-500 hover:text-red-700 transition-colors cursor-pointer">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
                <h4 className="font-bold text-surface-900 text-md">{t.name}</h4>
                <div className="bg-slate-50 p-3 rounded-lg border border-slate-200/50 min-h-[100px] text-xs text-slate-600 font-mono whitespace-pre-wrap leading-relaxed">
                  {t.content}
                </div>
              </div>
              <p className="text-[10px] text-slate-400">
                Created: {new Date(t.createdAt).toLocaleDateString('en-IN')}
              </p>
            </div>
          ))}
        </div>
      )}

      {/* Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editingTemplate ? 'Edit Template' : 'Create Template'}>
        <form onSubmit={handleSave} className="space-y-4 pt-2">
          <div>
            <label className="text-xs font-semibold text-slate-600 block mb-1">Template Name</label>
            <Input
              name="name"
              placeholder="e.g. Welcome Broadcast"
              value={form.name}
              onChange={handleFormChange}
              required
            />
          </div>
          <div>
            <label className="text-xs font-semibold text-slate-600 block mb-1">Dispatch Channel</label>
            <select
              name="channel"
              value={form.channel}
              onChange={handleFormChange}
              className="w-full text-sm border border-surface-200 rounded-lg p-2.5 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500 font-semibold"
            >
              <option value="EMAIL">Email Channel</option>
              <option value="SMS">SMS Channel (MSG91)</option>
            </select>
          </div>
          <div>
            <label className="text-xs font-semibold text-slate-600 block mb-1">Template Content</label>
            <textarea
              name="content"
              rows="5"
              placeholder="Write template. Support placeholders like {{name}} and {{role}}."
              value={form.content}
              onChange={handleFormChange}
              required
              className="w-full text-sm border border-surface-200 rounded-lg p-2.5 bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary-500 font-mono leading-relaxed"
            />
            <div className="flex items-center gap-1.5 mt-1.5 text-[10px] text-slate-400">
              <AlertCircle className="w-3 h-3 text-primary-500" />
              <span>Use <strong>&#123;&#123;name&#125;&#125;</strong> and <strong>&#123;&#123;role&#125;&#125;</strong> to inject dynamic user values.</span>
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-3 border-t">
            <Button variant="secondary" type="button" onClick={() => setModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit">
              Save Template
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
