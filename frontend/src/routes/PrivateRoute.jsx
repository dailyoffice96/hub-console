import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

function PrivateRoute() {
  const { isLoggedIn } = useAuth();
  return isLoggedIn ? <Outlet /> : <Navigate to="/login" />;
}

export default PrivateRoute;
