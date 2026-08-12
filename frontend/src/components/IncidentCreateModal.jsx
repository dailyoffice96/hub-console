import { useState } from 'react';
import { createIncident } from '../api/incidentApi';

function IncidentCreateModal({ onClose, onCreated }) {
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");
    const [severity, setSeverity] = useState("MEDIUM");
    const [occurredAt, setOccurredAt] = useState("");
    const [slaDueAt, setSlaDueAt] = useState("");

    const handleSubmit = async () => {
      if (!title.trim() || !content.trim()) {
        alert("제목과 내용을 입력해주세요.");
        return;
      }

      try {
        await createIncident({
          title,
          content,
          severity,
          occurredAt: occurredAt ? occurredAt : null,
          slaDueAt: slaDueAt ? slaDueAt : null,
        });

        alert("저장되었습니다.");
        if (onCreated) onCreated();
        onClose();
      } catch (err) {
        alert(err.response?.data?.message || "저장 중 오류가 발생했습니다.");
      }
    };

    return (
      <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
        <div className="modal-dialog modal-lg modal-dialog-centered">
          <div className="modal-content border shadow-sm p-4 rounded-4 bg-white overflow-hidden">
            <div className="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
              <h5 className="fw-bold text-dark mb-0">장애 등록</h5>
              <button type="button" className="btn-close shadow-none" onClick={onClose}></button>
            </div>

            <div className="mb-3">
              <label className="form-label small fw-semibold text-secondary">제목</label>
              <input
                className="form-control"
                style={{ height: '44px', borderRadius: '8px' }}
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="제목을 입력하세요"
              />
            </div>

            <div className="mb-3">
              <label className="form-label small fw-semibold text-secondary">내용</label>
              <textarea
                className="form-control"
                style={{ borderRadius: '8px' }}
                rows="4"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="내용을 입력하세요"
              />
            </div>

            <div className="mb-3">
              <label className="form-label small fw-semibold text-secondary">심각도</label>
              <select
                className="form-select"
                style={{ height: '44px', borderRadius: '8px' }}
                value={severity}
                onChange={(e) => setSeverity(e.target.value)}
              >
                <option value="CRITICAL">치명적</option>
                <option value="HIGH">높음</option>
                <option value="MEDIUM">보통</option>
                <option value="LOW">낮음</option>
              </select>
            </div>

            <div className="row">
              <div className="col-md-6 mb-3">
                <label className="form-label small fw-semibold text-secondary">발생일시</label>
                <input
                  type="datetime-local"
                  className="form-control"
                  style={{ height: '44px', borderRadius: '8px' }}
                  value={occurredAt}
                  onChange={(e) => setOccurredAt(e.target.value)}
                />
              </div>

              <div className="col-md-6 mb-3">
                <label className="form-label small fw-semibold text-secondary">SLA 기한</label>
                <input
                  type="datetime-local"
                  className="form-control"
                  style={{ height: '44px', borderRadius: '8px' }}
                  value={slaDueAt}
                  onChange={(e) => setSlaDueAt(e.target.value)}
                />
              </div>
            </div>

            <div className="d-flex justify-content-end gap-2 pt-3 border-top mt-2">
              <button
                className="btn btn-outline-secondary px-4 fw-semibold shadow-sm"
                style={{ height: '44px', borderRadius: '8px' }}
                onClick={onClose}
              >
                취소
              </button>
              <button
                className="btn btn-primary px-4 fw-semibold shadow-sm"
                style={{ height: '44px', borderRadius: '8px' }}
                onClick={handleSubmit}
              >
                등록
              </button>
            </div>
          </div>
        </div>
      </div>
    );
}

export default IncidentCreateModal;