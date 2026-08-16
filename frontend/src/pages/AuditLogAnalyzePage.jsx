import { useState } from 'react';
import { analyzeSuspicious } from '../api/auditLogApi';

function AuditLogAnalyzePage() {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleAnalyze = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await analyzeSuspicious();
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || '분석 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid px-4 py-3">
      <div className="row mx-2 mb-4">
        <div className="col-12 px-0">
          <div className="card border shadow-sm p-4 rounded-4 bg-white">
            <h5 className="fw-bold text-dark mb-1">AI 이상행위 분석</h5>
            <p className="text-muted small mb-0">최근 1시간 동안의 감사로그를 AI가 분석하여, 비정상적인 활동 패턴이 있는지 확인합니다.</p>
          </div>
        </div>
      </div>

      <div className="card border shadow-sm rounded-4 bg-white overflow-hidden mx-2">
        <div className="p-4 border-bottom bg-light bg-opacity-25">
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <h6 className="fw-bold text-dark mb-1">🤖 AI 분석</h6>
              <span className="text-muted small">버튼을 눌러 최근 1시간 활동을 분석해보세요.</span>
            </div>
            <button
              className="btn btn-primary px-4 fw-semibold shadow-sm"
              style={{ height: '44px', borderRadius: '8px' }}
              onClick={handleAnalyze}
              disabled={loading}
            >
              {loading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status" />
                  분석 중...
                </>
              ) : (
                '이상행위 분석 실행'
              )}
            </button>
          </div>
        </div>

        <div className="p-4">
          {error && (
            <div className="alert alert-danger border-0 shadow-sm rounded-3 mb-0">
              {error}
            </div>
          )}

          {result && !error && (
            <div
              className="p-4 rounded-4"
              style={{
                backgroundColor: result.includes('특이사항 없음') || result.includes('없습니다')
                  ? '#F0FDF4'
                  : '#FEF2F2',
                border: `1px solid ${result.includes('특이사항 없음') || result.includes('없습니다') ? '#BBF7D0' : '#FECACA'}`,
              }}
            >
              <div className="d-flex align-items-start gap-3">
                <span style={{ fontSize: '24px' }}>
                  {result.includes('특이사항 없음') || result.includes('없습니다') ? '✅' : '⚠️'}
                </span>
                <div className="text-dark" style={{ whiteSpace: 'pre-line', lineHeight: '1.6' }}>
                  {result}
                </div>
              </div>
            </div>
          )}

          {!result && !error && !loading && (
            <div className="text-center text-muted py-5">
              <div style={{ fontSize: '48px' }} className="mb-2">🔍</div>
              <p className="fw-medium mb-0">아직 분석을 실행하지 않았습니다.</p>
              <small className="text-muted">상단의 분석 실행 버튼을 눌러 로그를 검사하세요.</small>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default AuditLogAnalyzePage;