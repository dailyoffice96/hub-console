import { useState, useEffect, useRef } from 'react';
import { getUser, getUserStats, downloadUser, uploadUser } from "../api/usersApi";
import UserDetailModal from '../components/UserDetailModal';

const statusColors = {
  ACTIVE: { bg: '#d1e7dd', text: '#0f5132' },
  DORMANT: { bg: '#fff3cd', text: '#664d03' },
  WITHDRAWN: { bg: '#f8d7da', text: '#842029' }
};

function UserListPage() {
    const [users, setUsers] = useState([]);
    const [name, setName] = useState("");
    const [status, setStatus] = useState("");
    const [page, setPage] = useState(0);
    const [loginId, setLoginId] = useState("");
    const [states, setStates] = useState({ active: 0, dormant: 0, withdrawn: 0 });
    const [totalPages, setTotalPages] = useState(0);
    const [selectedUser, setSelectedUser] = useState(null);
    const fileInputRef = useRef(null);

    const fetchStats = () => {
        getUserStats().then(res => setStates(res.data));
    };

    useEffect(() => {
        getUser({ name, status, page, size: 10 })
            .then(res => {
                setUsers(res.data.content || []);
                setTotalPages(res.data.totalPages);
            })
            .catch(err => {
                console.error(err);
                setUsers([]);
            });
    }, [page, status]);

    useEffect(() => {
        fetchStats();
    }, []);

    const handleSearch = async () => {
        setPage(0);
        const res = await getUser({ name, loginId, status, page: 0, size: 10 });
        setUsers(res.data.content);
        setTotalPages(res.data.totalPages);
    };

    const handleDownload = () => {
        downloadUser().then(res => {
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', '회원목록.xlsx');
            document.body.appendChild(link);
            link.click();
            link.remove();
        });
    };

    const handleUpload = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        uploadUser(file).then(() => {
            alert("업로드가 완료되었습니다.");
            getUser({ name, status, page, size: 10 })
                .then(res => {
                    setUsers(res.data.content || []);
                    setTotalPages(res.data.totalPages);
                });
            fetchStats();
        }).catch(err => {
            alert(err.response?.data?.message || "업로드 중 오류가 발생하였습니다.");
        });
    };

    return (
        <div className="container-fluid px-4 py-3">
            {/* 상단 통계 카드 (실무형 플랫 화이트 디자인) */}
            <div className="row g-4 mb-4">
                <div className="col-md-4">
                    <div className="card border shadow-sm p-4 rounded-4 bg-white">
                        <div className="d-flex justify-content-between align-items-center">
                            <div>
                                <span className="text-muted small fw-semibold d-block mb-1">활동 회원</span>
                                <h3 className="fw-bold mb-0 text-success">{states.active} <span className="fs-6 fw-normal text-muted">명</span></h3>
                            </div>
                            <div className="bg-success bg-opacity-10 text-success p-3 rounded-3 fw-bold">ACTIVE</div>
                        </div>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="card border shadow-sm p-4 rounded-4 bg-white">
                        <div className="d-flex justify-content-between align-items-center">
                            <div>
                                <span className="text-muted small fw-semibold d-block mb-1">휴면 회원</span>
                                <h3 className="fw-bold mb-0 text-warning">{states.dormant} <span className="fs-6 fw-normal text-muted">명</span></h3>
                            </div>
                            <div className="bg-warning bg-opacity-10 text-warning p-3 rounded-3 fw-bold">DORMANT</div>
                        </div>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="card border shadow-sm p-4 rounded-4 bg-white">
                        <div className="d-flex justify-content-between align-items-center">
                            <div>
                                <span className="text-muted small fw-semibold d-block mb-1">탈퇴 회원</span>
                                <h3 className="fw-bold mb-0 text-danger">{states.withdrawn} <span className="fs-6 fw-normal text-muted">명</span></h3>
                            </div>
                            <div className="bg-danger bg-opacity-10 text-danger p-3 rounded-3 fw-bold">LEAVE</div>
                        </div>
                    </div>
                </div>
            </div>

            {/* 테이블 및 검색 박스 (mx-2를 주어 바깥쪽 여백 확보) */}
            <div className="card border shadow-sm rounded-4 bg-white overflow-hidden mx-2">
                <div className="p-4 border-bottom bg-light bg-opacity-25">
                    <div className="d-flex flex-wrap gap-2 align-items-center">
                        <input
                            className="form-control"
                            style={{ maxWidth: '200px', height: '44px', borderRadius: '8px' }}
                            placeholder="이름 검색"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                        />
                        <input
                            className="form-control"
                            style={{ maxWidth: '200px', height: '44px', borderRadius: '8px' }}
                            placeholder="아이디 검색"
                            value={loginId}
                            onChange={(e) => setLoginId(e.target.value)}
                        />
                        <select
                            className="form-select"
                            style={{ width: '150px', height: '44px', borderRadius: '8px' }}
                            value={status}
                            onChange={(e) => setStatus(e.target.value)}
                        >
                            <option value="">전체 상태</option>
                            <option value="ACTIVE">활성</option>
                            <option value="DORMANT">휴면</option>
                            <option value="WITHDRAWN">탈퇴</option>
                        </select>
                        <button
                            className="btn btn-primary px-4 fw-semibold shadow-sm"
                            style={{ height: '44px', borderRadius: '8px' }}
                            onClick={handleSearch}
                        >
                            검색
                        </button>
                    </div>
                </div>

                <div className="table-responsive mb-0">
                    <table className="table table-hover align-middle mb-0">
                        <thead className="table-light text-secondary small text-uppercase">
                            <tr>
                                <th className="py-3 ps-4">번호</th>
                                <th className="py-3">이름</th>
                                <th className="py-3">아이디</th>
                                <th className="py-3">전화번호</th>
                                <th className="py-3">이메일</th>
                                <th className="py-3">상태</th>
                                <th className="py-3 pe-4">가입일</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.length > 0 ? (
                                users.map((user, index) => (
                                    <tr
                                        key={user.id}
                                        onClick={() => setSelectedUser(user)}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        <td className="ps-4 fw-medium text-muted">{page * 10 + index + 1}</td>
                                        <td className="fw-semibold text-dark">{user.maskedName}</td>
                                        <td className="text-muted">{user.loginId}</td>
                                        <td className="text-muted">{user.maskedPhone}</td>
                                        <td className="text-muted">{user.maskedEmail}</td>
                                        <td>
                                            <span
                                                className="badge px-2 py-1 border"
                                                style={{
                                                    backgroundColor: statusColors[user.status]?.bg,
                                                    color: statusColors[user.status]?.text,
                                                    borderColor: 'transparent'
                                                }}
                                            >
                                                {user.status}
                                            </span>
                                        </td>
                                        <td className="pe-4 text-muted small">{user.createdAt}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="7" className="text-center py-5 text-muted">등록된 회원이 없습니다.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

                <div className="d-flex flex-wrap justify-content-between align-items-center p-4 border-top bg-light bg-opacity-50">
                    <div style={{ width: '150px' }}></div>

                    <div className="d-flex justify-content-center align-items-center mx-auto">
                        <button
                            className="btn btn-white border shadow-sm px-3 me-2 rounded-pill"
                            disabled={page === 0}
                            onClick={() => setPage(page - 1)}
                        >
                            이전
                        </button>
                        <span className="text-secondary small fw-bold mx-3">{page + 1} / {totalPages || 1} 페이지</span>
                        <button
                            className="btn btn-white border shadow-sm px-3 ms-2 rounded-pill"
                            disabled={page >= totalPages - 1 || totalPages === 0}
                            onClick={() => setPage(page + 1)}
                        >
                            다음
                        </button>
                    </div>

                    <div className="d-flex gap-2">
                        <button className="btn btn-outline-success px-3 fw-semibold shadow-sm rounded-3" onClick={handleDownload}>
                            엑셀 다운
                        </button>
                        <input
                            type="file"
                            ref={fileInputRef}
                            accept=".xlsx"
                            style={{ display: 'none' }}
                            onChange={handleUpload}
                        />
                        <button className="btn btn-outline-danger px-3 fw-semibold shadow-sm rounded-3" onClick={() => fileInputRef.current.click()}>
                            엑셀 업로드
                        </button>
                    </div>
                </div>
            </div>

            {selectedUser && (
                <UserDetailModal
                    user={selectedUser}
                    onClose={() => setSelectedUser(null)}
                    onUpdated={() => {
                        getUser({ name, status, page, size: 10 })
                            .then(res => setUsers(res.data.content));
                        fetchStats();
                    }}
                />
            )}
        </div>
    );
}

export default UserListPage;