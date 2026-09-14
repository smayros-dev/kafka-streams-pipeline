import { Component, ErrorInfo, ReactNode } from 'react';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Uncaught error:', error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div style={{
          padding: '24px',
          margin: '16px',
          backgroundColor: '#fee2e2',
          borderRadius: '8px',
          border: '1px solid #fecaca'
        }}>
          <h2 style={{
            margin: '0 0 8px 0',
            color: '#dc2626',
            fontSize: '18px'
          }}>
            Une erreur est survenue
          </h2>
          <p style={{
            margin: '0 0 16px 0',
            color: '#991b1b',
            fontSize: '14px'
          }}>
            {this.state.error?.message || 'Erreur inconnue'}
          </p>
          <button
            onClick={() => this.setState({ hasError: false, error: null })}
            style={{
              padding: '8px 16px',
              backgroundColor: '#dc2626',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            Réessayer
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
