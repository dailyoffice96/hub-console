import { useState, useEffect } from 'react';
import { getInquiry, getInquiryStats } from "../api/inquiryApi";
import InquiryDetailModal from '../components/InquiryDetailModal';

const statusLabels = { WAITING: '대기', IN_PROGRESS: '처리중', DONE: '완료' };
const statusColors = {
  WAITING: { bg: '#fff3cd', text: '#664d03' },
  IN_PROGRESS: { bg: '#cfe2ff', text: '#084298' },
  DONE: { bg: '#d1e7dd', text: '#0f5132' }
};
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
    <div className="container-fluid px-4 py-3">
      {/* 상단 통계 카드 (실무형 플랫 화이트 디자인) */}
      <div className="row g-4 mb-4">
        <div className="col-md-4">
          <div className="card border shadow-sm p-4 rounded-4 bg-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <span className="text-muted small fw-semibold d-block mb-1">대기 중</span>
                <h3 className="fw-bold mb-0 text-warning">{stats.waiting} <span className="fs-6 fw-normal text-muted">건</span></h3>
              </div>
              <div className="bg-warning bg-opacity-10 text-warning p-3 rounded-3 fw-bold">WAIT</div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card border shadow-sm p-4 rounded-4 bg-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <span className="text-muted small fw-semibold d-block mb-1">처리 중</span>
                <h3 className="fw-bold mb-0 text-primary">{stats.inProgress} <span className="fs-6 fw-normal text-muted">건</span></h3>
              </div>
              <div className="bg-primary bg-opacity-10 text-primary p-3 rounded-3 fw-bold">PROG</div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card border shadow-sm p-4 rounded-4 bg-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <span className="text-muted small fw-semibold d-block mb-1">처리 완료</span>
                <h3 className="fw-bold mb-0 text-success">{stats.done} <span className="fs-6 fw-normal text-muted">건</span></h3>
              </div>
              <div className="bg-success bg-opacity-10 text-success p-3 rounded-3 fw-bold">DONE</div>
            </div>
          </div>
        </div>
      </div>

      {/* 검색 및 테이블 통합 박스 (mx-2로 바깥쪽 여백 확보) */}
      <div className="card border shadow-sm rounded-4 bg-white overflow-hidden mx-2">
        <div className="p-4 border-bottom bg-light bg-opacity-25">
          <div className="d-flex flex-wrap gap-2 align-items-center">
            <input
              className="form-control"
              style={{ maxWidth: '200px', height: '44px', borderRadius: '8px' }}
              placeholder="담당자 이름 검색"
              value={assigneeName}
              onChange={(e) => setAssigneeName(e.target.value)}
            />
            <select
              className="form-select"
              style={{ width: '150px', height: '44px', borderRadius: '8px' }}
              value={status}
              onChange={(e) => setStatus(e.target.value)}
            >
              <option value="">전체 상태</option>
              <option value="WAITING">대기</option>
              <option value="IN_PROGRESS">처리중</option>
              <option value="DONE">완료</option>
            </select>
            <select
              className="form-select"
              style={{ width: '150px', height: '44px', borderRadius: '8px' }}
              value={type}
              onChange={(e) => setType(e.target.value)}
            >
              <option value="">문의유형</option>
              <option value="ACCOUNT">계정문의</option>
              <option value="PAYMENT">결제문의</option>
              <option value="TECHNICAL">기술문의</option>
              <option value="SERVICE">서비스 이용문의</option>
              <option value="ETC">기타</option>
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
                <th className="py-3">회원이름</th>
                <th className="py-3">제목</th>
                <th className="py-3">유형</th>
                <th className="py-3">상태</th>
                <th className="py-3">담당자</th>
                <th className="py-3 pe-4">접수일</th>
              </tr>
            </thead>
            <tbody>
              {inquiries.length > 0 ? (
                inquiries.map((inquiry, index) => (
                  <tr key={inquiry.id} onClick={() => setSelectedInquiry(inquiry)} style={{ cursor: 'pointer' }}>
                    <td className="ps-4 fw-medium text-muted">{page * 10 + index + 1}</td>
                    <td className="fw-semibold text-dark">{inquiry.userName}</td>
                    <td className="text-dark">{inquiry.title}</td>
                    <td>
                      <span className="badge bg-light text-dark border px-2 py-1">
                        {typeLabels[inquiry.type]}
                      </span>
                    </td>
                    <td>
                      <span
                        className="badge px-2 py-1 border"
                        style={{
                          backgroundColor: statusColors[inquiry.status]?.bg,
                          color: statusColors[inquiry.status]?.text,
                          borderColor: 'transparent'
                        }}
                      >
                        {statusLabels[inquiry.status]}
                      </span>
                    </td>
                    <td className="text-muted">{inquiry.assigneeName || '-'}</td>
                    <td className="pe-4 text-muted small">{inquiry.createdAt}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="7" className="text-center py-5 text-muted">등록된 문의 내역이 없습니다.</td>
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
    </div>
  );
}

export default InquiryListPage;