import { useState, useEffect } from 'react';

export function useAuth() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    // TODO: check session with backend (e.g. call a /me endpoint)
  }, []);

  return { isLoggedIn, setIsLoggedIn };
}
