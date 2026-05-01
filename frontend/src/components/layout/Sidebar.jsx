import { NavLink, useLocation } from 'react-router-dom';
import {
  LayoutDashboard, Users, MapPin, Building2, Store, ShieldCheck,
  Package, Warehouse, Receipt, PieChart, Bell, MessageSquare,
  Settings, ChevronLeft, ChevronRight, Heart, X,
} from 'lucide-react';
import { cn } from '@/utils/cn';
import { useSidebarStore } from '@/store/useSidebarStore';
import { usePermission } from '@/hooks/usePermission';
import { ROUTES } from '@/constants/routePaths';
import { ROLES } from '@/constants/roles';
import { APP } from '@/constants/appConstants';

const menuSections = [
  {
    title: 'Main',
    items: [
      { label: 'Dashboard', icon: LayoutDashboard, path: ROUTES.DASHBOARD, alwaysShow: true },
    ],
  },
  {
    title: 'Management',
    items: [
      { label: 'Users', icon: Users, path: ROUTES.USERS, permission: 'USERS:VIEW' },
      { label: 'States', icon: MapPin, path: ROUTES.STATES, permission: 'GEOGRAPHY:VIEW' },
      { label: 'Districts', icon: Building2, path: ROUTES.DISTRICTS, permission: 'GEOGRAPHY:VIEW' },
      { label: 'Blocks', icon: MapPin, path: ROUTES.BLOCKS, permission: 'GEOGRAPHY:VIEW' },
      { label: 'Stores', icon: Store, path: ROUTES.STORES, permission: 'GEOGRAPHY:VIEW' },
    ],
  },
  {
    title: 'Operations',
    items: [
      { label: 'Products', icon: Package, path: ROUTES.PRODUCTS, permission: 'PRODUCTS:VIEW' },
      { label: 'Inventory', icon: Warehouse, path: ROUTES.INVENTORY, permission: 'INVENTORY:VIEW' },
      { label: 'Billing', icon: Receipt, path: ROUTES.BILLING, permission: 'BILLING:VIEW' },
    ],
  },
  {
    title: 'Insights',
    items: [
      { label: 'Commissions', icon: PieChart, path: ROUTES.COMMISSIONS, permission: 'COMMISSIONS:VIEW', roles: [ROLES.SUPER_ADMIN] },
      { label: 'Reports', icon: PieChart, path: ROUTES.REPORTS, permission: 'REPORTS:VIEW' },
      { label: 'Notifications', icon: Bell, path: ROUTES.NOTIFICATIONS, alwaysShow: true },
    ],
  },
  {
    title: 'System',
    items: [
      { label: 'Sessions', icon: ShieldCheck, path: ROUTES.SESSIONS, alwaysShow: true },
      { label: 'Settings', icon: Settings, path: ROUTES.SETTINGS, roles: [ROLES.SUPER_ADMIN] },
    ],
  },
];

export function Sidebar() {
  const { isCollapsed, toggle, isMobileOpen, closeMobile } = useSidebarStore();
  const { hasPermission, hasAnyRole, isSuperAdmin } = usePermission();

  const shouldShow = (item) => {
    if (item.alwaysShow) return true;
    if (isSuperAdmin) return true;
    if (item.roles && hasAnyRole(...item.roles)) return true;
    if (item.permission && hasPermission(item.permission)) return true;
    return false;
  };

  const sidebarContent = (
    <>
      {/* Logo */}
      <div className={cn(
        'flex items-center h-16 px-4 border-b border-white/10 shrink-0',
        isCollapsed ? 'justify-center' : 'gap-3'
      )}>
        <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center shadow-lg shrink-0">
          <Heart className="w-5 h-5 text-white" />
        </div>
        {!isCollapsed && (
          <div className="overflow-hidden">
            <h1 className="text-sm font-bold text-white truncate">{APP.SHORT_NAME}</h1>
            <p className="text-[10px] text-surface-400 truncate">Healthcare ERP</p>
          </div>
        )}
        {/* Mobile close */}
        <button onClick={closeMobile} className="ml-auto p-1.5 lg:hidden text-surface-400 hover:text-white cursor-pointer">
          <X className="w-5 h-5" />
        </button>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-4 px-3 space-y-6">
        {menuSections.map((section) => {
          const visibleItems = section.items.filter(shouldShow);
          if (!visibleItems.length) return null;
          return (
            <div key={section.title}>
              {!isCollapsed && (
                <p className="px-3 mb-2 text-[10px] font-semibold uppercase tracking-wider text-surface-500">
                  {section.title}
                </p>
              )}
              <ul className="space-y-0.5">
                {visibleItems.map((item) => (
                  <li key={item.path}>
                    <NavLink
                      to={item.path}
                      onClick={closeMobile}
                      className={({ isActive }) => cn(
                        'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 group',
                        isActive
                          ? 'bg-primary-600 text-white shadow-md shadow-primary-600/20'
                          : 'text-surface-400 hover:bg-white/5 hover:text-white',
                        isCollapsed && 'justify-center px-2'
                      )}
                      title={isCollapsed ? item.label : undefined}
                    >
                      <item.icon className={cn('w-5 h-5 shrink-0', isCollapsed && 'w-5 h-5')} />
                      {!isCollapsed && <span className="truncate">{item.label}</span>}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </div>
          );
        })}
      </nav>

      {/* Collapse toggle (desktop only) */}
      <div className="hidden lg:flex items-center justify-center p-3 border-t border-white/10 shrink-0">
        <button
          onClick={toggle}
          className="p-2 rounded-lg text-surface-400 hover:text-white hover:bg-white/10 transition-colors cursor-pointer"
          title={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {isCollapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
        </button>
      </div>
    </>
  );

  return (
    <>
      {/* Mobile overlay */}
      {isMobileOpen && (
        <div className="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm lg:hidden" onClick={closeMobile} />
      )}
      {/* Mobile sidebar */}
      <aside className={cn(
        'fixed inset-y-0 left-0 z-50 w-64 bg-sidebar-bg flex flex-col transition-transform duration-300 lg:hidden',
        isMobileOpen ? 'translate-x-0' : '-translate-x-full'
      )}>
        {sidebarContent}
      </aside>
      {/* Desktop sidebar */}
      <aside className={cn(
        'hidden lg:flex flex-col bg-sidebar-bg transition-all duration-300 shrink-0',
        isCollapsed ? 'w-[72px]' : 'w-64'
      )}>
        {sidebarContent}
      </aside>
    </>
  );
}
