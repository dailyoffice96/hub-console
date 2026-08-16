import { useState, useEffect } from 'react';
import { getInquiry, getInquiryStats } from "../api/inquiryApi";
import InquiryDetailModal from '../components/InquiryDetailModal';
import { INQUIRY_STATUS_LABELS, INQUIRY_STATUS_COLORS, INQUIRY_STATUS_TYPELABELS } from '../constants/statusColors';
import RowTable from '../components/common/RowTable';
import NumberBadge from '../components/common/NumberBadge';
import StatusBadge from '../components/common/StatusBadge';
import CompactStatGroup from '../components/stats/CompactStatGroup';
import AlertList from '../components/common/AlertList';
import { TABLE_TEXT_COLOR } from '../constants/designTokens';

// 문의 목록 행의 컬럼 비율 (헤더와 데이터 행이 동일하게 사용)
const INQUIRY_ROW_GRID = '0.5fr 1.2fr 2fr 1fr 1fr 1fr 1fr';

// 페이지당 행 개수 — 번호 배지 계산(page * PAGE_SIZE + index + 1)에도 그대로 씀
const PAGE_SIZE = 10;

// 원래 색상(warning=대기, primary=처리중, success=완료)에 맞춘 파스텔 카드 배경 (레퍼런스 이미지 톤)
const STAT_PASTELS = {
  waiting: { bg: '#FEF6D8', text: '#7A5B00' },
  inProgress: { bg: '#ECE4FF', text: '#4C2A8C' },
  done: { bg: '#E3F5DE', text: '#1F5C2E' },
};

