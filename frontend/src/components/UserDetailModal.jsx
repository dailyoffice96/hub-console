import { useState } from 'react';
import { updateUser, dormantUser, activateUser } from "../api/usersApi"

function UserDetailModal({ user, onClose, onUpdated }) {
  // 조건 없이 호출 즉시 DORMANT로 바뀌는 수동 처리입니다.
  const handleDormant = async () => {
    try {
      await dormantUser(user.id);
      onUpdated();
      onClose();
    } catch (error) {
      alert(error.response?.data?.message || "처리에 실패했습니다.");
    }
  };

  // 휴면 상태를 다시 정상(ACTIVE)으로 되돌립니다.
  const handleActivate = async () => {
    try {
      await activateUser(user.id);
      onUpdated();
      onClose();
    } catch (error) {
      alert(error.response?.data?.message || "처리에 실패했습니다.");
    }
  };

  const statusColors = {
    ACTIVE: { bg: '#d1e7dd', text: '#0f5132' },
    DORMANT: { bg: '#fff3cd', text: '#664d03' },
    WITHDRAWN: { bg: '#f8d7da', text: '#842029' },
  };

  const statusLabels = {
    ACTIVE: '활성',
    DORMANT: '휴면',
    WITHDRAWN: '탈퇴',
  };

  return (
    <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border shadow-sm p-4 rounded-4 bg-white overflow-hidden">

          <div className="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
            <div className="d-flex align-items-center gap-2">
              <h5 className="fw-bold text-dark mb-0">{user.maskedName}님 상세 정보</h5>
              <span
                className="badge px-2 py-1 border"
                style={{
                  backgroundColor: statusColors[user.status]?.bg || '#f1f1f1',
                  color: statusColors[user.status]?.text || '#333',
                  borderColor: 'transparent'
                }}
              >
                {statusLabels[user.status] || user.status}
              </span>
            </div>
            <button type="button" className="btn-close shadow-none" onClick={onClose}></button>
          </div>

          <div className="card bg-light bg-opacity-25 border p-3 rounded-3 mb-4">
            <div className="row g-3 small">
              <div className="col-12">
                <span className="text-muted d-block mb-1">아이디</span>
                <div className="fw-semibold text-dark">{user.loginId}</div>
              </div>
              <div className="col-12">
                <span className="text-muted d-block mb-1">전화번호</span>
                <div className="fw-semibold text-dark">{user.maskedPhone}</div>
              </div>
              <div className="col-12">
                <span className="text-muted d-block mb-1">이메일</span>
                <div className="fw-semibold text-dark">{user.maskedEmail}</div>
              </div>
              <div className="col-12">
                <span className="text-muted d-block mb-1">가입일</span>
                <div className="fw-semibold text-dark">{user.createdAt}</div>
              </div>
            </div>
          </div>

          <div className="d-flex justify-content-end gap-2 pt-3 border-top">
            {user.status === 'ACTIVE' && (
              <button
                className="btn btn-warning px-4 fw-semibold shadow-sm text-dark"
                style={{ height: '44px', borderRadius: '8px' }}
                onClick={handleDormant}
              >
                즉시 휴면 전환
              </button>
            )}
            {user.status === 'DORMANT' && (
              <button
                className="btn btn-success px-4 fw-semibold shadow-sm"
                style={{ height: '44px', borderRadius: '8px' }}
                onClick={handleActivate}
              >
                휴면 해제
              </button>
            )}
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

export default UserDetailModal;