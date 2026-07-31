import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import UserListPage from '../pages/UserListPage';
// import AdminListPage from '../pages/AdminListPage';
// import InquiryListPage from '../pages/InquiryListPage';
// import IncidentListPage from '../pages/IncidentListPage';
import Layout from '../components/Layout';


function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<Layout />}>
        <Route path="/users" element={<UserListPage />} />
{/*         <Route path="/admins" element={<AdminListPage />} /> */}
{/*         <Route path="/inquiries" element={<InquiryListPage />} /> */}
{/*         <Route path="/incidents" element={<IncidentListPage />} /> */}
{/*         <Route path="/auditlog" element={<AuditLogPage />} /> */}
      </Route>
    </Routes>
  );
}

export default AppRoutes;
