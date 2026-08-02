import { useState, useEffect } from 'react';
import { getAuditLogs } from '../api/auditLogApi';
import { formatDateTime } from '../utils/format';

const actionLabels = { CREATE: '등록', UPDATE: '수정', DELETE: '삭제' };
const targetTypeLabels = { INQUIRY: '문의', INCIDENT: '장애', ADMIN: '관리자' };

function AuditLogPage() {
    const [logs, setLogs] = useState([]);
    const [adminName, setAdminName] = useState("");
    const [action, setAction] = useState("");
    const [targetType, setTargetType] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const fetchLogs = () => {
      getAuditLogs({ adminName, action, targetType, page, size: 15 })
     .then(res => {
            setLogs(res.data.content || []);
            setTotalPages(res.data.totalPages);
          })
          .catch(err => {
            console.error(err);
            setLogs([]);
          });
      };

    useEffect(() => {
        fetchLogs();
      }, [page, action, targetType]);

    const handleSearch = () => {
        setPage(0);
        fetchLogs();
      };

  return (
    <div>
      <div className="d-flex mb-3">
        <input
          className="form-control me-2"
          placeholder="담당자 이름 검색"
          value={adminName}
          onChange={(e) => setAdminName(e.target.value)} />
        <select className="form-select me-2" style={{ width: '150px' }}
          value={action} onChange={(e) => setAction(e.target.value)}>
          <option value="">변경 상태</option>
          <option value="CREATE">등록</option>
          <option value="UPDATE">수정</option>
          <option value="DELETE">삭제</option>
        </select>
         <select className="form-select me-2" style={{ width: '150px' }}
          value={targetType} onChange={(e) => setTargetType(e.target.value)}>
          <option value="">전체 대상</option>
          <option value="INQUIRY">문의</option>
          <option value="INCIDENT">장애</option>
          <option value="ADMIN">관리자</option>
        </select>
        <button className="btn btn-primary" onClick={handleSearch}>검색</button>
      </div>

      <table className="table table-hover">
      <thead>
       <tr>
         <th>번호</th>
         <th>담당자</th>
         <th>변경타입</th>
         <th>변경상태</th>
         <th>내용</th>
         <th>일시</th>
       </tr>
     </thead>
     <tbody>
       {logs.map((log, index) => (
         <tr key={log.id}>
           <td>{page * 10 + index + 1}</td>
           <td>{log.admin || '-'}</td>
           <td>{targetTypeLabels[log.targetType]} #{log.targetId}</td>
           <td>{actionLabels[log.action]}</td>
           <td>{log.detail}</td>
           <td>{formatDateTime(log.createdAt)}</td>
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

export default AuditLogPage;
