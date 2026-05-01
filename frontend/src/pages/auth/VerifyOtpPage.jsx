import { useState, useRef, useEffect } from 'react';
import { ShieldCheck, ArrowRight, RotateCw } from 'lucide-react';
import { Navigate } from 'react-router-dom';
import { Button } from '@/components/common/Button';
import { useAuth } from '@/hooks/useAuth';
import { useAuthStore } from '@/store/useAuthStore';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { ROUTES } from '@/constants/routePaths';
import { ToastContainer } from '@/components/common/ToastContainer';

export default function VerifyOtpPage() {
  const [otp, setOtp] = useState(Array(6).fill(''));
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const inputRefs = useRef([]);
  const { verifyOtp, resendOtp } = useAuth();
  const { requiresTwoFactor, twoFactorEmail } = useAuthStore();

  useEffect(() => { inputRefs.current[0]?.focus(); }, []);

  if (!requiresTwoFactor) return <Navigate to={ROUTES.LOGIN} replace />;

  const handleChange = (index, value) => {
    if (!/^\d*$/.test(value)) return;
    const newOtp = [...otp];
    newOtp[index] = value.slice(-1);
    setOtp(newOtp);
    if (value && index < 5) inputRefs.current[index + 1]?.focus();
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    const newOtp = [...otp];
    pasted.split('').forEach((char, i) => { newOtp[i] = char; });
    setOtp(newOtp);
    inputRefs.current[Math.min(pasted.length, 5)]?.focus();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const code = otp.join('');
    if (code.length !== 6) { toast.warning('Please enter the complete 6-digit OTP'); return; }
    setLoading(true);
    try { await verifyOtp(code); }
    catch (error) { toast.error(getErrorMessage(error)); }
    finally { setLoading(false); }
  };

  const handleResend = async () => {
    setResending(true);
    try { await resendOtp(); }
    catch (error) { toast.error(getErrorMessage(error)); }
    finally { setResending(false); }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-surface-50">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-2xl shadow-card p-8 border border-surface-200/60">
          <div className="flex flex-col items-center mb-8">
            <div className="w-16 h-16 rounded-2xl bg-primary-100 flex items-center justify-center mb-4">
              <ShieldCheck className="w-8 h-8 text-primary-600" />
            </div>
            <h2 className="text-2xl font-bold text-surface-900">Verify Your Identity</h2>
            <p className="text-sm text-surface-500 mt-2 text-center">
              We sent a 6-digit OTP to <span className="font-medium text-surface-700">{twoFactorEmail}</span>
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="flex justify-center gap-3" onPaste={handlePaste}>
              {otp.map((digit, index) => (
                <input
                  key={index}
                  ref={(el) => (inputRefs.current[index] = el)}
                  type="text"
                  inputMode="numeric"
                  maxLength={1}
                  value={digit}
                  onChange={(e) => handleChange(index, e.target.value)}
                  onKeyDown={(e) => handleKeyDown(index, e)}
                  className="w-12 h-14 text-center text-xl font-bold rounded-xl border border-surface-300 bg-surface-50 text-surface-900 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-all"
                />
              ))}
            </div>

            <Button type="submit" loading={loading} className="w-full" size="lg">
              Verify & Continue <ArrowRight className="w-4 h-4" />
            </Button>
          </form>

          <div className="mt-5 text-center">
            <button
              onClick={handleResend}
              disabled={resending}
              className="inline-flex items-center gap-1.5 text-sm text-primary-600 hover:text-primary-700 font-medium disabled:opacity-50 cursor-pointer"
            >
              <RotateCw className={`w-3.5 h-3.5 ${resending ? 'animate-spin' : ''}`} />
              Resend OTP
            </button>
          </div>
        </div>
      </div>
      <ToastContainer />
    </div>
  );
}
