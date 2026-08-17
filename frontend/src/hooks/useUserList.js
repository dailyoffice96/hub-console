import { useState, useEffect } from 'react';
import { getUser, getUserDetail, getUserStats, downloadUser, uploadUser } from '../api/usersApi';

// 페이지당 행 개수 — 번호 배지 계산(page * PAGE_SIZE + index + 1)에도 그대로 씀
export const PAGE_SIZE = 10;

// UserListPage의 데이터 조회 + 상태 관리만 따로 뺀 훅이다.
// 페이지 컴포넌트는 이 훅이 돌려주는 값을 그대로 받아서 화면만 그리면 된다.
export function useUserList() {
    const [users, setUsers] = useState([]);
    const [name, setName] = useState("");
    const [status, setStatus] = useState("");
    const [page, setPage] = useState(0);
    const [loginId, setLoginId] = useState("");
    const [states, setStates] = useState({ active: 0, dormant: 0, withdrawn: 0 });
    const [totalPages, setTotalPages] = useState(0);
    const [selectedUser, setSelectedUser] = useState(null);

    // targetPage를 안 넘기면 현재 page state를 쓴다. handleSearch처럼 setPage(0)과 같이
    // 쓸 때는 state가 아직 안 바뀐 시점이라 0을 직접 넘겨줘야 그 페이지로 조회된다.
    const fetchUsers = (targetPage = page) => {
        return getUser({ name, loginId, status, page: targetPage, size: PAGE_SIZE })
            .then(res => {
                setUsers(res.data.content || []);
                setTotalPages(res.data.totalPages);
            })
            .catch(err => {
                console.error(err);
                setUsers([]);
            });
    };

    const fetchStats = () => {
        getUserStats().then(res => setStates(res.data));
    };

    useEffect(() => {
        fetchUsers();
    }, [page, status]);

    useEffect(() => {
        fetchStats();
    }, []);

    const handleSearch = () => {
        setPage(0);
        fetchUsers(0);
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

    const handleUpload = (file) => {
        return uploadUser(file).then(() => {
            alert("업로드가 완료되었습니다.");
            fetchUsers();
            fetchStats();
        }).catch(err => {
            alert(err.response?.data?.message || "업로드 중 오류가 발생하였습니다.");
        });
    };

    const openUserDetail = (id) => {
        getUserDetail(id).then(res => setSelectedUser(res.data));
    };

    const closeUserDetail = () => setSelectedUser(null);

    const handleUserUpdated = () => {
        fetchUsers();
        fetchStats();
    };

    return {
        users, name, setName, status, setStatus, page, setPage, loginId, setLoginId,
        states, totalPages, selectedUser,
        handleSearch, handleDownload, handleUpload,
        openUserDetail, closeUserDetail, handleUserUpdated,
    };
}
