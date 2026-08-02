import { useState, useEffect } from 'react';
import { getInquiryDetail, createComment, updateStatus, assignInquiry } from '../api/inquiryApi';
import { getAdmin } from '../api/adminApi';
import axiosInstance from '../api/axiosInstance';

const formatDateTime = (value) => {
  if (!value) return '';
  const [datePart, timePart] = value.split('T');
  if (!timePart) return datePart;
  const [hh, mm] = timePart.split(':');
  return `${datePart} ${hh}:${mm}`;
};

const statusLabels = { WAITING: '대기', IN_PROGRESS: '처리중', DONE: '완료' };
const typeLabels = { ACCOUNT: '계정문의', PAYMENT: '결제문의', TECHNICAL: '기술문의', SERVICE: '서비스문의', ETC: '기타' };

function InquiryDetailModal({ inquiry, onClose, onUpdated }) {
  const [detail, setDetail] = useState(null);
  const [admins, setAdmins] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [myRole, setMyRole] = useState("");

  // 화면에서만 바뀌는 임시 값들 (저장 전)
  const [tempStatus, setTempStatus] = useState("");
  const [tempAssigneeId, setTempAssigneeId] = useState("");

  const fetchDetail = () => {
    getInquiryDetail(inquiry.id).then(res => {
      setDetail(res.data);
      setTempStatus(res.data.status);
      setTempAssigneeId(res.data.assigneeId || "");
    });
  };

  useEffect(() => {
    fetchDetail();
    getAdmin({ size: 100 }).then(res => setAdmins(res.data.content));
    axiosInstance.get('/api/me').then(res => setMyRole(res.data.role));
  }, []);

  const handleSave = async () => {
    if (tempStatus !== detail.status) {
      await updateStatus(inquiry.id, tempStatus);
    }
    if (String(tempAssigneeId) !== String(detail.assigneeId || "")) {
      await assignInquiry(inquiry.id, tempAssigneeId);
    }
    fetchDetail();
    onUpdated();
    alert("저장되었습니다.");
  };

  const handleAddComment = async () => {
    if (!newComment.trim()) return;
    await createComment(inquiry.id, newComment);
    setNewComment("");
    fetchDetail();
  };

  if (!detail) return null;

  const canAssign = myRole === 'SUPER_ADMIN' || myRole === 'ADMIN';

  return (
    <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-lg modal-dialog-centered">
        <div className="modal-content border-0 shadow p-4"
             style={{ borderRadius: '20px', background: 'linear-gradient(180deg, #EAF2FB 0%, #FFFFFF 100%)' }}>

          <h5 className="mb-3">{detail.userName}님의 문의 - {detail.title}</h5>

          <p><strong>유형:</strong> {typeLabels[detail.type]}</p>
          <p><strong>내용:</strong> {detail.content}</p>
          <p><strong>접수일:</strong> {formatDateTime(detail.createdAt)}</p>


          <div className="mb-3">
            <label className="form-label">상태</label>
            <select className="form-select" value={tempStatus}
                    onChange={(e) => setTempStatus(e.target.value)}>
              <option value="WAITING">대기</option>
              <option value="IN_PROGRESS">처리중</option>
              <option value="DONE">완료</option>
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label">담당자</label>
            {canAssign ? (
              <select className="form-select" value={tempAssigneeId}
                      onChange={(e) => setTempAssigneeId(e.target.value)}>
                <option value="">미배정</option>
                {admins.map(admin => (
                  <option key={admin.id} value={admin.id}>{admin.name}</option>
                ))}
              </select>
            ) : (
              <p className="text-muted">{detail.assigneeName || '미배정'} (배정 권한 없음)</p>
            )}
          </div>

          {canAssign && (
            <button className="btn btn-success mb-3" onClick={handleSave}>저장</button>
          )}

          <h6 className="mt-4">상태변경 이력</h6>
          <ul className="list-group mb-3">
            {detail.histories?.map(h => (
              <li key={h.id} className="list-group-item">
               {statusLabels[h.beforeStatus]} → {statusLabels[h.afterStatus]} ({h.adminName}, {formatDateTime(h.changedAt)})              </li>
            ))}
          </ul>

          <h6>댓글</h6>
          <ul className="list-group mb-3">
            {detail.comments?.map(c => (
              <li key={c.id} className="list-group-item">
               {c.content} - {c.adminName} ({formatDateTime(c.createdAt)})
            </li>
            ))}
          </ul>

          <div className="d-flex mb-3">
            <input className="form-control me-2" value={newComment}
                   onChange={(e) => setNewComment(e.target.value)}
                   placeholder="댓글을 입력하세요" />
            <button className="btn btn-primary" onClick={handleAddComment}>등록</button>
          </div>

          <div className="text-end">
            <button className="btn btn-secondary" onClick={onClose}>닫기</button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default InquiryDetailModal;