import { useState, useEffect } from 'react';
import { getInquiry, getInquiryStats } from '../api/inquiryApi';

// 페이지당 행 개수 — 번호 배지 계산(page * PAGE_SIZE + index + 1)에도 그대로 씀
export const PAGE_SIZE = 10;

// InquiryListPage의 데이터 조회 + 상태 관리만 따로 뺀 훅이다. useAdminList/useIncidentList와 같은 패턴.
export function useInquiryList() {
    const [inquiries, setInquiries] = useState([]);
    const [assigneeName, setAssigneeName] = useState("");
    const [status, setStatus] = useState("");
    const [type, setType] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [stats, setStats] = useState({ waiting: 0, inProgress: 0, done: 0 });

    // targetPage를 안 넘기면 현재 page state를 쓴다. handleSearch처럼 setPage(0)과 같이 쓸 때는
    // state가 아직 안 바뀐 시점이라 0을 직접 넘겨줘야 그 페이지로 조회된다.
    const fetchInquiry = (targetPage = page) => {
        getInquiry({ assigneeName, type, status, page: targetPage, size: PAGE_SIZE })
            .then(res => {
                setInquiries(res.data.content || []);
                setTotalPages(res.data.totalPages);
            })
            .catch(err => {
                console.error(err);
                setInquiries([]);
            });
    };

    const fetchStats = () => {
        getInquiryStats().then(res => setStats(res.data));
    };

    useEffect(() => {
        fetchInquiry();
    }, [page, status, type]);

    useEffect(() => {
        fetchStats();
    }, []);

    const handleSearch = () => {
        setPage(0);
        fetchInquiry(0);
    };

    return {
        inquiries, assigneeName, setAssigneeName, status, setStatus, type, setType,
        page, setPage, totalPages, stats,
        fetchInquiry, fetchStats, handleSearch,
    };
}
