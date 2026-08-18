import React, { createContext, useContext, useState, useCallback } from 'react';
import api, { getErrorMessage } from '../api/axios';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });
  const [loading, setLoading] = useState(false);

  const login = useCallback(async (email, password) => {
    setLoading(true);
    try {
      const { data } = await api.post('/auth/login', { email, password });
      const currentUser = {
        id: data.userId,
        name: data.name,
        email: data.email,
        role: data.role,
      };
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(currentUser));
      setUser(currentUser);
      return currentUser;
    } catch (err) {
      throw new Error(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  const register = useCallback(async (payload) => {
    setLoading(true);
    try {
      const { data } = await api.post('/auth/register', payload);
      if (!data.token) {
        // Alumni accounts require admin approval before a token is issued
        return { pendingApproval: true };
      }
      const currentUser = {
        id: data.userId,
        name: data.name,
        email: data.email,
        role: data.role,
      };
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(currentUser));
      setUser(currentUser);
      return { pendingApproval: false, user: currentUser };
    } catch (err) {
      throw new Error(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
