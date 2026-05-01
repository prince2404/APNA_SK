import { Menu, Bell, LogOut, User, ChevronDown } from 'lucide-react';
import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSidebarStore } from '@/store/useSidebarStore';
import { useAuthStore } from '@/store/useAuthStore';
import { useAuth } from '@/hooks/useAuth';
import { ROLE_DISPLAY_NAMES } from '@/constants/roles';
import { ROUTES } from '@/constants/routePaths';
import { cn } from '@/utils/cn';

export function Navbar() {
  const { toggleMobile } = useSidebarStore();
  const user = useAuthStore((s) => s.user);
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handler = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const initials = user?.fullName
    ?.split(' ')
    .map((n) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase() || 'U';

  return (
    <header className="h-16 bg-white border-b border-surface-200 flex items-center justify-between px-4 lg:px-6 shrink-0 z-30">
      {/* Left */}
      <div className="flex items-center gap-3">
        <button
          onClick={toggleMobile}
          className="p-2 rounded-lg text-surface-500 hover:bg-surface-100 lg:hidden transition-colors cursor-pointer"
        >
          <Menu className="w-5 h-5" />
        </button>
        <div className="hidden sm:block">
          <h2 className="text-sm font-semibold text-surface-800">
            Welcome back, <span className="text-primary-600">{user?.fullName?.split(' ')[0] || 'User'}</span>
          </h2>
        </div>
      </div>

      {/* Right */}
      <div className="flex items-center gap-2">
        {/* Notifications */}
        <button
          onClick={() => navigate(ROUTES.NOTIFICATIONS)}
          className="relative p-2.5 rounded-lg text-surface-500 hover:bg-surface-100 transition-colors cursor-pointer"
        >
          <Bell className="w-5 h-5" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-danger-500 rounded-full" />
        </button>

        {/* Profile dropdown */}
        <div className="relative" ref={dropdownRef}>
          <button
            onClick={() => setDropdownOpen(!dropdownOpen)}
            className="flex items-center gap-2.5 p-1.5 pr-3 rounded-lg hover:bg-surface-100 transition-colors cursor-pointer"
          >
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center text-xs font-bold text-white">
              {initials}
            </div>
            <div className="hidden sm:block text-left">
              <p className="text-sm font-medium text-surface-800 leading-tight">{user?.fullName || 'User'}</p>
              <p className="text-[11px] text-surface-500 leading-tight">{ROLE_DISPLAY_NAMES[user?.roleName] || user?.roleName}</p>
            </div>
            <ChevronDown className={cn('w-4 h-4 text-surface-400 transition-transform hidden sm:block', dropdownOpen && 'rotate-180')} />
          </button>

          {dropdownOpen && (
            <div className="absolute right-0 mt-2 w-52 bg-white border border-surface-200 rounded-xl shadow-lg py-1.5 animate-scale-in z-50">
              <button
                onClick={() => { navigate(ROUTES.PROFILE); setDropdownOpen(false); }}
                className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-surface-700 hover:bg-surface-50 transition-colors cursor-pointer"
              >
                <User className="w-4 h-4" /> My Profile
              </button>
              <button
                onClick={() => { navigate(ROUTES.SESSIONS); setDropdownOpen(false); }}
                className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-surface-700 hover:bg-surface-50 transition-colors cursor-pointer"
              >
                <Bell className="w-4 h-4" /> Sessions
              </button>
              <div className="border-t border-surface-100 my-1" />
              <button
                onClick={() => { logout(); setDropdownOpen(false); }}
                className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-danger-600 hover:bg-danger-50 transition-colors cursor-pointer"
              >
                <LogOut className="w-4 h-4" /> Sign Out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
