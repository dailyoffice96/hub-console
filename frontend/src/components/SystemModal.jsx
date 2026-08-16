import { useState, useEffect } from 'react';
import { getSystemSetting, saveSystemSetting } from '../api/systemsettingApi';

function SystemModal({ onClose }) {
  const [message, setMessage] = useState("");
  const [startAt, setStartAt] = useState("");
  const [endAt, setEndAt] = useState("");

  useEffect(() => {
    getSystemSetting().then(res => {
      if (res.data) {
        setMessage(res.data.message);
        setStartAt(res.data.startAt);
        setEndAt(res.data.endAt);
      }
    });
  }, []);

  const handleSave = async () => {
    await saveSystemSetting({ message, startAt, endAt });
    alert("점검 설정이 저장되었습니다.");
    onClose();
  };

  return (
    <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 shadow p-4" style={{ borderRadius: '20px' }}>

          <h5 className="mb-3">점검 설정</h5>

          <div className="mb-3">
            <label className="form-label">시작일</label>
            <input type="date" className="form-control" value={startAt}
                   onChange={(e) => setStartAt(e.target.value)} />
          </div>

          <div className="mb-3">
            <label className="form-label">종료일</label>
            <input type="date" className="form-control" value={endAt}
                   onChange={(e) => setEndAt(e.target.value)} />
          </div>

          <div className="mb-3">
            <label className="form-label">점검 안내 메시지</label>
            <textarea className="form-control" rows="3" value={message}
                      onChange={(e) => setMessage(e.target.value)} />
          </div>

          <div className="d-flex justify-content-end">
            <button className="btn btn-secondary me-2" onClick={onClose}>취소</button>
            <button className="btn btn-danger" onClick={handleSave}>저장</button>
          </div>

        </div>
      </div>
    </div>
  );
}

export default SystemModal;