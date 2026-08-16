import { Routes, Route } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import UserListPage from '../pages/UserListPage';
import AdminListPage from '../pages/AdminListPage';
import InquiryListPage from '../pages/InquiryListPage';
import IncidentListPage from '../pages/IncidentListPage';
import IncidentMonitoringPage from '../pages/IncidentMonitoringPage';
import AuditLogPage from '../pages/AuditLogPage';
import AuditLogAnalyzePage from '../pages/AuditLogAnalyzePage';
import Layout from '../components/Layout/Layout';
import PrivateRoute from './PrivateRoute';

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route element={<PrivateRoute />}>
        <Route element={<Layout />}>
          <Route path="/users" element={<UserListPage />} />
          <Route path="/admins" element={<AdminListPage />} />
          <Route path="/inquiries" element={<InquiryListPage />} />
          <Route path="/incidents" element={<IncidentListPage />} />
          <Route path="/incidents/monitoring" element={<IncidentMonitoringPage />} />
          <Route path="/auditLog" element={<AuditLogPage />} />
          <Route path="/auditLog/analyze" element={<AuditLogAnalyzePage  />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default AppRoutes;