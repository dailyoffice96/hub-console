import { useState, useEffect } from 'react';
import { getAdmin, getAdminStats, unlockAdmin, deleteAdmin } from '../api/adminApi';
import axiosInstance from '../api/axiosInstance';

// 페이지당 행 개수 — 번호 배지 계산(page * PAGE_SIZE + index + 1)에도 그대로 씀
export const PAGE_SIZE = 10;

// AdminListPage의 데이터 조회 + 상태 관리만 따로 뺀 훅이다.
export function useAdminList() {
    const [admins, setAdmins] = useState([]);
    const [name, setName] = useState("");
    const [role, setRole] = useState("");
    const [myRole, setMyRole] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [stats, setStats] = useState({ totalCount: 0, lockedCount: 0, superAdminCount: 0, adminCount: 0, staffCount: 0 });

    // targetPage를 안 넘기면 현재 page state를 쓴다. handleSearch처럼 setPage(0)과 같이
    // 쓸 때는 state가 아직 안 바뀐 시점이라 0을 직접 넘겨줘야 그 페이지로 조회된다.
    const fetchAdmins = (targetPage = page) => {
        getAdmin({ name, role, page: targetPage, size: PAGE_SIZE })
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
        fetchAdmins(0);
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

    return {
        admins, name, setName, role, setRole, myRole, page, setPage, totalPages, stats,
        fetchAdmins, fetchStats, handleSearch, handleUnlock, handleDelete,
    };
}
