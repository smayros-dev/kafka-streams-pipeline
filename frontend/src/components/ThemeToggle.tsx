import React from 'react';
import { useTheme } from '../context/ThemeContext';

export const ThemeToggle: React.FC = () => {
  const { theme, toggleTheme } = useTheme();

  return (
    <button
      onClick={toggleTheme}
      style={{
        padding: '8px 16px',
        backgroundColor: theme === 'dark' ? '#4a5568' : '#e2e8f0',
        color: theme === 'dark' ? '#ffffff' : '#333333',
        border: 'none',
        borderRadius: '8px',
        cursor: 'pointer',
        fontSize: '14px',
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        transition: 'all 0.2s ease'
      }}
      onMouseOver={(e) => {
        e.currentTarget.style.backgroundColor = theme === 'dark' ? '#5a6578' : '#cbd5e0';
      }}
      onMouseOut={(e) => {
        e.currentTarget.style.backgroundColor = theme === 'dark' ? '#4a5568' : '#e2e8f0';
      }}
    >
      <span style={{ fontSize: '18px' }}>
        {theme === 'dark' ? '☀️' : '🌙'}
      </span>
      {theme === 'dark' ? 'Mode Clair' : 'Mode Sombre'}
    </button>
  );
};
