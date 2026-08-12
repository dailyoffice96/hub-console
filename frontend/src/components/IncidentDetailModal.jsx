import { useState, useEffect } from 'react';
import { getIncidentDetail, updateIncidentStatus } from '../api/incidentApi';
import { formatDateTime } from '../utils/format';

const statusLabels = { RECEIVED: '접수', IN_PROGRESS: '처리중', DONE: '해결' };
const severityLabels = { LOW: '낮음', MEDIUM: '중간', HIGH: '높음', CRITICAL: '크리티컬' };
const severityColors = {
  LOW: { bg: '#d1e7dd', text: '#0f5132' },
  MEDIUM: { bg: '#fff3cd', text: '#664d03' },
  HIGH: { bg: '#ffe5d0', text: '#b45309' },
  CRITICAL: { bg: '#f8d7da', text: '#842029' }
};

function IncidentDetailModal({ incident, onClose, onUpdated }) {
  const [detail, setDetail] = useState(null);
  const [tempStatus, setTempStatus] = useState("");

  const fetchDetail = () => {
      getIncidentDetail(incident.id).then(res => {
        setDetail(res.data);
        setTempStatus(res.data.status);
      });
  };

  useEffect(() => {
    fetchDetail();
  }, []);

  const handleSave = async () => {
    try {
      if (tempStatus !== detail.status) {
        await updateIncidentStatus(incident.id, tempStatus, detail.version);
        fetchDetail();
        onUpdated();
        alert("저장되었습니다.");
      }
    } catch(error) {
      alert(error.response?.data?.message || "저장 중 오류가 발생했습니다.");
      fetchDetail();
    }
  };

  if (!detail) return null;

  return (
    <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-lg modal-dialog-centered">
        <div className="modal-content border shadow-sm p-4 rounded-4 bg-white overflow-hidden">

          <div className="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
            <div className="d-flex align-items-center gap-2">
              <h5 className="fw-bold text-dark mb-0">{detail.title}</h5>
              <span
                className="badge px-2 py-1 border"
                style={{
                  backgroundColor: severityColors[detail.severity]?.bg,
                  color: severityColors[detail.severity]?.text,
                  borderColor: 'transparent'
                }}
              >
                {severityLabels[detail.severity]}
              </span>
            </div>
            <button type="button" className="btn-close shadow-none" onClick={onClose}></button>
          </div>

          <div className="card bg-light bg-opacity-25 border p-3 rounded-3 mb-4">
            <div className="row g-2 small text-secondary">
              <div className="col-6"><strong>등록자:</strong> {detail.reporter || '-'}</div>
              <div className="col-6"><strong>발생일시:</strong> {formatDateTime(detail.occurredAt)}</div>
              <div className="col-6"><strong>SLA기한:</strong> {formatDateTime(detail.slaDueAt)}</div>
              {detail.resolvedAt && <div className="col-6"><strong>해결일시:</strong> {formatDateTime(detail.resolvedAt)}</div>}
            </div>
            <div className="mt-3 pt-3 border-top text-dark">
              <strong>내용:</strong>
              <p className="mt-1 mb-0 text-muted" style={{ whiteSpace: 'pre-line' }}>{detail.content}</p>
            </div>
          </div>

          <div className="mb-4">
            <label className="form-label small fw-semibold text-secondary">상태 변경</label>
            <div className="d-flex gap-2">
              <select
                className="form-select"
                style={{ height: '44px', borderRadius: '8px' }}
                value={tempStatus}
                onChange={(e) => setTempStatus(e.target.value)}
              >
                <option value="RECEIVED">접수</option>
                <option value="IN_PROGRESS">처리중</option>
                <option value="DONE">해결</option>
              </select>
              <button
                className="btn btn-primary px-4 fw-semibold shadow-sm"
                style={{ height: '44px', borderRadius: '8px', minWidth: '90px' }}
                onClick={handleSave}
              >
                저장
              </button>
            </div>
          </div>

          <h6 className="fw-bold text-dark mb-2 small text-uppercase">상태변경 이력</h6>
          <ul className="list-group list-group-flush border rounded-3 overflow-hidden mb-4 small">
            {detail.histories?.length > 0 ? (
              detail.histories.map(h => (
                <li key={h.id} className="list-group-item d-flex justify-content-between align-items-center py-2 px-3 text-secondary">
                  <span>{statusLabels[h.beforeStatus]} → {statusLabels[h.afterStatus]}</span>
                  <span className="text-muted small">{formatDateTime(h.changedAt)}</span>
                </li>
              ))
            ) : (
              <li className="list-group-item text-center py-3 text-muted">변경 이력이 없습니다.</li>
            )}
          </ul>

          <div className="d-flex justify-content-end pt-3 border-top">
            <button
              className="btn btn-outline-secondary px-4 fw-semibold shadow-sm"
              style={{ height: '44px', borderRadius: '8px' }}
              onClick={onClose}
            >
              닫기
            </button>
          </div>

        </div>
      </div>
    </div>
  );
}

export default IncidentDetailModal;