import { useState, useEffect } from 'react';
import { Heart, Eye, EyeOff, ArrowRight } from 'lucide-react';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { useAuth } from '@/hooks/useAuth';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';
import { APP } from '@/constants/appConstants';
import { ToastContainer } from '@/components/common/ToastContainer';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();

  useEffect(() => {
    const logoutMsg = localStorage.getItem('logout_message');
    if (logoutMsg) {
      toast.error(logoutMsg);
      localStorage.removeItem('logout_message');
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email || !password) { toast.warning('Please fill in all fields'); return; }
    setLoading(true);
    try {
      await login(email, password);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left panel — branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-primary-700 via-primary-600 to-primary-800 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-20 left-20 w-72 h-72 bg-white rounded-full blur-3xl" />
          <div className="absolute bottom-20 right-20 w-96 h-96 bg-primary-300 rounded-full blur-3xl" />
        </div>
        <div className="relative flex flex-col justify-center px-16 text-white z-10">
          <div className="flex items-center gap-3 mb-8">
            <div className="w-14 h-14 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center">
              <Heart className="w-8 h-8 text-white" />
            </div>
          </div>
          <h1 className="text-4xl font-bold mb-3">{APP.NAME}</h1>
          <p className="text-xl text-primary-100 mb-8">Healthcare Retail Management Platform</p>
          <div className="space-y-4 text-primary-100/90">
            <div className="flex items-center gap-3">
              <div className="w-2 h-2 bg-primary-300 rounded-full" />
              <span>Managing 200+ stores across 3 states</span>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-2 h-2 bg-primary-300 rounded-full" />
              <span>Genuine medicines at affordable prices</span>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-2 h-2 bg-primary-300 rounded-full" />
              <span>Complete inventory & billing solution</span>
            </div>
          </div>
        </div>
      </div>

      {/* Right panel — login form */}
      <div className="flex-1 flex items-center justify-center p-6 bg-surface-50">
        <div className="w-full max-w-md">
          {/* Mobile logo */}
          <div className="lg:hidden flex items-center gap-3 mb-8">
            <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center">
              <Heart className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-lg font-bold text-surface-900">{APP.SHORT_NAME}</h1>
              <p className="text-xs text-surface-500">Healthcare ERP</p>
            </div>
          </div>

          <div className="bg-white rounded-2xl shadow-card p-8 border border-surface-200/60">
            <div className="mb-6">
              <h2 className="text-2xl font-bold text-surface-900">Welcome back</h2>
              <p className="text-sm text-surface-500 mt-1">Sign in to your account to continue</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
              <Input
                label="Email Address"
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
                autoFocus
              />

              <div className="relative">
                <Input
                  label="Password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-[38px] p-1 text-surface-400 hover:text-surface-600 cursor-pointer"
                  tabIndex={-1}
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>

              <Button type="submit" loading={loading} className="w-full" size="lg">
                Sign In <ArrowRight className="w-4 h-4" />
              </Button>
            </form>
          </div>

          <p className="text-center text-xs text-surface-400 mt-6">
            © 2026 {APP.NAME}. All rights reserved.
          </p>
        </div>
      </div>
      <ToastContainer />
    </div>
  );
}