// 리스트가 눌릴 때 페이지 전체가 아니라 표 안쪽에서만 가로 스크롤되게 하는 최소 너비
const ROW_MIN_WIDTH = '780px';

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
    getInquiry({ assigneeName, type, status, page, size: PAGE_SIZE })
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

  // 담당자 미배정 건만 뽑아 공통 AlertList가 받는 형태({icon, badge, meta, ...})로 변환
  const unassigned = inquiries.filter((i) => !i.assigneeName).slice(0, 5);
  const unassignedAlertItems = unassigned.map((inquiry) => ({
    id: inquiry.id,
    icon: '🔔',
    title: inquiry.title,
    badge: { label: INQUIRY_STATUS_LABELS[inquiry.status], bg: '#FEF3C7', text: '#92400E' },
    meta: inquiry.createdAt,
    onClick: () => setSelectedInquiry(inquiry),
  }));
  const unassignedCount = inquiries.filter((i) => !i.assigneeName).length;

  return (
    // d-flex flex-column + height:100% — Layout이 준 세로 공간을 그대로 받아서 아래 row가 채우게 한다.
    <div className="container-fluid px-4 d-flex flex-column" style={{ height: '100%', minHeight: 0 }}>
      <div className="row g-4 flex-grow-1" style={{ minHeight: 0 }}>
        {/* 왼쪽 — 문의 목록 (넓게)
            minWidth: 0 — row가 flex라서 안쪽 표의 최소 너비가 이 컬럼 자체를 넓혀버리는 것 방지
            d-flex flex-column + height:100% — 표(RowTable)가 남는 세로 공간을 채우게 한다 */}
        <div className="col-lg-8 d-flex flex-column" style={{ minWidth: 0, height: '100%', minHeight: 0 }}>
          <div className="d-flex justify-content-between align-items-end mb-3">
            <h2 className="fw-bold mb-0" style={{ fontSize: '18px' }}>문의 목록</h2>
          </div>

          {/* 공통 RowTable 적용 (검색 영역 + 헤더 + 데이터 행 + 페이징을 카드 하나로 통합)
              flex-grow-1 + minHeight:0 — 위쪽 섹션을 뺀 나머지 세로 공간을 표가 전부 차지하고,
              그 안에서만(데이터 행 부분) 세로 스크롤이 생기게 한다. */}
          <div className="flex-grow-1 d-flex flex-column" style={{ minHeight: 0 }}>
          <RowTable
            onSearch={handleSearch}
            filters={
              <>
                <input
                  className="form-control rounded-pill border-2"
                  style={{ maxWidth: '200px', height: '42px' }}
                  placeholder="담당자 이름 검색"
                  value={assigneeName}
                  onChange={(e) => setAssigneeName(e.target.value)}
                />
                <select
                  className="form-select rounded-pill border-2"
                  style={{ width: '150px', height: '42px' }}
                  value={status}
                  onChange={(e) => setStatus(e.target.value)}
                >
                  <option value="">전체 상태</option>
                  <option value="WAITING">대기</option>
                  <option value="IN_PROGRESS">처리중</option>
                  <option value="DONE">완료</option>
                </select>
                <select
                  className="form-select rounded-pill border-2"
                  style={{ width: '150px', height: '42px' }}
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
              </>
            }
            headers={['번호', '회원이름', '제목', '유형', '상태', '담당자', '접수일']}
            gridTemplateColumns={INQUIRY_ROW_GRID}
            minWidth={ROW_MIN_WIDTH}
            page={page}
            totalPages={totalPages}
            onPageChange={setPage}
          >
            {inquiries.length > 0 ? (
              inquiries.map((inquiry, index) => (
                <div
                  key={inquiry.id}
                  className="d-grid align-items-center bg-white shadow-sm px-3 py-3"
                  style={{ gridTemplateColumns: INQUIRY_ROW_GRID, columnGap: '10px', borderRadius: '18px', cursor: 'pointer' }}
                  onClick={() => setSelectedInquiry(inquiry)}
                >
                  <span>
                    <NumberBadge number={page * PAGE_SIZE + index + 1} />
                  </span>
                  <span className="fw-semibold text-truncate">{inquiry.userName}</span>
                  <span className="text-truncate">{inquiry.title}</span>
                  <span>
                    {/* Bootstrap .badge 기본 글자색이 흰색이라 bg-light 위에서 안 보였음 — 명시적으로 색 지정 */}
                    <span className="badge rounded-pill bg-light border px-3 py-2 fw-semibold" style={{ color: TABLE_TEXT_COLOR }}>
                      {INQUIRY_STATUS_TYPELABELS[inquiry.type]}
                    </span>
                  </span>
                  <span>
                    <StatusBadge color={INQUIRY_STATUS_COLORS[inquiry.status]}>
                      {INQUIRY_STATUS_LABELS[inquiry.status]}
                    </StatusBadge>
                  </span>
                  <span className="text-truncate">{inquiry.assigneeName || '-'}</span>
                  <span className="small">{inquiry.createdAt}</span>
                </div>
              ))
            ) : (
              <div className="text-center py-5 text-muted bg-white" style={{ borderRadius: '18px' }}>
                등록된 문의 내역이 없습니다.
              </div>
            )}
          </RowTable>
          </div>
        </div>

        {/* 오른쪽 — 현황 + 알림 (좁게)
            내용이 화면보다 길어지면 이 컬럼 자체가 자기 안에서만 세로 스크롤되게 함 */}
        <div className="col-lg-4" style={{ height: '100%', overflowY: 'auto' }}>
          <div className="d-flex justify-content-between align-items-end mb-3">
            <h2 className="fw-bold mb-0" style={{ fontSize: '18px' }}>현황</h2>
            <span className="text-muted small">총 {stats.waiting + stats.inProgress + stats.done}건</span>
          </div>

          <CompactStatGroup
            tiles={[
              { pastel: STAT_PASTELS.waiting, tagLabel: 'WAIT', value: stats.waiting, title: '대기 중', note: '접수 후 미처리' },
              { pastel: STAT_PASTELS.inProgress, tagLabel: 'PROG', value: stats.inProgress, title: '처리 중', note: '담당자 대응 중' },
              { pastel: STAT_PASTELS.done, tagLabel: 'DONE', value: stats.done, title: '처리 완료', note: '답변 완료' },
            ]}
          />

          {unassignedCount > 0 && (
            <div className="d-flex justify-content-between align-items-end mb-3">
              <h2 className="fw-bold mb-0" style={{ fontSize: '18px' }}>담당자 미배정 알림</h2>
              <span className="text-muted small">{unassignedCount}건</span>
            </div>
          )}
          <AlertList items={unassignedAlertItems} />
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
