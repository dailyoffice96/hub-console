import { useState, useEffect } from 'react';
import StatCard from '../components/StatCard';
import { getAdmin, getAdminStats, unlockAdmin, deleteAdmin } from "../api/adminApi";
import axiosInstance from '../api/axiosInstance';


function AdminListPage() {
    const [admins, setAdmins] = useState([]);
    const [name, setName] = useState("");
    const [role, setRole] = useState("");
    const [myRole, setMyRole] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [stats, setStats] = useState({ totalCount: 0, lockedCount: 0, superAdminCount: 0, adminCount: 0, staffCount: 0 });

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
        <div>
            <div className="row mb-4">
                <div className="col-4"><StatCard icon="✅" count={stats.totalCount} label="전체 관리자" /></div>
                <div className="col-4"><StatCard icon="🔒" count={stats.lockedCount} label="잠긴 계정" /></div>
                <div className="col-4"><StatCard icon="👑" count={stats.superAdminCount} label="대표" /></div>
            </div>

            <div className="d-flex mb-3">
                <input
                    className="form-control me-2"
                    placeholder="이름 검색"
                    value={name}
                    onChange={(e) => setName(e.target.value)} />
                <select className="form-select me-2" style={{ width: '150px' }}
                    value={role} onChange={(e) => setRole(e.target.value)}>
                    <option value="">전체 직급</option>
                    <option value="SUPER_ADMIN">대표</option>
                    <option value="ADMIN">팀장</option>
                    <option value="STAFF">직원</option>
                </select>
                <button className="btn btn-primary" onClick={handleSearch}>검색</button>
            </div>

            <table className="table table-hover">
                <thead>
                    <tr>
                        <th>번호</th><th>이름</th><th>아이디</th><th>직급</th><th>잠금여부</th><th>입사일</th><th>관리</th>
                    </tr>
                </thead>
                <tbody>
                    {admins.map((admin, index) => (
                        <tr key={admin.id}>
                            <td>{page * 10 + index + 1}</td>
                            <td>{admin.name}</td>
                            <td>{admin.loginId}</td>
                            <td>{admin.role}</td>
                            <td>
                                {admin.isLocked ? (
                                    <span
                                        className="badge bg-danger"
                                        style={{ cursor: 'pointer' }}
                                        onClick={() => handleUnlock(admin.id)}
                                        title="클릭하면 잠금 해제">🔒 잠김</span>
                                ) : (
                                    <span className="badge bg-success">정상</span>
                                )}
                            </td>
                            <td>{admin.createdAt}</td>
                            <td>
                                {myRole === 'SUPER_ADMIN' && (
                                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(admin.id)}>
                                        탈퇴
                                    </button>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            <div className="d-flex justify-content-center mt-3">
                <button className="btn btn-outline-secondary me-2" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</button>
                <span className="align-self-center mx-2">{page + 1} / {totalPages} 페이지</span>
                <button className="btn btn-outline-secondary" disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>다음</button>
            </div>
        </div>
    );
}

export default AdminListPage;