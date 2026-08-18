import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function Sidebar({ items, active, onSelect, roleLabel }) {
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="sidebar">
      <div className="brand">🎓 Alumni Portal</div>
      <div className="role-badge">{roleLabel}</div>
      <nav>
        {items.map((item) => (
          <button
            key={item.key}
            className={active === item.key ? 'active' : ''}
            onClick={() => onSelect(item.key)}
          >
            {item.label}
          </button>
        ))}
      </nav>
      <button className="btn btn-outline logout-btn" onClick={handleLogout}>
        Logout ({user?.name})
      </button>
    </aside>
  );
}
