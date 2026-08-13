import { useState, useEffect } from 'react';
import axiosInstance from '../api/axiosInstance';

export function useAuth() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  // /api/me 응답이 아직 안 왔을 때는 "로그인 아님"으로 단정하면 안 되므로 별도로 관리
  const [isChecking, setIsChecking] = useState(true);
  const [me, setMe] = useState(null);

  useEffect(() => {
    let cancelled = false;

    axiosInstance.get('/api/me')
      .then((res) => {
        // 비로그인 상태에서도 백엔드가 302로 /login을 리다이렉트하고,
        // 브라우저가 그걸 그대로 따라가서 200 + HTML을 돌려주는 경우가 있다.
        // 그래서 상태코드만으로 판단하지 않고, 실제로 MyInfoResponse 형태(JSON에 role 필드)인지 확인한다.
        const data = res.data;
        const isMyInfoResponse = data && typeof data === 'object' && typeof data.role === 'string';

        if (cancelled) return;
        if (isMyInfoResponse) {
          setIsLoggedIn(true);
          setMe(data);
        } else {
          setIsLoggedIn(false);
          setMe(null);
        }
      })
      .catch(() => {
        // 401/403 등 인증 실패
        if (!cancelled) {
          setIsLoggedIn(false);
          setMe(null);
        }
      })
      .finally(() => {
        if (!cancelled) setIsChecking(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  return { isLoggedIn, isChecking, me, setIsLoggedIn };
}
