import { useState, useEffect } from 'react';
import { getAuditLogs, downloadAuditLog } from '../api/auditLogApi';
import { getDailyStats } from '../api/dailyStatsApi';

// 페이지당 행 개수 — 번호 배지 계산(page * PAGE_SIZE + index + 1)에도 그대로 씀
export const PAGE_SIZE = 8;

// AuditLogPage의 데이터 조회 + 상태 관리만 따로 뺀 훅이다. useAdminList/useIncidentList와 같은 패턴.
// 일별 통계로부터 "전일 대비" 등을 계산하는 부분은 화면 표시 로직이라 페이지 컴포넌트에 그대로 뒀다.
export function useAuditLogList() {
    const [logs, setLogs] = useState([]);
    const [adminName, setAdminName] = useState("");
    const [action, setAction] = useState("");
    const [targetType, setTargetType] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [dailyStats, setDailyStats] = useState([]);

    // targetPage를 안 넘기면 현재 page state를 쓴다. handleSearch처럼 setPage(0)과 같이 쓸 때는
    // state가 아직 안 바뀐 시점이라 0을 직접 넘겨줘야 그 페이지로 조회된다.
    const fetchLogs = (targetPage = page) => {
        getAuditLogs({ adminName, action, targetType, page: targetPage, size: PAGE_SIZE })
            .then(res => {
                setLogs(res.data.content || []);
                setTotalPages(res.data.totalPages);
            })
            .catch(err => {
                console.error(err);
                setLogs([]);
            });
    };

    const fetchDailyStats = () => {
        getDailyStats()
            .then(res => {
                const sorted = [...(res.data || [])].sort((a, b) => a.targetDate.localeCompare(b.targetDate));
                setDailyStats(sorted);
            })
            .catch(err => {
                console.error(err);
                setDailyStats([]);
            });
    };

    useEffect(() => {
        fetchLogs();
    }, [page, action, targetType]);

    useEffect(() => {
        fetchDailyStats();
    }, []);

    const handleSearch = () => {
        setPage(0);
        fetchLogs(0);
    };

    const handleDownload = () => {
        downloadAuditLog().then(res => {
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', '감사로그_목록.xlsx');
            document.body.appendChild(link);
            link.click();
            link.remove();
        });
    };

    return {
        logs, adminName, setAdminName, action, setAction, targetType, setTargetType,
        page, setPage, totalPages, dailyStats,
        fetchLogs, fetchDailyStats, handleSearch, handleDownload,
    };
}
