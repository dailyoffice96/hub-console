import { useState } from 'react';
import { changeMyPassword } from '../api/authApi';

// 본인 비밀번호 변경 모달. 다른 사람의 비밀번호를 바꾸는 화면이 아니라,
// 지금 로그인한 사람 본인이 현재 비밀번호를 확인받고 새 비밀번호로 바꾸는 셀프서비스.
function ChangePasswordModal({ onClose }) {
    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const handleSubmit = async () => {
        if (!currentPassword.trim() || !newPassword.trim()) {
            alert("현재 비밀번호와 새 비밀번호를 모두 입력해주세요.");
            return;
        }
        if (newPassword !== confirmPassword) {
            alert("새 비밀번호 확인이 일치하지 않습니다.");
            return;
        }

        try {
            await changeMyPassword(currentPassword, newPassword);
            alert("비밀번호가 변경되었습니다. 다른 기기/탭에 로그인돼 있던 세션은 모두 로그아웃됩니다.");
            onClose();
        } catch (err) {
            alert(err.response?.data?.message || "변경 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
            <div className="modal-dialog modal-dialog-centered">
                <div className="modal-content border shadow-sm p-4 rounded-4 bg-white overflow-hidden">
                    <div className="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
                        <h5 className="fw-bold text-dark mb-0">비밀번호 변경</h5>
                        <button type="button" className="btn-close shadow-none" onClick={onClose}></button>
                    </div>

                    <div className="mb-3">
                        <label className="form-label small fw-semibold text-secondary">현재 비밀번호</label>
                        <input
                            type="password"
                            className="form-control"
                            style={{ height: '44px', borderRadius: '8px' }}
                            value={currentPassword}
                            onChange={(e) => setCurrentPassword(e.target.value)}
                            placeholder="현재 사용 중인 비밀번호"
                        />
                    </div>

                    <div className="mb-3">
                        <label className="form-label small fw-semibold text-secondary">새 비밀번호</label>
                        <input
                            type="password"
                            className="form-control"
                            style={{ height: '44px', borderRadius: '8px' }}
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            placeholder="4자 이상"
                        />
                    </div>

                    <div className="mb-3">
                        <label className="form-label small fw-semibold text-secondary">새 비밀번호 확인</label>
                        <input
                            type="password"
                            className="form-control"
                            style={{ height: '44px', borderRadius: '8px' }}
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            placeholder="한 번 더 입력"
                        />
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
                            변경
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ChangePasswordModal;
