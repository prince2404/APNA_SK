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
import VerificationQueuePage from '@/pages/users/VerificationQueuePage';
import PermissionRequestsPage from '@/pages/users/PermissionRequestsPage';

// Operations (Phase 3-4 placeholders)
import ProductsPage from '@/pages/products/ProductsPage';
import InventoryPage from '@/pages/inventory/InventoryPage';
import BillingPage from '@/pages/billing/BillingPage';
import PatientsPage from '@/pages/patients/PatientsPage';
import BulkUploadPage from '@/pages/patients/BulkUploadPage';
import HospitalsPage from '@/pages/hospitals/HospitalsPage';
import HealthCardsPage from '@/pages/health-cards/HealthCardsPage';
import BillsPage from '@/pages/billing/BillsPage';
import SchemesPage from '@/pages/billing/SchemesPage';

// Insights (Phase 5 placeholders)
import CommissionsPage from '@/pages/commissions/CommissionsPage';
import ReportsPage from '@/pages/reports/ReportsPage';
import NotificationsPage from '@/pages/notifications/NotificationsPage';

// Messaging (Phase 6)
import BulkMessageComposer from '@/pages/messages/BulkMessageComposer';
import MessageTemplatesPage from '@/pages/messages/MessageTemplatesPage';
import MessageHistoryPage from '@/pages/messages/MessageHistoryPage';

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
        <Route
          path={ROUTES.VERIFICATION_QUEUE}
          element={
            <RoleGuard roles={[ROLES.SUPER_ADMIN, ROLES.SYSTEM_ADMIN]}>
              <VerificationQueuePage />
            </RoleGuard>
          }
        />
        <Route path={ROUTES.PERMISSION_REQUESTS} element={<PermissionRequestsPage />} />

        {/* Patients */}
        <Route path={ROUTES.PATIENTS} element={<PatientsPage />} />
        <Route path={ROUTES.PATIENTS_BULK_UPLOAD} element={<BulkUploadPage />} />
        <Route path={ROUTES.HOSPITALS} element={<HospitalsPage />} />
        <Route path={ROUTES.HEALTH_CARDS} element={<HealthCardsPage />} />

        {/* Products & Inventory */}
        <Route path={ROUTES.PRODUCTS} element={<ProductsPage />} />
        <Route path={ROUTES.INVENTORY} element={<InventoryPage />} />

        {/* Billing */}
        <Route path={ROUTES.BILLING} element={<BillingPage />} />
        <Route path={ROUTES.INVOICES} element={<BillsPage />} />
        <Route path={ROUTES.SCHEMES} element={<SchemesPage />} />

        {/* Commissions */}
        <Route path={ROUTES.COMMISSIONS} element={<CommissionsPage />} />

        {/* Reports */}
        <Route path={ROUTES.REPORTS} element={<ReportsPage />} />

        {/* Messaging (Admins Only) */}
        <Route
          path={ROUTES.MESSAGES}
          element={
            <RoleGuard roles={[ROLES.SUPER_ADMIN, ROLES.SYSTEM_ADMIN]}>
              <BulkMessageComposer />
            </RoleGuard>
          }
        />
        <Route
          path={ROUTES.MESSAGE_TEMPLATES}
          element={
            <RoleGuard roles={[ROLES.SUPER_ADMIN, ROLES.SYSTEM_ADMIN]}>
              <MessageTemplatesPage />
            </RoleGuard>
          }
        />
        <Route
          path={ROUTES.MESSAGE_HISTORY}
          element={
            <RoleGuard roles={[ROLES.SUPER_ADMIN, ROLES.SYSTEM_ADMIN]}>
              <MessageHistoryPage />
            </RoleGuard>
          }
        />

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
