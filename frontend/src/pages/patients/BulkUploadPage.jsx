import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Upload, ChevronLeft, AlertCircle, CheckCircle, HelpCircle, FileText, Info } from 'lucide-react';
import { patientApi } from '@/api/patientApi';
import { PageHeader } from '@/components/common/PageHeader';
import { ROUTES } from '@/constants/routePaths';

export default function BulkUploadPage() {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleFileChange = (e) => {
    setError('');
    setResult(null);
    if (e.target.files && e.target.files[0]) {
      const selectedFile = e.target.files[0];
      if (!selectedFile.name.endsWith('.csv')) {
        setError('Only CSV files are supported.');
        setFile(null);
        return;
      }
      setFile(selectedFile);
    }
  };

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);
    setError('');
    setResult(null);

    try {
      const res = await patientApi.bulkUpload(file);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload file.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="animate-fade-in p-6">
      <div className="flex items-center gap-2 mb-4">
        <button 
          onClick={() => navigate(ROUTES.PATIENTS)} 
          className="p-1 hover:bg-surface-100 rounded-lg text-surface-600 cursor-pointer"
        >
          <ChevronLeft className="w-5 h-5" />
        </button>
        <span className="text-sm font-semibold text-surface-500">Back to Patients</span>
      </div>

      <PageHeader 
        title="Bulk Import Patients" 
        description="Upload a CSV spreadsheet containing patient information"
      />

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Left: Drag and Drop */}
        <div className="md:col-span-2 space-y-6">
          <div className="bg-white rounded-2xl border border-surface-200/60 p-8 shadow-card flex flex-col items-center justify-center border-dashed border-2 border-surface-300">
            <div className="w-16 h-16 rounded-full bg-primary-50 flex items-center justify-center text-primary-600 mb-4">
              <Upload className="w-8 h-8 animate-pulse" />
            </div>
            
            <p className="text-sm font-semibold text-surface-900 mb-1">Upload your CSV spreadsheet</p>
            <p className="text-xs text-surface-500 mb-6 text-center max-w-sm">
              Make sure your CSV contains columns for <strong>name</strong>, <strong>phone</strong>, and <strong>gender</strong> at minimum.
            </p>

            <input 
              type="file" 
              accept=".csv"
              onChange={handleFileChange}
              id="csv-file-input"
              className="hidden" 
            />
            <label 
              htmlFor="csv-file-input" 
              className="px-4 py-2.5 bg-primary-600 hover:bg-primary-700 text-white rounded-xl text-sm font-semibold cursor-pointer shadow-lg shadow-primary-600/10 mb-4"
            >
              Select CSV File
            </label>

            {file && (
              <div className="flex items-center gap-2 px-3 py-1.5 bg-surface-50 rounded-lg border border-surface-200">
                <FileText className="w-4 h-4 text-surface-500" />
                <span className="text-xs font-semibold text-surface-700">{file.name}</span>
                <span className="text-[10px] text-surface-400">({(file.size / 1024).toFixed(1)} KB)</span>
              </div>
            )}
          </div>

          {error && (
            <div className="p-4 bg-red-50 text-red-700 rounded-xl text-sm flex items-center gap-3 border border-red-100">
              <AlertCircle className="w-5 h-5 shrink-0" />
              <div>{error}</div>
            </div>
          )}

          {file && !result && (
            <button 
              onClick={handleUpload}
              disabled={uploading}
              className="w-full py-3 bg-primary-600 hover:bg-primary-700 disabled:bg-primary-400 text-white rounded-xl text-sm font-bold shadow-lg shadow-primary-600/10 cursor-pointer flex items-center justify-center gap-2"
            >
              {uploading ? 'Processing file...' : 'Start Import'}
            </button>
          )}

          {result && (
            <div className="bg-white rounded-2xl border border-surface-200/60 p-6 shadow-card space-y-4">
              <h3 className="text-sm font-bold text-surface-900 flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-green-600" />
                Import Summary
              </h3>
              <div className="grid grid-cols-4 gap-4">
                <div className="bg-surface-50 p-4 rounded-xl border border-surface-100 text-center">
                  <div className="text-xl font-bold text-surface-900">{result.total}</div>
                  <div className="text-[10px] font-semibold text-surface-500 uppercase tracking-wider">Total Rows</div>
                </div>
                <div className="bg-green-50 p-4 rounded-xl border border-green-100 text-center">
                  <div className="text-xl font-bold text-green-700">{result.success}</div>
                  <div className="text-[10px] font-semibold text-green-600 uppercase tracking-wider">Imported</div>
                </div>
                <div className="bg-yellow-50 p-4 rounded-xl border border-yellow-100 text-center">
                  <div className="text-xl font-bold text-yellow-700">{result.skipped}</div>
                  <div className="text-[10px] font-semibold text-yellow-600 uppercase tracking-wider">Skipped (Dup)</div>
                </div>
                <div className="bg-red-50 p-4 rounded-xl border border-red-100 text-center">
                  <div className="text-xl font-bold text-red-700">{result.invalid}</div>
                  <div className="text-[10px] font-semibold text-red-600 uppercase tracking-wider">Invalid</div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Right: Help / Formatting Rules */}
        <div className="space-y-4">
          <div className="bg-white rounded-2xl border border-surface-200/60 p-5 shadow-card space-y-4">
            <h4 className="text-xs font-bold uppercase tracking-wider text-surface-500 flex items-center gap-1.5">
              <Info className="w-4 h-4 text-surface-400" />
              CSV Format Rules
            </h4>
            <ul className="text-xs text-surface-600 space-y-3">
              <li>
                <strong>Required Columns:</strong><br />
                <code>name</code> / <code>full_name</code>, <code>phone</code>, <code>gender</code>
              </li>
              <li>
                <strong>Gender Values:</strong><br />
                Must be <code>MALE</code>, <code>FEMALE</code>, or <code>OTHER</code>.
              </li>
              <li>
                <strong>Phone Format:</strong><br />
                10 to 12 digits. Duplicate phone numbers are skipped automatically.
              </li>
              <li>
                <strong>Optional Columns:</strong><br />
                <code>age</code>, <code>email</code>, <code>address</code>, <code>messaging_pref</code>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
