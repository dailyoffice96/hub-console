import {useState} from 'react';
import {updateUser, dormantUser} from "../api/usersApi"

function UserDetailModal({ user, onClose, onUpdated }) {
  const handleDormant = async () => {
    await dormantUser(user.id);
    onUpdated();
    onClose();
  };

  const statusColor = {
    ACTIVE: 'success',
    DORMANT: 'warning',
    WITHDRAWN: 'secondary',
  };



  return (
    <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 shadow"
             style={{ borderRadius: '20px', background: 'linear-gradient(180deg, #EAF2FB 0%, #FFFFFF 100%)' }}>

          <div className="modal-body p-4">
            <div className="text-center mb-4">
              <h5 className="fw-bold mb-1" style={{ color: '#042C53' }}>{user.maskedName}님</h5>
              <span className={`badge bg-${statusColor[user.status]}`}>{user.status}</span>
            </div>

            <div className="mb-2 p-2 rounded" style={{ backgroundColor: '#DCE8F7' }}>
              <span className="text-muted" style={{ fontSize: '13px' }}>아이디</span>
              <div>{user.loginId}</div>
            </div>
            <div className="mb-2 p-2 rounded" style={{ backgroundColor: '#DCE8F7' }}>
              <span className="text-muted" style={{ fontSize: '13px' }}>전화번호</span>
              <div>{user.maskedPhone}</div>
            </div>
            <div className="mb-2 p-2 rounded" style={{ backgroundColor: '#DCE8F7' }}>
              <span className="text-muted" style={{ fontSize: '13px' }}>이메일</span>
              <div>{user.maskedEmail}</div>
            </div>
            <div className="mb-3 p-2 rounded" style={{ backgroundColor: '#DCE8F7' }}>
              <span className="text-muted" style={{ fontSize: '13px' }}>가입일</span>
              <div>{user.createdAt}</div>
            </div>

            <div className="d-flex gap-2">
              <button className="btn flex-fill text-white"
                      style={{ backgroundColor: '#042C53', borderRadius: '10px' }}
                      onClick={handleDormant}>
                휴면 처리
              </button>
              <button className="btn btn-outline-secondary flex-fill"
                      style={{ borderRadius: '10px' }}
                      onClick={onClose}>
                닫기
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default UserDetailModal;
