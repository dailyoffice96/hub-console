import { Routes, Route } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import MemberListPage from '../pages/MemberListPage';
import InquiryListPage from '../pages/InquiryListPage';
import IncidentListPage from '../pages/IncidentListPage';
import PrivateRoute from './PrivateRoute';

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<PrivateRoute />}>
        <Route path="/members" element={<MemberListPage />} />
        <Route path="/inquiries" element={<InquiryListPage />} />
        <Route path="/incidents" element={<IncidentListPage />} />
      </Route>
    </Routes>
  );
}

export default AppRoutes;
