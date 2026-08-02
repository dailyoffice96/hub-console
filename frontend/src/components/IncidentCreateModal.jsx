import { useState, useEffect } from 'react';
import { createIncident } from '../api/incidentApi';

function IncidentCreateModal({onClose, onUpdated }) {
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

        await createIncident({
          title,
          content,
          severity,
          occurredAt: occurredAt ? occurredAt : null,
          slaDueAt: slaDueAt ? slaDueAt : null,
        });

        alert("저장되었습니다.");
        onCreated();
        onClose();


      };

 return (
       <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg modal-dialog-centered">
            <div className="modal-content border-0 shadow p-4"
                 style={{ borderRadius: '20px', background: 'linear-gradient(180deg, #FDEAEA 0%, #FFFFFF 100%)' }}>

              <h5 className="mb-3">장애 등록</h5>

              <div className="mb-3">
                <label className="form-label">제목</label>
                <input className="form-control" value={title}
                       onChange={(e) => setTitle(e.target.value)} placeholder="제목을 입력하세요" />
              </div>

              <div className="mb-3">
                <label className="form-label">내용</label>
                <textarea className="form-control" rows="4" value={content}
                          onChange={(e) => setContent(e.target.value)} placeholder="내용을 입력하세요" />
              </div>

              <div className="mb-3">
                <label className="form-label">심각도</label>
                <select className="form-select" value={severity}
                        onChange={(e) => setSeverity(e.target.value)}>
                  <option value="CRITICAL">치명적</option>
                  <option value="HIGH">높음</option>
                  <option value="MEDIUM">보통</option>
                  <option value="LOW">낮음</option>
                </select>
              </div>

              <div className="mb-3">
                <label className="form-label">발생일시</label>
                <input type="datetime-local" className="form-control" value={occurredAt}
                       onChange={(e) => setOccurredAt(e.target.value)} />
              </div>

              <div className="mb-3">
                <label className="form-label">SLA 기한</label>
                <input type="datetime-local" className="form-control" value={slaDueAt}
                       onChange={(e) => setSlaDueAt(e.target.value)} />
              </div>

              <div className="d-flex justify-content-end">
                <button className="btn btn-secondary me-2" onClick={onClose}>취소</button>
                <button className="btn btn-danger" onClick={handleSubmit}>등록</button>
              </div>

            </div>
          </div>
        </div>
   );
 }

export default IncidentCreateModal;