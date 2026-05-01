import { Component } from 'react';
import { AlertTriangle } from 'lucide-react';
import { Button } from './Button';

export class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex flex-col items-center justify-center min-h-[400px] gap-4 text-center px-4">
          <div className="w-16 h-16 rounded-2xl bg-danger-100 flex items-center justify-center">
            <AlertTriangle className="w-8 h-8 text-danger-600" />
          </div>
          <h2 className="text-xl font-bold text-surface-900">Something went wrong</h2>
          <p className="text-sm text-surface-500 max-w-md">
            An unexpected error occurred. Please try refreshing the page.
          </p>
          <Button onClick={() => window.location.reload()}>Refresh Page</Button>
        </div>
      );
    }
    return this.props.children;
  }
}
