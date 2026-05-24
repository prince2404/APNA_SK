import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  User,
  Mail,
  Phone,
  MapPin,
  Lock,
  Save,
  Edit3,
  Camera,
  Upload,
  FileText,
  CheckCircle2,
  AlertCircle,
  XCircle,
  Key,
  Landmark,
  Download,
  ExternalLink
} from 'lucide-react';
import { PageHeader } from '@/components/common/PageHeader';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { StatusBadge } from '@/components/common/StatusBadge';
import { Badge } from '@/components/common/Badge';
import { useAuthStore } from '@/store/useAuthStore';
import { useAuth } from '@/hooks/useAuth';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { ROLE_DISPLAY_NAMES } from '@/constants/roles';
import { profileApi } from '@/api/profileApi';
import { ROUTES } from '@/constants/routePaths';

export default function ProfilePage() {
  const user = useAuthStore((s) => s.user);
  const { changePassword } = useAuth();
  
  // Photo states
  const [photoUrl, setPhotoUrl] = useState(null);
  const [loadingPhoto, setLoadingPhoto] = useState(false);
  const [uploadingPhoto, setUploadingPhoto] = useState(false);

  // PW form states
  const [showPwForm, setShowPwForm] = useState(false);
  const [currentPw, setCurrentPw] = useState('');
  const [newPw, setNewPw] = useState('');
  const [confirmPw, setConfirmPw] = useState('');
  const [changingPw, setChangingPw] = useState(false);

  // KYC form states
  const [bankName, setBankName] = useState('');
  const [bankIfsc, setBankIfsc] = useState('');
  const [bankAccount, setBankAccount] = useState('');
  const [panNumber, setPanNumber] = useState('');
  const [aadhaarLastFour, setAadhaarLastFour] = useState('');
  const [aadhaarFile, setAadhaarFile] = useState(null);
  const [submittingKyc, setSubmittingKyc] = useState(false);
  const [downloadingDoc, setDownloadingDoc] = useState(false);

  // Initialize KYC fields when user updates
  useEffect(() => {
    if (user) {
      setBankName(user.bankName || '');
      setBankIfsc(user.bankIfsc || '');
      setBankAccount(user.bankAccount || '');
      setPanNumber(user.panNumber || '');
      setAadhaarLastFour(user.aadhaarLastFour || '');
    }
  }, [user]);

  // Fetch profile photo
  const fetchPhoto = useCallback(async () => {
    if (user?.profilePhotoUrl) {
      setLoadingPhoto(true);
      try {
        const res = await profileApi.getPhoto();
        const blob = new Blob([res.data], { type: res.headers['content-type'] });
        const url = window.URL.createObjectURL(blob);
        setPhotoUrl(url);
      } catch (err) {
        console.error('Failed to load profile photo', err);
      } finally {
        setLoadingPhoto(false);
      }
    } else {
      setPhotoUrl(null);
    }
  }, [user?.profilePhotoUrl]);

  useEffect(() => {
    fetchPhoto();
    return () => {
      if (photoUrl) {
        window.URL.revokeObjectURL(photoUrl);
      }
    };
  }, [fetchPhoto]);

  const getInitials = (name) =>
    name
      ?.split(' ')
      .map((n) => n[0])
      .join('')
      .slice(0, 2)
      .toUpperCase() || 'U';

  // Photo upload handler
  const handlePhotoChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      toast.warning('Please select an image file');
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      toast.warning('Image size should be less than 2MB');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    setUploadingPhoto(true);
    try {
      const res = await profileApi.uploadPhoto(formData);
      toast.success('Profile photo uploaded successfully');
      useAuthStore.setState({ user: res.data.data });
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setUploadingPhoto(false);
    }
  };

  // Password change handler
  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (!currentPw || !newPw || !confirmPw) {
      toast.warning('Please fill all password fields');
      return;
    }
    if (newPw !== confirmPw) {
      toast.warning('Passwords do not match');
      return;
    }
    if (newPw.length < 8) {
      toast.warning('Password must be at least 8 characters');
      return;
    }
    setChangingPw(true);
    try {
      await changePassword(currentPw, newPw, confirmPw);
      setShowPwForm(false);
      setCurrentPw('');
      setNewPw('');
      setConfirmPw('');
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setChangingPw(false);
    }
  };

  // KYC submit handler
  const handleKycSubmit = async (e) => {
    e.preventDefault();
    if (!bankName || !bankIfsc || !bankAccount || !panNumber || !aadhaarLastFour) {
      toast.warning('Please fill in all KYC details');
      return;
    }

    if (!/^\d{4}$/.test(aadhaarLastFour)) {
      toast.warning('Aadhaar Last 4 digits must contain exactly 4 digits');
      return;
    }

    if (!aadhaarFile && !user?.bankAccount) {
      toast.warning('Please upload your Aadhaar document');
      return;
    }

    const formData = new FormData();
    formData.append('bankName', bankName.trim());
    formData.append('bankIfsc', bankIfsc.trim().toUpperCase());
    formData.append('bankAccount', bankAccount.trim());
    formData.append('panNumber', panNumber.trim().toUpperCase());
    formData.append('aadhaarLastFour', aadhaarLastFour.trim());
    if (aadhaarFile) {
      formData.append('aadhaarFile', aadhaarFile);
    }

    setSubmittingKyc(true);
    try {
      const res = await profileApi.submitKyc(formData);
      toast.success('KYC details submitted successfully');
      useAuthStore.setState({ user: res.data.data });
      setAadhaarFile(null);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setSubmittingKyc(false);
    }
  };

  // Download user KYC document
  const handleDownloadKycDoc = async () => {
    setDownloadingDoc(true);
    try {
      const res = await profileApi.getKycDocument();
      const blob = new Blob([res.data], { type: res.headers['content-type'] });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `kyc_document_${user.id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      toast.success('KYC document downloaded successfully');
    } catch (error) {
      toast.error('Failed to download document: ' + getErrorMessage(error));
    } finally {
      setDownloadingDoc(false);
    }
  };

  const geoPath = [user?.stateName, user?.districtName, user?.blockName, user?.storeName]
    .filter(Boolean)
    .join(' → ');

  const hasKycSubmitted = !!user?.bankAccount;
  const isKycPending = user?.verificationStatus === 'PENDING' && hasKycSubmitted;
  const isKycVerified = user?.verificationStatus === 'VERIFIED';
  const isKycRejected = user?.verificationStatus === 'REJECTED';
  const showKycForm = !hasKycSubmitted || isKycRejected;

  return (
    <div className="animate-fade-in max-w-4xl mx-auto space-y-6">
      <PageHeader title="My Profile" description="View and manage your account information and verification status" />

      {/* Profile Header Card */}
      <div className="bg-white rounded-xl border border-surface-200/60 shadow-card overflow-hidden">
        <div className="bg-gradient-to-r from-primary-600 via-primary-700 to-primary-800 px-6 py-8 relative overflow-hidden">
          <div className="absolute right-0 top-0 w-48 h-48 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/2" />
          <div className="relative flex items-center gap-5">
            {/* Avatar & Photo Upload */}
            <div className="relative group shrink-0">
              <div className="w-20 h-20 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center text-2xl font-bold text-white shadow-lg overflow-hidden border border-white/30">
                {uploadingPhoto || loadingPhoto ? (
                  <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-white" />
                ) : photoUrl ? (
                  <img src={photoUrl} alt={user?.fullName} className="w-full h-full object-cover" />
                ) : (
                  getInitials(user?.fullName)
                )}
              </div>
              <label className="absolute -bottom-1.5 -right-1.5 w-7 h-7 bg-white text-primary-700 rounded-full flex items-center justify-center cursor-pointer shadow-md border border-slate-100 hover:bg-slate-50 transition-colors">
                <Camera className="w-3.5 h-3.5" />
                <input
                  type="file"
                  className="hidden"
                  accept="image/*"
                  onChange={handlePhotoChange}
                  disabled={uploadingPhoto || loadingPhoto}
                />
              </label>
            </div>
            
            <div className="text-white">
              <h1 className="text-2xl font-bold">{user?.fullName || 'User'}</h1>
              <div className="flex items-center gap-3 mt-2">
                <Badge>{ROLE_DISPLAY_NAMES[user?.roleName] || user?.roleName}</Badge>
                <StatusBadge status={user?.status || 'ACTIVE'} />
              </div>
            </div>
          </div>
        </div>

        <div className="p-6 space-y-5">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center shrink-0">
                <Mail className="w-4 h-4 text-primary-600" />
              </div>
              <div>
                <p className="text-xs text-surface-500 mb-0.5">Email Address</p>
                <p className="text-sm font-medium text-surface-900">{user?.email || '—'}</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center shrink-0">
                <Phone className="w-4 h-4 text-primary-600" />
              </div>
              <div>
                <p className="text-xs text-surface-500 mb-0.5">Phone Number</p>
                <p className="text-sm font-medium text-surface-900">{user?.phone || '—'}</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center shrink-0">
                <User className="w-4 h-4 text-primary-600" />
              </div>
              <div>
                <p className="text-xs text-surface-500 mb-0.5">Gender</p>
                <p className="text-sm font-medium text-surface-900">{user?.gender || '—'}</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center shrink-0">
                <MapPin className="w-4 h-4 text-primary-600" />
              </div>
              <div>
                <p className="text-xs text-surface-500 mb-0.5">Geography</p>
                <p className="text-sm font-medium text-surface-900">{geoPath || 'Platform-wide access'}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left Side: KYC details form or card */}
        <div className="space-y-6">
          <div className="bg-white rounded-xl border border-surface-200/60 shadow-card p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-lg bg-emerald-50 flex items-center justify-center">
                  <Landmark className="w-4 h-4 text-emerald-600" />
                </div>
                <div>
                  <h3 className="text-sm font-semibold text-surface-900">KYC & Verification</h3>
                  <p className="text-xs text-surface-500">Provide bank details and identity documents</p>
                </div>
              </div>
              <div>
                <Badge variant={isKycVerified ? 'success' : isKycPending ? 'warning' : isKycRejected ? 'danger' : 'secondary'}>
                  {isKycVerified ? 'Verified' : isKycPending ? 'Under Review' : isKycRejected ? 'Rejected' : 'Not Submitted'}
                </Badge>
              </div>
            </div>

            {/* Verification Status Alerts */}
            {isKycRejected && (
              <div className="p-4 rounded-xl bg-red-50 border border-red-150 flex items-start gap-3 mb-6">
                <XCircle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
                <div>
                  <h4 className="text-sm font-semibold text-red-800">KYC Verification Rejected</h4>
                  <p className="text-xs text-red-700 mt-1">
                    Your previous KYC submission was rejected by an administrator. Please correct the fields below and upload a valid Aadhaar document to re-submit.
                  </p>
                </div>
              </div>
            )}

            {isKycPending && (
              <div className="p-4 rounded-xl bg-amber-50 border border-amber-150 flex items-start gap-3 mb-6">
                <AlertCircle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
                <div>
                  <h4 className="text-sm font-semibold text-amber-800">KYC Under Review</h4>
                  <p className="text-xs text-amber-700 mt-1">
                    Your details are being reviewed by administrators. Editing is disabled during review.
                  </p>
                </div>
              </div>
            )}

            {isKycVerified && (
              <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-150 flex items-start gap-3 mb-6">
                <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
                <div>
                  <h4 className="text-sm font-semibold text-emerald-800">KYC Verified</h4>
                  <p className="text-xs text-emerald-700 mt-1">
                    Your account KYC has been fully verified. You have full access to retail actions.
                  </p>
                </div>
              </div>
            )}

            {showKycForm ? (
              /* Editable KYC Form */
              <form onSubmit={handleKycSubmit} className="space-y-4">
                <div className="pt-2 border-t border-surface-100">
                  <h4 className="text-xs font-semibold text-surface-600 uppercase tracking-wider mb-3">Bank Details</h4>
                  <div className="space-y-3">
                    <Input
                      label="Bank Name"
                      placeholder="e.g. State Bank of India"
                      value={bankName}
                      onChange={(e) => setBankName(e.target.value)}
                      required
                    />
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <Input
                        label="IFSC Code"
                        placeholder="e.g. SBIN0001234"
                        value={bankIfsc}
                        onChange={(e) => setBankIfsc(e.target.value)}
                        required
                      />
                      <Input
                        label="Account Number"
                        placeholder="Enter account number"
                        value={bankAccount}
                        onChange={(e) => setBankAccount(e.target.value)}
                        required
                      />
                    </div>
                  </div>
                </div>

                <div className="pt-4 border-t border-surface-100">
                  <h4 className="text-xs font-semibold text-surface-600 uppercase tracking-wider mb-3">Identity Details</h4>
                  <div className="space-y-3">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <Input
                        label="PAN Number"
                        placeholder="e.g. ABCDE1234F"
                        value={panNumber}
                        onChange={(e) => setPanNumber(e.target.value)}
                        required
                      />
                      <Input
                        label="Aadhaar (Last 4 Digits)"
                        placeholder="e.g. 5678"
                        maxLength={4}
                        value={aadhaarLastFour}
                        onChange={(e) => setAadhaarLastFour(e.target.value)}
                        required
                      />
                    </div>
                    
                    <div>
                      <label className="block text-xs font-semibold text-surface-700 mb-1">
                        Upload Aadhaar Document (PDF or Image)
                      </label>
                      <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-surface-300 border-dashed rounded-lg hover:border-primary-500 transition-colors">
                        <div className="space-y-1 text-center">
                          <Upload className="mx-auto h-10 w-10 text-surface-400" />
                          <div className="flex text-sm text-surface-600">
                            <label className="relative cursor-pointer bg-white rounded-md font-medium text-primary-600 hover:text-primary-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-primary-500">
                              <span>Upload a file</span>
                              <input
                                type="file"
                                className="sr-only"
                                accept=".pdf,image/*"
                                onChange={(e) => setAadhaarFile(e.target.files?.[0] || null)}
                              />
                            </label>
                            <p className="pl-1">or drag and drop</p>
                          </div>
                          <p className="text-xs text-surface-500">PDF, PNG, JPG up to 5MB</p>
                          {aadhaarFile && (
                            <p className="text-sm font-semibold text-emerald-600 mt-2 flex items-center justify-center gap-1">
                              <FileText className="w-4 h-4" /> {aadhaarFile.name}
                            </p>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="pt-4 flex justify-end">
                  <Button type="submit" loading={submittingKyc} className="w-full sm:w-auto">
                    <Save className="w-4 h-4 mr-2" /> Submit KYC
                  </Button>
                </div>
              </form>
            ) : (
              /* Read-only KYC display */
              <div className="space-y-6 pt-2 border-t border-surface-100">
                <div>
                  <h4 className="text-xs font-semibold text-surface-500 uppercase tracking-wider mb-3">Bank Details</h4>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm bg-slate-50 p-4 rounded-xl border border-slate-100">
                    <div>
                      <span className="text-xs text-surface-400 block">Bank Name</span>
                      <span className="font-semibold text-surface-900">{user?.bankName}</span>
                    </div>
                    <div>
                      <span className="text-xs text-surface-400 block">IFSC Code</span>
                      <span className="font-semibold text-surface-900 font-mono">{user?.bankIfsc}</span>
                    </div>
                    <div className="sm:col-span-2">
                      <span className="text-xs text-surface-400 block">Account Number</span>
                      <span className="font-semibold text-surface-900 font-mono">{user?.bankAccount}</span>
                    </div>
                  </div>
                </div>

                <div>
                  <h4 className="text-xs font-semibold text-surface-500 uppercase tracking-wider mb-3">Identity Details</h4>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm bg-slate-50 p-4 rounded-xl border border-slate-100">
                    <div>
                      <span className="text-xs text-surface-400 block">PAN Number</span>
                      <span className="font-semibold text-surface-900 font-mono">{user?.panNumber}</span>
                    </div>
                    <div>
                      <span className="text-xs text-surface-400 block">Aadhaar (Last 4 Digits)</span>
                      <span className="font-semibold text-surface-900 font-mono">XXXX XXXX {user?.aadhaarLastFour}</span>
                    </div>
                    <div className="sm:col-span-2 pt-2">
                      <Button
                        variant="secondary"
                        size="sm"
                        className="w-full flex items-center justify-center gap-1.5"
                        onClick={handleDownloadKycDoc}
                        loading={downloadingDoc}
                      >
                        <Download className="w-4 h-4" /> Download Submitted Aadhaar Doc
                      </Button>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Side: Security / pw change & Quick actions */}
        <div className="space-y-6">
          {/* Quick Actions Card */}
          <div className="bg-white rounded-xl border border-surface-200/60 shadow-card p-6">
            <h3 className="text-sm font-semibold text-surface-900 mb-4 flex items-center gap-2">
              <Key className="w-4 h-4 text-primary-600" />
              Quick Actions
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Link
                to={ROUTES.PERMISSION_REQUESTS}
                className="flex items-center justify-between p-4 rounded-xl border border-surface-150 hover:border-primary-500 hover:bg-primary-50/20 transition-all group"
              >
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center group-hover:bg-primary-100 transition-colors">
                    <Key className="w-4 h-4 text-primary-600" />
                  </div>
                  <div className="text-left">
                    <span className="text-sm font-semibold text-surface-900 block">Request Permissions</span>
                    <span className="text-xs text-surface-500 block mt-0.5">Ask for operational access</span>
                  </div>
                </div>
                <ExternalLink className="w-4 h-4 text-surface-400 group-hover:text-primary-600 transition-colors" />
              </Link>

              <Link
                to={ROUTES.SESSIONS}
                className="flex items-center justify-between p-4 rounded-xl border border-surface-150 hover:border-primary-500 hover:bg-primary-50/20 transition-all group"
              >
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center group-hover:bg-primary-100 transition-colors">
                    <Lock className="w-4 h-4 text-primary-600" />
                  </div>
                  <div className="text-left">
                    <span className="text-sm font-semibold text-surface-900 block">Manage Sessions</span>
                    <span className="text-xs text-surface-500 block mt-0.5">View & revoke active sessions</span>
                  </div>
                </div>
                <ExternalLink className="w-4 h-4 text-surface-400 group-hover:text-primary-600 transition-colors" />
              </Link>
            </div>
          </div>

          {/* Change Password Card */}
          <div className="bg-white rounded-xl border border-surface-200/60 shadow-card p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-lg bg-amber-50 flex items-center justify-center">
                  <Lock className="w-4 h-4 text-amber-600" />
                </div>
                <div>
                  <h3 className="text-sm font-semibold text-surface-900">Password & Security</h3>
                  <p className="text-xs text-surface-500">Update your password to keep your account secure</p>
                </div>
              </div>
              {!showPwForm && (
                <Button variant="secondary" size="sm" onClick={() => setShowPwForm(true)}>
                  <Edit3 className="w-3 h-3" /> Change Password
                </Button>
              )}
            </div>

            {showPwForm && (
              <form onSubmit={handleChangePassword} className="space-y-4 pt-2 border-t border-surface-100 mt-4">
                <div className="pt-4">
                  <Input
                    label="Current Password"
                    type="password"
                    placeholder="Enter current password"
                    value={currentPw}
                    onChange={(e) => setCurrentPw(e.target.value)}
                    autoFocus
                  />
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <Input
                    label="New Password"
                    type="password"
                    placeholder="Min 8 characters"
                    value={newPw}
                    onChange={(e) => setNewPw(e.target.value)}
                  />
                  <Input
                    label="Confirm New Password"
                    type="password"
                    placeholder="Re-enter new password"
                    value={confirmPw}
                    onChange={(e) => setConfirmPw(e.target.value)}
                  />
                </div>
                <div className="flex gap-3 pt-2">
                  <Button
                    variant="secondary"
                    type="button"
                    onClick={() => {
                      setShowPwForm(false);
                      setCurrentPw('');
                      setNewPw('');
                      setConfirmPw('');
                    }}
                  >
                    Cancel
                  </Button>
                  <Button type="submit" loading={changingPw}>
                    <Save className="w-4 h-4" /> Update Password
                  </Button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
