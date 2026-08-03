import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import UserListPage from '../pages/UserListPage';
import AdminListPage from '../pages/AdminListPage';
import InquiryListPage from '../pages/InquiryListPage';
import IncidentListPage from '../pages/IncidentListPage';
import AuditLogPage from '../pages/AuditLogPage';
import Layout from '../components/Layout';  // 실제 경로 확인 필요

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route element={<Layout />}>
        <Route path="/users" element={<UserListPage />} />
        <Route path="/admins" element={<AdminListPage />} />
        <Route path="/inquiries" element={<InquiryListPage />} />
        <Route path="/incidents" element={<IncidentListPage />} />
        <Route path="/auditLog" element={<AuditLogPage />} />
      </Route>
    </Routes>
  );
}

export default AppRoutes;