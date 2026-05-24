import { useState, useRef, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Menu, Bell, LogOut, User, ChevronDown, Key, AlertCircle, CheckCircle2, Lock, Sun, Moon } from 'lucide-react';
import { useSidebarStore } from '@/store/useSidebarStore';
import { useAuthStore } from '@/store/useAuthStore';
import { useAuth } from '@/hooks/useAuth';
import { ROLE_DISPLAY_NAMES } from '@/constants/roles';
import { ROUTES } from '@/constants/routePaths';
import { cn } from '@/utils/cn';
import { notificationApi } from '@/api/notificationApi';
import { toast } from '@/store/useNotificationStore';
import { getErrorMessage } from '@/utils/errorUtils';

export function Navbar() {
  const { toggleMobile } = useSidebarStore();
  const user = useAuthStore((s) => s.user);
  const { logout } = useAuth();
  const navigate = useNavigate();
  
  // Dropdown states
  const [profileDropdownOpen, setProfileDropdownOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  const [darkMode, setDarkMode] = useState(
    localStorage.getItem('theme') === 'dark' ||
    (!localStorage.getItem('theme') && window.matchMedia('(prefers-color-scheme: dark)').matches)
  );

  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }, [darkMode]);
  
  // Refs
  const profileDropdownRef = useRef(null);
  const notificationDropdownRef = useRef(null);

  // Notification lists & counts
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  // Click outside listener
  useEffect(() => {
    const handler = (e) => {
      if (profileDropdownRef.current && !profileDropdownRef.current.contains(e.target)) {
        setProfileDropdownOpen(false);
      }
      if (notificationDropdownRef.current && !notificationDropdownRef.current.contains(e.target)) {
        setNotificationsOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  // Fetch notifications and unread count
  const fetchNotifications = useCallback(async () => {
    if (!user) return;
    try {
      const countRes = await notificationApi.getUnreadCount();
      setUnreadCount(countRes.data.data || 0);

      const listRes = await notificationApi.getNotifications({ page: 0, size: 5 });
      setNotifications(listRes.data.data.content || []);
    } catch (err) {
      console.error('Failed to fetch notifications', err);
    }
  }, [user]);

  // Initial fetch and polling setup
  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, [fetchNotifications]);

  // Notification click handler
  const handleNotificationClick = async (n) => {
    setNotificationsOpen(false);
    const nRead = n.isRead !== undefined ? n.isRead : n.read;
    if (!nRead) {
      try {
        await notificationApi.markAsRead(n.id);
        fetchNotifications();
      } catch (err) {
        console.error('Failed to mark notification as read', err);
      }
    }
    
    // Route navigation based on notification type
    if (n.type === 'KYC_SUBMISSION') {
      navigate(ROUTES.VERIFICATION_QUEUE);
    } else if (n.type === 'KYC_REVIEW') {
      navigate(ROUTES.PROFILE);
    } else if (n.type === 'PERMISSION_REQUEST' || n.type === 'PERMISSION_REVIEW') {
      navigate(ROUTES.PERMISSION_REQUESTS);
    } else {
      navigate(ROUTES.NOTIFICATIONS);
    }
  };

  // Mark all as read
  const handleMarkAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      toast.success('All notifications marked as read');
      fetchNotifications();
    } catch (err) {
      toast.error('Failed to mark all as read: ' + getErrorMessage(err));
    }
  };

  // Time-ago helper
  const formatTimeAgo = (dateString) => {
    if (!dateString) return '';
    const now = new Date();
    const past = new Date(dateString);
    const diffMs = now - past;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    return `${diffDays}d ago`;
  };

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
        {/* Dark Mode Toggle */}
        <button
          onClick={() => setDarkMode(!darkMode)}
          className="p-2.5 rounded-lg text-surface-500 hover:bg-surface-100 transition-colors cursor-pointer"
          title={darkMode ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
        >
          {darkMode ? <Sun className="w-5 h-5 text-amber-500" /> : <Moon className="w-5 h-5" />}
        </button>

        {/* Notifications */}
        <div className="relative" ref={notificationDropdownRef}>
          <button
            onClick={() => {
              setNotificationsOpen(!notificationsOpen);
              fetchNotifications();
            }}
            className="relative p-2.5 rounded-lg text-surface-500 hover:bg-surface-100 transition-colors cursor-pointer"
          >
            <Bell className="w-5 h-5" />
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 min-w-[16px] h-4 bg-danger-500 text-[10px] font-bold text-white rounded-full flex items-center justify-center px-1">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </button>

          {notificationsOpen && (
            <div className="absolute right-0 mt-2 w-80 bg-white/95 backdrop-blur-md border border-surface-200 rounded-xl shadow-lg py-2 animate-scale-in z-50">
              <div className="flex items-center justify-between px-4 py-2 border-b border-surface-100">
                <span className="font-semibold text-surface-900 text-sm">Recent Notifications</span>
                {unreadCount > 0 && (
                  <button
                    onClick={handleMarkAllAsRead}
                    className="text-xs text-primary-600 hover:text-primary-800 font-semibold transition-colors cursor-pointer"
                  >
                    Mark all as read
                  </button>
                )}
              </div>

              <div className="max-h-64 overflow-y-auto divide-y divide-surface-100">
                {notifications.length === 0 ? (
                  <div className="px-4 py-6 text-center text-xs text-surface-400">
                    No notifications
                  </div>
                ) : (
                  notifications.map((n) => {
                    const nRead = n.isRead !== undefined ? n.isRead : n.read;
                    return (
                      <div
                        key={n.id}
                        onClick={() => handleNotificationClick(n)}
                        className={cn(
                          'px-4 py-3 flex gap-3 cursor-pointer transition-colors text-left',
                          nRead ? 'hover:bg-surface-50' : 'bg-primary-50/20 hover:bg-primary-50/40'
                        )}
                      >
                        <div className="shrink-0 mt-0.5">
                          {n.type?.includes('KYC') ? (
                            <div className="w-7 h-7 rounded-lg bg-emerald-50 flex items-center justify-center text-emerald-600">
                              <User className="w-4 h-4" />
                            </div>
                          ) : n.type?.includes('PERMISSION') ? (
                            <div className="w-7 h-7 rounded-lg bg-amber-50 flex items-center justify-center text-amber-600">
                              <Key className="w-4 h-4" />
                            </div>
                          ) : (
                            <div className="w-7 h-7 rounded-lg bg-primary-50 flex items-center justify-center text-primary-600">
                              <Bell className="w-4 h-4" />
                            </div>
                          )}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between gap-2">
                            <p className="text-xs font-semibold text-surface-900 truncate">{n.title}</p>
                            <span className="text-[9px] text-surface-400 whitespace-nowrap shrink-0">
                              {formatTimeAgo(n.createdAt)}
                            </span>
                          </div>
                          <p className="text-[11px] text-surface-500 line-clamp-2 mt-0.5 leading-relaxed">
                            {n.message}
                          </p>
                        </div>
                        {!nRead && (
                          <div className="shrink-0 flex items-center">
                            <span className="w-1.5 h-1.5 bg-primary-600 rounded-full" />
                          </div>
                        )}
                      </div>
                    );
                  })
                )}
              </div>

              <div className="border-t border-surface-100 pt-2 px-4">
                <button
                  onClick={() => {
                    setNotificationsOpen(false);
                    navigate(ROUTES.NOTIFICATIONS);
                  }}
                  className="w-full text-center text-xs text-primary-600 hover:text-primary-850 font-semibold py-1.5 transition-colors cursor-pointer"
                >
                  View all notifications
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Profile dropdown */}
        <div className="relative" ref={profileDropdownRef}>
          <button
            onClick={() => setProfileDropdownOpen(!profileDropdownOpen)}
            className="flex items-center gap-2.5 p-1.5 pr-3 rounded-lg hover:bg-surface-100 transition-colors cursor-pointer"
          >
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center text-xs font-bold text-white">
              {initials}
            </div>
            <div className="hidden sm:block text-left">
              <p className="text-sm font-medium text-surface-800 leading-tight">
                {user?.fullName || 'User'}
              </p>
              <p className="text-[11px] text-surface-500 leading-tight">
                {ROLE_DISPLAY_NAMES[user?.roleName] || user?.roleName}
              </p>
            </div>
            <ChevronDown
              className={cn(
                'w-4 h-4 text-surface-400 transition-transform hidden sm:block',
                profileDropdownOpen && 'rotate-180'
              )}
            />
          </button>

          {profileDropdownOpen && (
            <div className="absolute right-0 mt-2 w-52 bg-white border border-surface-200 rounded-xl shadow-lg py-1.5 animate-scale-in z-50">
              <button
                onClick={() => {
                  navigate(ROUTES.PROFILE);
                  setProfileDropdownOpen(false);
                }}
                className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-surface-700 hover:bg-surface-50 transition-colors cursor-pointer"
              >
                <User className="w-4 h-4" /> My Profile
              </button>
              <button
                onClick={() => {
                  navigate(ROUTES.SESSIONS);
                  setProfileDropdownOpen(false);
                }}
                className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-surface-700 hover:bg-surface-50 transition-colors cursor-pointer"
              >
                <Lock className="w-4 h-4" /> Sessions
              </button>
              <div className="border-t border-surface-100 my-1" />
              <button
                onClick={() => {
                  logout();
                  setProfileDropdownOpen(false);
                }}
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
