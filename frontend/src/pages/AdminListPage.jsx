import { useState, useEffect } from 'react';
import { getAdmin, getAdminStats, unlockAdmin, deleteAdmin } from "../api/adminApi";
import axiosInstance from '../api/axiosInstance';
import SystemModal from '../components/SystemModal';
import AdminCreateModal from '../components/AdminCreateModal';

function AdminListPage() {
    const [admins, setAdmins] = useState([]);
    const [name, setName] = useState("");
    const [role, setRole] = useState("");
    const [myRole, setMyRole] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [stats, setStats] = useState({ totalCount: 0, lockedCount: 0, superAdminCount: 0, adminCount: 0, staffCount: 0 });
    const [showSystemModal, setShowSystemModal] = useState(false);
    const [showCreateModal, setShowCreateModal] = useState(false);

    const fetchAdmins = () => {
        getAdmin({ name, role, page, size: 10 })
            .then(res => {
                setAdmins(res.data.content || []);
                setTotalPages(res.data.totalPages);
            })
            .catch(err => {
                console.error(err);
                setAdmins([]);
            });
    };

    const fetchStats = () => {
        getAdminStats().then(res => setStats(res.data));
    };

    useEffect(() => {
        fetchAdmins();
    }, [page, role]);

    useEffect(() => {
        fetchStats();
    }, []);

    useEffect(() => {
        axiosInstance.get('/api/me').then(res => setMyRole(res.data.role));
    }, []);

    const handleSearch = () => {
        setPage(0);
        fetchAdmins();
    };

    const handleUnlock = async (id) => {
        await unlockAdmin(id);
        fetchAdmins();
        fetchStats();
    };

    const handleDelete = async (id) => {
        if (window.confirm("정말 삭제하시겠습니까?")) {
            await deleteAdmin(id);
            fetchAdmins();
            fetchStats();
        }
    };

    return (
        <div className="container-fluid px-4 py-3">
            <div className="row g-4 mb-4">
                <div className="col-md-4">
                    <div className="card border shadow-sm p-4 rounded-4 bg-white">
                        <div className="d-flex justify-content-between align-items-center">
                            <div>
                                <span className="text-muted small fw-semibold d-block mb-1">전체 관리자</span>
                                <h3 className="fw-bold mb-0 text-dark">{stats.totalCount} <span className="fs-6 fw-normal text-muted">명</span></h3>
                            </div>
                            <div className="bg-primary bg-opacity-10 text-primary p-3 rounded-3 fw-bold">ALL</div>
                        </div>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="card border shadow-sm p-4 rounded-4 bg-white">
                        <div className="d-flex justify-content-between align-items-center">
                            <div>
                                <span className="text-muted small fw-semibold d-block mb-1">잠긴 계정</span>
                                <h3 className="fw-bold mb-0 text-danger">{stats.lockedCount} <span className="fs-6 fw-normal text-muted">명</span></h3>
                            </div>
                            <div className="bg-danger bg-opacity-10 text-danger p-3 rounded-3 fw-bold">LOCK</div>
                        </div>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="card border shadow-sm p-4 rounded-4 bg-white">
                        <div className="d-flex justify-content-between align-items-center">
                            <div>
                                <span className="text-muted small fw-semibold d-block mb-1">대표 관리자</span>
                                <h3 className="fw-bold mb-0 text-dark">{stats.superAdminCount} <span className="fs-6 fw-normal text-muted">명</span></h3>
                            </div>
                            <div className="bg-warning bg-opacity-10 text-warning p-3 rounded-3 fw-bold">SUPER</div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="card border shadow-sm rounded-4 bg-white overflow-hidden mx-2">
                <div className="p-4 border-bottom bg-light bg-opacity-25">
                    <div className="d-flex flex-wrap gap-2 align-items-center">
                        <input
                            className="form-control"
                            style={{ maxWidth: '240px', height: '44px', borderRadius: '8px' }}
                            placeholder="이름 검색"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                        />
                        <select
                            className="form-select"
                            style={{ width: '150px', height: '44px', borderRadius: '8px' }}
                            value={role}
                            onChange={(e) => setRole(e.target.value)}
                        >
                            <option value="">전체 직급</option>
                            <option value="SUPER_ADMIN">대표</option>
                            <option value="ADMIN">팀장</option>
                            <option value="STAFF">직원</option>
                        </select>
                        <button
                            className="btn btn-primary px-4 fw-semibold shadow-sm"
                            style={{ height: '44px', borderRadius: '8px' }}
                            onClick={handleSearch}
                        >
                            검색
                        </button>
                        {myRole === 'SUPER_ADMIN' && (
                            <div className="d-flex gap-2 ms-auto">
                                <button
                                    className="btn btn-primary px-4 fw-semibold"
                                    style={{ height: '44px', borderRadius: '8px' }}
                                    onClick={() => setShowCreateModal(true)}
                                >
                                    관리자 추가
                                </button>
                                <button
                                    className="btn btn-outline-danger px-4 fw-semibold"
                                    style={{ height: '44px', borderRadius: '8px' }}
                                    onClick={() => setShowSystemModal(true)}
                                >
                                    시스템 점검 설정
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                <div className="table-responsive data-table-wrap mb-0">
                    <table className="table table-hover align-middle mb-0 data-table">
                        <colgroup>
                            <col style={{ width: '6%' }} />
                            <col style={{ width: '14%' }} />
                            <col style={{ width: '18%' }} />
                            <col style={{ width: '14%' }} />
                            <col style={{ width: '16%' }} />
                            <col style={{ width: '16%' }} />
                            <col style={{ width: '16%' }} />
                        </colgroup>
                        <thead className="table-light text-secondary small text-uppercase">
                            <tr>
                                <th className="py-3 ps-4">번호</th>
                                <th className="py-3">이름</th>
                                <th className="py-3">아이디</th>
                                <th className="py-3">직급</th>
                                <th className="py-3">잠금여부</th>
                                <th className="py-3">입사일</th>
                                <th className="py-3 pe-4 text-end">관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            {admins.length > 0 ? (
                                admins.map((admin, index) => (
                                    <tr key={admin.id}>
                                        <td className="ps-4 fw-medium text-muted">{page * 10 + index + 1}</td>
                                        <td className="fw-semibold text-dark">{admin.name}</td>
                                        <td className="text-muted">{admin.loginId}</td>
                                        <td>
                                            <span className="badge bg-light text-dark border px-2 py-1">
                                                {admin.role}
                                            </span>
                                        </td>
                                        <td>
                                            {admin.isLocked ? (
                                                <span
                                                    className="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 px-2 py-1"
                                                    style={{ cursor: 'pointer' }}
                                                    onClick={() => handleUnlock(admin.id)}
                                                    title="클릭하면 잠금 해제"
                                                >
                                                    🔒 잠김 (해제)
                                                </span>
                                            ) : (
                                                <span className="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 px-2 py-1">
                                                    정상
                                                </span>
                                            )}
                                        </td>
                                        <td className="text-muted small">{admin.createdAt}</td>
                                        <td className="pe-4 text-end">
                                            {myRole === 'SUPER_ADMIN' && (
                                                <button
                                                    className="btn btn-sm btn-outline-danger px-3 rounded-pill"
                                                    onClick={() => handleDelete(admin.id)}
                                                >
                                                    탈퇴
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="7" className="text-center py-5 text-muted">검색된 관리자가 없습니다.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

                <div className="d-flex justify-content-center align-items-center p-4 border-top bg-light bg-opacity-50">
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
            </div>

            {showSystemModal && (
                <SystemModal onClose={() => setShowSystemModal(false)} />
            )}

            {showCreateModal && (
                <AdminCreateModal
                    onClose={() => setShowCreateModal(false)}
                    onCreated={() => { fetchAdmins(); fetchStats(); }}
                />
            )}
        </div>
    );
}

export default AdminListPage;