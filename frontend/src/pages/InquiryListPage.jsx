import { useState, useEffect } from 'react';
import StatCard from '../components/StatCard';
import { getInquiry, getInquiryStats } from "../api/inquiryApi";
import InquiryDetailModal from '../components/InquiryDetailModal';

const statusLabels = { WAITING: '대기', IN_PROGRESS: '처리중', DONE: '완료' };
const typeLabels = { ACCOUNT: '계정문의', PAYMENT: '결제문의', TECHNICAL: '기술문의', SERVICE: '서비스문의', ETC: '기타' };

function InquiryListPage() {
  const [inquiries, setInquiries] = useState([]);
  const [assigneeName, setAssigneeName] = useState("");
  const [status, setStatus] = useState("");
  const [type, setType] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedInquiry, setSelectedInquiry] = useState(null);
  const [stats, setStats] = useState({ waiting: 0, inProgress: 0, done: 0 });

  const fetchInquiry = () => {
    getInquiry({ assigneeName, type, status, page, size: 10 })
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
    fetchInquiry();
  };

  return (
    <div>
      <div className="row mb-4">
        <div className="col-4"><StatCard icon="⏳" count={stats.waiting} label="대기 중" /></div>
        <div className="col-4"><StatCard icon="🔄" count={stats.inProgress} label="처리 중" /></div>
        <div className="col-4"><StatCard icon="✅" count={stats.done} label="처리 완료" /></div>
      </div>

      <div className="d-flex mb-3">
        <input
          className="form-control me-2"
          placeholder="담당자 이름 검색"
          value={assigneeName}
          onChange={(e) => setAssigneeName(e.target.value)} />
        <select className="form-select me-2" style={{ width: '150px' }}
          value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">전체 상태</option>
          <option value="WAITING">대기</option>
          <option value="IN_PROGRESS">처리중</option>
          <option value="DONE">완료</option>
        </select>
        <select className="form-select me-2" style={{ width: '150px' }}
          value={type} onChange={(e) => setType(e.target.value)}>
          <option value="">문의유형</option>
          <option value="ACCOUNT">계정문의</option>
          <option value="PAYMENT">결제문의</option>
          <option value="TECHNICAL">기술문의</option>
          <option value="SERVICE">서비스 이용문의</option>
          <option value="ETC">기타</option>
        </select>
        <button className="btn btn-primary" onClick={handleSearch}>검색</button>
      </div>

      <table className="table table-hover">
        <thead>
          <tr>
            <th>번호</th>
            <th>회원이름</th>
            <th>제목</th>
            <th>유형</th>
            <th>상태</th>
            <th>담당자</th>
            <th>접수일</th>
          </tr>
        </thead>
        <tbody>
          {inquiries.map((inquiry, index) => (
            <tr key={inquiry.id} onClick={() => setSelectedInquiry(inquiry)} style={{ cursor: 'pointer' }}>
              <td>{page * 10 + index + 1}</td>
              <td>{inquiry.userName}</td>
              <td>{inquiry.title}</td>
              <td>{typeLabels[inquiry.type]}</td>
              <td>{statusLabels[inquiry.status]}</td>
              <td>{inquiry.assigneeName || '미배정'}</td>
              <td>{inquiry.createdAt}</td>
            </tr>
          ))}
        </tbody>
      </table>

      {selectedInquiry && (
        <InquiryDetailModal
          inquiry={selectedInquiry}
          onClose={() => setSelectedInquiry(null)}
          onUpdated={() => {
            fetchInquiry();
            fetchStats();
          }}
        />
      )}

      <div className="d-flex justify-content-center mt-3">
        <button className="btn btn-outline-secondary me-2" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</button>
        <span className="align-self-center mx-2">{page + 1} / {totalPages} 페이지</span>
        <button className="btn btn-outline-secondary" disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>다음</button>
      </div>
    </div>
  );
}

export default InquiryListPage;