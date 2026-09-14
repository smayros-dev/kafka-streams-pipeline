import React, { Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { ThemeProvider, useTheme } from './context/ThemeContext';
import { ThemeToggle } from './components/ThemeToggle';
import { LoadingSpinner } from './components/LoadingSpinner';

const Dashboard = lazy(() => import('./pages/Dashboard').then(m => ({ default: m.Dashboard })));

const AppContent: React.FC = () => {
  const { theme } = useTheme();

  return (
    <Router>
      <div style={{
        minHeight: '100vh',
        backgroundColor: theme === 'dark' ? '#1a1a1a' : '#f5f5f5',
        color: theme === 'dark' ? '#ffffff' : '#333333',
        transition: 'background-color 0.2s ease, color 0.2s ease'
      }}>
        <nav style={{
          backgroundColor: theme === 'dark' ? '#2d3748' : '#2c3e50',
          padding: '16px 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '24px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '24px' }}>
            <Link to="/" style={{
              color: 'white',
              textDecoration: 'none',
              fontSize: '20px',
              fontWeight: 'bold'
            }}>
              Data Dammages
            </Link>
            <Link to="/" style={{
              color: 'white',
              textDecoration: 'none'
            }}>
              Dashboard
            </Link>
          </div>
          <ThemeToggle />
        </nav>

        <Suspense fallback={<LoadingSpinner message="Chargement..." />}>
          <Routes>
            <Route path="/" element={<Dashboard />} />
          </Routes>
        </Suspense>
      </div>
    </Router>
  );
};

const App: React.FC = () => {
  return (
    <ThemeProvider>
      <AppContent />
    </ThemeProvider>
  );
};

export default App;
