import { Routes, Route, Navigate } from 'react-router-dom';
import { ROUTES } from '@/constants/routePaths';

// Layout
import { PageWrapper } from '@/components/layout/PageWrapper';
import { ProtectedRoute } from '@/components/layout/ProtectedRoute';
import { RoleGuard } from '@/components/layout/RoleGuard';
import { ROLES } from '@/constants/roles';

// Auth Pages
import LoginPage from '@/pages/auth/LoginPage';
import VerifyOtpPage from '@/pages/auth/VerifyOtpPage';
import ChangePasswordPage from '@/pages/auth/ChangePasswordPage';

// Dashboard
import DashboardPage from '@/pages/dashboard/DashboardPage';

// Geography
import StatesPage from '@/pages/geography/StatesPage';
import DistrictsPage from '@/pages/geography/DistrictsPage';
import BlocksPage from '@/pages/geography/BlocksPage';
import StoresPage from '@/pages/geography/StoresPage';

// Users
import UserListPage from '@/pages/users/UserListPage';
import UserCreatePage from '@/pages/users/UserCreatePage';
import UserDetailPage from '@/pages/users/UserDetailPage';

// Operations (Phase 3-4 placeholders)
import ProductsPage from '@/pages/products/ProductsPage';
import InventoryPage from '@/pages/inventory/InventoryPage';
import BillingPage from '@/pages/billing/BillingPage';
import PatientsPage from '@/pages/patients/PatientsPage';

// Insights (Phase 5 placeholders)
import CommissionsPage from '@/pages/commissions/CommissionsPage';
import ReportsPage from '@/pages/reports/ReportsPage';
import NotificationsPage from '@/pages/notifications/NotificationsPage';

// Supporting
import SessionsPage from '@/pages/sessions/SessionsPage';
import ProfilePage from '@/pages/profile/ProfilePage';
import SettingsPage from '@/pages/settings/SettingsPage';

// Error Pages
import ForbiddenPage from '@/pages/errors/ForbiddenPage';
import NotFoundPage from '@/pages/errors/NotFoundPage';
import ServerErrorPage from '@/pages/errors/ServerErrorPage';

function App() {
  return (
    <Routes>
      {/* Public auth routes */}
      <Route path={ROUTES.LOGIN} element={<LoginPage />} />
      <Route path={ROUTES.VERIFY_OTP} element={<VerifyOtpPage />} />
      <Route path={ROUTES.CHANGE_PASSWORD} element={<ChangePasswordPage />} />

      {/* Protected routes with layout */}
      <Route
        element={
          <ProtectedRoute>
            <PageWrapper />
          </ProtectedRoute>
        }
      >
        {/* Dashboard */}
        <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />

        {/* Geography */}
        <Route path={ROUTES.STATES} element={<StatesPage />} />
        <Route path={ROUTES.DISTRICTS} element={<DistrictsPage />} />
        <Route path={ROUTES.BLOCKS} element={<BlocksPage />} />
        <Route path={ROUTES.STORES} element={<StoresPage />} />

        {/* Users */}
        <Route path={ROUTES.USERS} element={<UserListPage />} />
        <Route path={ROUTES.USER_CREATE} element={<UserCreatePage />} />
        <Route path={ROUTES.USER_DETAIL} element={<UserDetailPage />} />
        <Route path={ROUTES.USER_EDIT} element={<UserDetailPage />} />

        {/* Patients */}
        <Route path={ROUTES.PATIENTS} element={<PatientsPage />} />

        {/* Products & Inventory */}
        <Route path={ROUTES.PRODUCTS} element={<ProductsPage />} />
        <Route path={ROUTES.INVENTORY} element={<InventoryPage />} />

        {/* Billing */}
        <Route path={ROUTES.BILLING} element={<BillingPage />} />

        {/* Commissions (Super Admin only) */}
        <Route
          path={ROUTES.COMMISSIONS}
          element={
            <RoleGuard roles={[ROLES.SUPER_ADMIN]}>
              <CommissionsPage />
            </RoleGuard>
          }
        />

        {/* Reports */}
        <Route path={ROUTES.REPORTS} element={<ReportsPage />} />

        {/* Notifications */}
        <Route path={ROUTES.NOTIFICATIONS} element={<NotificationsPage />} />

        {/* Sessions */}
        <Route path={ROUTES.SESSIONS} element={<SessionsPage />} />

        {/* Profile */}
        <Route path={ROUTES.PROFILE} element={<ProfilePage />} />

        {/* Settings (Super Admin only) */}
        <Route
          path={ROUTES.SETTINGS}
          element={
            <RoleGuard roles={[ROLES.SUPER_ADMIN]}>
              <SettingsPage />
            </RoleGuard>
          }
        />
      </Route>

      {/* Error pages */}
      <Route path={ROUTES.FORBIDDEN} element={<ForbiddenPage />} />
      <Route path={ROUTES.SERVER_ERROR} element={<ServerErrorPage />} />
      <Route path={ROUTES.NOT_FOUND} element={<NotFoundPage />} />
      <Route path="*" element={<Navigate to={ROUTES.NOT_FOUND} replace />} />
    </Routes>
  );
}

export default App;
