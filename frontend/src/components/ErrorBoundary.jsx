import { Component } from 'react';

/**
 * 렌더링 중 터진 예외를 잡아 화면 전체가 백지가 되는 것을 막는다.
 * 이벤트 핸들러와 비동기 코드의 예외는 여기로 오지 않는다.
 */
class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { error: null };
  }

  static getDerivedStateFromError(error) {
    return { error };
  }

  componentDidCatch(error, info) {
    console.error('렌더링 중 예외', error, info?.componentStack);
  }

  handleReset = () => {
    this.setState({ error: null });
  };

  handleReload = () => {
    window.location.href = '/';
  };

  render() {
    if (!this.state.error) {
      return this.props.children;
    }

    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <div className="max-w-md w-full bg-white border border-gray-200 rounded-lg p-8 text-center">
          <h1 className="text-xl font-bold text-gray-800 mb-2">
            화면을 그리지 못했습니다
          </h1>
          <p className="text-gray-600 mb-6">
            잠시 후 다시 시도해 주세요. 계속 같은 화면이 나오면 처음으로 돌아가 주세요.
          </p>

          {import.meta.env.DEV && (
            <pre className="text-left text-xs bg-gray-100 text-red-700 rounded p-3 mb-6 overflow-x-auto">
              {String(this.state.error?.message ?? this.state.error)}
            </pre>
          )}

          <div className="flex gap-2 justify-center">
            <button
              onClick={this.handleReset}
              className="px-4 py-2 border border-gray-300 rounded text-gray-700 hover:bg-gray-50"
            >
              다시 시도
            </button>
            <button
              onClick={this.handleReload}
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
            >
              처음으로
            </button>
          </div>
        </div>
      </div>
    );
  }
}

export default ErrorBoundary;
