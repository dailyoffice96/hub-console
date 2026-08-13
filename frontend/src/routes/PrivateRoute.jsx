import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

function PrivateRoute() {
  const { isLoggedIn, isChecking } = useAuth();

  // /api/me 응답을 기다리는 동안 섣불리 로그인 페이지로 튕기지 않는다.
  if (isChecking) {
    return <div className="text-center mt-5 text-secondary">로딩 중...</div>;
  }

  // 로그인 페이지는 "/" 라우트이므로 그쪽으로 리다이렉트한다.
  return isLoggedIn ? <Outlet /> : <Navigate to="/" replace />;
}

export default PrivateRoute;
