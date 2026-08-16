import { useState, useEffect } from 'react';
import { getInquiryDetail, createComment, updateStatus, assignInquiry } from '../api/inquiryApi';
import { getAdmin } from '../api/adminApi';
import axiosInstance from '../api/axiosInstance';
import {INQUIRY_STATUS_LABELS, INQUIRY_STATUS_TYPELABELS} from '../constants/statusColors'

const formatDateTime = (value) => {
  if (!value) return '';
  const [datePart, timePart] = value.split('T');
  if (!timePart) return datePart;
  const [hh, mm] = timePart.split(':');
  return `${datePart} ${hh}:${mm}`;
};

function InquiryDetailModal({ inquiry, onClose, onUpdated }) {
  const [detail, setDetail] = useState(null);
  const [admins, setAdmins] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [myRole, setMyRole] = useState("");

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
    try {
      if (tempStatus !== detail.status) {
        await updateStatus(inquiry.id, tempStatus, detail.version);
      }
      if (String(tempAssigneeId) !== String(detail.assigneeId || "")) {
        await assignInquiry(inquiry.id, tempAssigneeId);
      }
      fetchDetail();
      onUpdated();
      alert("저장되었습니다.");
    } catch (error) {
      alert(error.response?.data?.message || "저장 중 오류가 발생했습니다.");
      fetchDetail();
    }
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
        <div className="modal-content border shadow-sm p-4 rounded-4 bg-white overflow-hidden">

          <div className="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
            <div className="d-flex align-items-center gap-2">
              <h5 className="fw-bold text-dark mb-0">{detail.userName}님의 문의 - {detail.title}</h5>
              <span className="badge bg-light text-dark border px-2 py-1">
                {INQUIRY_STATUS_TYPELABELS[detail.type]}
              </span>
            </div>
            <button type="button" className="btn-close shadow-none" onClick={onClose}></button>
          </div>

          <div className="card bg-light bg-opacity-25 border p-3 rounded-3 mb-4">
            <div className="row g-2 small text-secondary mb-2">
              <div className="col-6"><strong>접수일:</strong> {formatDateTime(detail.createdAt)}</div>
              <div className="col-6"><strong>상태:</strong> {INQUIRY_STATUS_LABELS[detail.status]}</div>
            </div>
            <div className="pt-2 border-top text-dark">
              <strong>내용:</strong>
              <p className="mt-1 mb-0 text-muted" style={{ whiteSpace: 'pre-line' }}>{detail.content}</p>
            </div>
          </div>

          <div className="row g-3 mb-4">
            <div className="col-md-6">
              <label className="form-label small fw-semibold text-secondary">상태 변경</label>
              <select
                className="form-select"
                style={{ height: '44px', borderRadius: '8px' }}
                value={tempStatus}
                onChange={(e) => setTempStatus(e.target.value)}
              >
                <option value="WAITING">대기</option>
                <option value="IN_PROGRESS">처리중</option>
                <option value="DONE">완료</option>
              </select>
            </div>

            <div className="col-md-6">
              <label className="form-label small fw-semibold text-secondary">담당자 배정</label>
              {canAssign ? (
                <select
                  className="form-select"
                  style={{ height: '44px', borderRadius: '8px' }}
                  value={tempAssigneeId}
                  onChange={(e) => setTempAssigneeId(e.target.value)}
                >
                  <option value="">미배정</option>
                  {admins.map(admin => (
                    <option key={admin.id} value={admin.id}>{admin.name}</option>
                  ))}
                </select>
              ) : (
                <div className="form-control bg-light text-muted d-flex align-items-center" style={{ height: '44px', borderRadius: '8px' }}>
                  {detail.assigneeName || '미배정'} (배정 권한 없음)
                </div>
              )}
            </div>
          </div>

          {canAssign && (
            <div className="d-flex justify-content-end mb-4">
              <button
                className="btn btn-primary px-4 fw-semibold shadow-sm"
                style={{ height: '44px', borderRadius: '8px' }}
                onClick={handleSave}
              >
                변경사항 저장
              </button>
            </div>
          )}

          <h6 className="fw-bold text-dark mb-2 small text-uppercase">상태변경 이력</h6>
          <ul className="list-group list-group-flush border rounded-3 overflow-hidden mb-4 small">
            {detail.histories?.length > 0 ? (
              detail.histories.map(h => (
                <li key={h.id} className="list-group-item d-flex justify-content-between align-items-center py-2 px-3 text-secondary">
                  <span>{INQUIRY_STATUS_LABELS[h.beforeStatus]} → {INQUIRY_STATUS_LABELS[h.afterStatus]} ({h.adminName})</span>
                  <span className="text-muted small">{formatDateTime(h.changedAt)}</span>
                </li>
              ))
            ) : (
              <li className="list-group-item text-center py-3 text-muted">변경 이력이 없습니다.</li>
            )}
          </ul>

          <h6 className="fw-bold text-dark mb-2 small text-uppercase">댓글</h6>
          <ul className="list-group list-group-flush border rounded-3 overflow-hidden mb-3 small">
            {detail.comments?.length > 0 ? (
              detail.comments.map(c => (
                <li key={c.id} className="list-group-item py-2 px-3">
                  <div className="d-flex justify-content-between align-items-center mb-1">
                    <span className="fw-semibold text-dark">{c.adminName}</span>
                    <span className="text-muted small">{formatDateTime(c.createdAt)}</span>
                  </div>
                  <div className="text-secondary">{c.content}</div>
                </li>
              ))
            ) : (
              <li className="list-group-item text-center py-3 text-muted">등록된 댓글이 없습니다.</li>
            )}
          </ul>

          <div className="d-flex gap-2 mb-4">
            <input
              className="form-control"
              style={{ height: '44px', borderRadius: '8px' }}
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="댓글을 입력하세요"
            />
            <button
              className="btn btn-primary px-4 fw-semibold shadow-sm"
              style={{ height: '44px', borderRadius: '8px', minWidth: '90px' }}
              onClick={handleAddComment}
            >
              등록
            </button>
          </div>

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

export default InquiryDetailModal;