import { useState } from 'react';
import { createAdmin } from '../api/adminApi';

function AdminCreateModal({ onClose, onCreated }) {
    const [loginId, setLoginId] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [role, setRole] = useState("STAFF");

    const handleSubmit = async () => {
        if (!loginId.trim() || !password.trim() || !name.trim()) {
            alert("아이디, 비밀번호, 이름을 모두 입력해주세요.");
            return;
        }

        try {
            await createAdmin({ loginId, password, name, role });
            alert("관리자가 추가되었습니다.");
            if (onCreated) onCreated();
            onClose();
        } catch (err) {
            alert(err.response?.data?.message || "등록 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
            <div className="modal-dialog modal-dialog-centered">
                <div className="modal-content border shadow-sm p-4 rounded-4 bg-white overflow-hidden">
                    <div className="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
                        <h5 className="fw-bold text-dark mb-0">관리자 추가</h5>
                        <button type="button" className="btn-close shadow-none" onClick={onClose}></button>
                    </div>

                    <div className="mb-3">
                        <label className="form-label small fw-semibold text-secondary">아이디</label>
                        <input
                            className="form-control"
                            style={{ height: '44px', borderRadius: '8px' }}
                            value={loginId}
                            onChange={(e) => setLoginId(e.target.value)}
                            placeholder="로그인 아이디"
                        />
                    </div>

                    <div className="mb-3">
                        <label className="form-label small fw-semibold text-secondary">비밀번호</label>
                        <input
                            type="password"
                            className="form-control"
                            style={{ height: '44px', borderRadius: '8px' }}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="4자 이상"
                        />
                    </div>

                    <div className="mb-3">
                        <label className="form-label small fw-semibold text-secondary">이름</label>
                        <input
                            className="form-control"
                            style={{ height: '44px', borderRadius: '8px' }}
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            placeholder="이름"
                        />
                    </div>

                    <div className="mb-3">
                        <label className="form-label small fw-semibold text-secondary">직급</label>
                        <select
                            className="form-select"
                            style={{ height: '44px', borderRadius: '8px' }}
                            value={role}
                            onChange={(e) => setRole(e.target.value)}
                        >
                            <option value="SUPER_ADMIN">대표</option>
                            <option value="ADMIN">팀장</option>
                            <option value="STAFF">직원</option>
                        </select>
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

export default AdminCreateModal;
