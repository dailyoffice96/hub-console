import { useState, useEffect } from 'react';
import { getIncidentDetail, updateIncidentStatus } from '../api/incidentApi';
import { formatDateTime } from '../utils/format';

const statusLabels = { RECEIVED: '접수', IN_PROGRESS: '처리중', DONE: '해결' };
const severityLabels = { LOW: '낮음', MEDIUM: '중간', HIGH: '높음', CRITICAL: '크리티컬' };

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
        console.log("지금 보내는 버전:", detail.version);
      try{
      if (tempStatus !== detail.status) {
        await updateIncidentStatus(incident.id, tempStatus, detail.version);
        fetchDetail();
        onUpdated();
        alert("저장되었습니다.");
      }
      } catch(error){
        alert(error.response?.data?.message || "저장 중 오류가 발생했습니다.");
        fetchDetail(); //최신 상태로 다시 불러오기
        }
    };

    if (!detail) return null;

    return (
      <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
        <div className="modal-dialog modal-lg modal-dialog-centered">
          <div className="modal-content border-0 shadow p-4"
               style={{ borderRadius: '20px', background: 'linear-gradient(180deg, #FDEAEA 0%, #FFFFFF 100%)' }}>

            <h5 className="mb-3">{detail.title}</h5>

            <p><strong>심각도:</strong> {severityLabels[detail.severity]}</p>
            <p><strong>내용:</strong> {detail.content}</p>
            <p><strong>등록자:</strong> {detail.reporter || '-'}</p>
            <p><strong>발생일시:</strong> {formatDateTime(detail.occurredAt)}</p>
            <p><strong>SLA기한:</strong> {formatDateTime(detail.slaDueAt)}</p>
            {detail.resolvedAt && <p><strong>해결일시:</strong> {formatDateTime(detail.resolvedAt)}</p>}

            <div className="mb-3">
              <label className="form-label">상태</label>
              <select className="form-select" value={tempStatus}
                      onChange={(e) => setTempStatus(e.target.value)}>
                <option value="RECEIVED">접수</option>
                <option value="IN_PROGRESS">처리중</option>
                <option value="DONE">해결</option>
              </select>
            </div>

            <button className="btn btn-danger mb-3" onClick={handleSave}>저장</button>

            <h6 className="mt-4">상태변경 이력</h6>
            <ul className="list-group mb-3">
              {detail.histories?.map(h => (
                <li key={h.id} className="list-group-item">
                  {statusLabels[h.beforeStatus]} → {statusLabels[h.afterStatus]} ({formatDateTime(h.changedAt)})
                </li>
              ))}
            </ul>

            <div className="text-end">
              <button className="btn btn-secondary" onClick={onClose}>닫기</button>
            </div>
          </div>
        </div>
      </div>
    );
  }

export default IncidentDetailModal;
