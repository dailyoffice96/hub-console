import { useState, useEffect } from 'react';
import { getIncidents, getIncidentStats } from '../api/incidentApi';
import IncidentCreateModal from '../components/IncidentCreateModal';
import IncidentDetailModal from '../components/IncidentDetailModal';
import { formatDateTime } from '../utils/format';
import { INCIDENT_SEVERITY_WEIGHTS, INCIDENT_STATUS_LABELS } from '../constants/statusColors';
import RowTable from '../components/common/RowTable';
import NumberBadge from '../components/common/NumberBadge';
import StatusBadge from '../components/common/StatusBadge';
import CompactStatGroup from '../components/stats/CompactStatGroup';
import AlertList from '../components/common/AlertList';

const severityColor = { LOW: '#94D2BD', MEDIUM: '#F9DFA0', HIGH: '#F4A261', CRITICAL: '#E63946' };

// 장애 목록 행의 컬럼 비율 (헤더와 데이터 행이 동일하게 사용)
const INCIDENT_ROW_GRID = '0.5fr 2fr 1fr 1fr 1fr 1.2fr 1.2fr';

// 페이지당 행 개수 — 번호 배지 계산(page * PAGE_SIZE + index + 1)에도 그대로 씀
const PAGE_SIZE = 10;

// "등록" 버튼 — 장애 신고라는 긴급성을 살려 레드 계열 유지
const REGISTER_BTN_COLOR = '#DC2626';

// 원래 색상(primary=접수, warning=처리중, success=해결)에 맞춘 파스텔 카드 배경 — 문의 목록과 동일한 톤 체계
const STAT_PASTELS = {
  received: { bg: '#E1EBFE', text: '#1D3E8C' },
  inProgress: { bg: '#FEF6D8', text: '#7A5B00' },
  done: { bg: '#E3F5DE', text: '#1F5C2E' },
};

// 상태 배지 색상 (부드러운 파스텔 톤 — 통계 카드와 같은 색상군)
const STATUS_BADGE_COLORS = {
  RECEIVED: { bg: '#DBEAFE', text: '#1D4ED8' },
  IN_PROGRESS: { bg: '#FEF3C7', text: '#B45309' },
  DONE: { bg: '#DCFCE7', text: '#166534' },
};

// 리스트가 눌릴 때 페이지 전체가 아니라 표 안쪽에서만 가로 스크롤되게 하는 최소 너비
const ROW_MIN_WIDTH = '820px';

// 장애 페이지만의 시그니처 알림 — 슬랙으로 알림을 보낸 건을 보여주는 리스트
// ⚠️ 백엔드에 아직 "슬랙 알림 발송 여부" 필드가 없어서, 우선 임의(mock) 데이터로 채워뒀습니다.
// 나중에 실제 필드가 생기면 이 배열 대신 incidents를 필터링하도록 바꿔주세요.
const MOCK_SLACK_NOTIFICATIONS = [
  { id: 'mock-1', title: '결제 서버 응답 지연', notifiedAt: '2026-08-17 09:12' },
  { id: 'mock-2', title: 'API 게이트웨이 5xx 급증', notifiedAt: '2026-08-17 08:40' },
  { id: 'mock-3', title: '로그인 인증 세션 만료 오류', notifiedAt: '2026-08-16 22:05' },
];

// 공통 AlertList가 받는 형태({icon, badge, meta, ...})로 mock 데이터를 변환
const SLACK_ALERT_ITEMS = MOCK_SLACK_NOTIFICATIONS.map((item) => ({
  id: item.id,
  icon: '💬',
  title: item.title,
  badge: { label: '슬랙 알림 완료', bg: '#DCFCE7', text: '#166534' },
  meta: item.notifiedAt,
}));

function IncidentListPage() {
  const [incidents, setIncidents] = useState([]);
  const [reporterName, setReporterName] = useState("");
  const [status, setStatus] = useState("");
  const [severity, setSeverity] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedIncident, setSelectedIncident] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [stats, setStats] = useState({ received: 0, inProgress: 0, done: 0 });

  const fetchIncidents = () => {
    getIncidents({ reporterName, status, severity, page, size: PAGE_SIZE })
      .then(res => {
        setIncidents(res.data.content || []);
        setTotalPages(res.data.totalPages);
      })
      .catch(err => {
        console.error(err);
        setIncidents([]);
      });
  };

  const fetchStats = () => {
    getIncidentStats().then(res => setStats(res.data));
  };

  useEffect(() => {
    fetchIncidents();
  }, [page, status, severity]);

  useEffect(() => {
    fetchStats();
  }, []);

  const handleSearch = () => {
    setPage(0);
    fetchIncidents();
  };

  return (
    // d-flex flex-column + height:100% — Layout이 준 세로 공간을 그대로 받아서 아래 row가 채우게 한다.
    <div className="container-fluid px-4 d-flex flex-column" style={{ height: '100%', minHeight: 0 }}>
      <div className="row g-4 flex-grow-1" style={{ minHeight: 0 }}>
        {/* 왼쪽 — 장애 목록 (넓게)
            minWidth: 0 — row가 flex라서 안쪽 표의 최소 너비가 이 컬럼 자체를 넓혀버리는 것 방지
            d-flex flex-column + height:100% — 표(RowTable)가 남는 세로 공간을 채우게 한다 */}
        <div className="col-lg-8 d-flex flex-column" style={{ minWidth: 0, height: '100%', minHeight: 0 }}>
          <div className="d-flex justify-content-between align-items-end mb-3">
            <h2 className="fw-bold mb-0" style={{ fontSize: '18px' }}>장애 목록</h2>
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
                  placeholder="등록자 이름 검색"
                  value={reporterName}
                  onChange={(e) => setReporterName(e.target.value)}
                />
                <select
                  className="form-select rounded-pill border-2"
                  style={{ width: '150px', height: '42px' }}
                  value={status}
                  onChange={(e) => setStatus(e.target.value)}
                >
                  <option value="">전체 상태</option>
                  <option value="RECEIVED">접수</option>
                  <option value="IN_PROGRESS">처리중</option>
                  <option value="DONE">해결</option>
                </select>
                <select
                  className="form-select rounded-pill border-2"
                  style={{ width: '150px', height: '42px' }}
                  value={severity}
                  onChange={(e) => setSeverity(e.target.value)}
                >
                  <option value="">전체 심각도</option>
                  <option value="LOW">낮음</option>
                  <option value="MEDIUM">중간</option>
                  <option value="HIGH">높음</option>
                  <option value="CRITICAL">크리티컬</option>
                </select>
              </>
            }
            rightActions={
              <button
                className="btn rounded-pill px-4 fw-semibold shadow-sm text-white"
                style={{ height: '42px', background: REGISTER_BTN_COLOR, border: 'none' }}
                onClick={() => setShowCreateModal(true)}
              >
                등록
              </button>
            }
            headers={['번호', '제목', '심각도', '상태', '등록자', '발생일시', 'SLA 기한']}
            gridTemplateColumns={INCIDENT_ROW_GRID}
            minWidth={ROW_MIN_WIDTH}
            page={page}
            totalPages={totalPages}
            onPageChange={setPage}
          >
            {incidents.length > 0 ? (
              incidents.map((incident, index) => (
                <div
                  key={incident.id}
                  className="d-grid align-items-center bg-white shadow-sm px-3 py-3"
                  style={{ gridTemplateColumns: INCIDENT_ROW_GRID, columnGap: '10px', borderRadius: '18px', cursor: 'pointer' }}
                  onClick={() => setSelectedIncident(incident)}
                >
                  <span>
                    <NumberBadge number={page * PAGE_SIZE + index + 1} />
                  </span>
                  <span className="fw-semibold text-truncate">{incident.title}</span>
                  <span>
                    <div style={{ display: 'flex', gap: '2px' }}>
                      {[1, 2, 3, 4].map((i) => (
                        <div
                          key={i}
                          style={{
                            width: '16px',
                            height: '11px',
                            borderRadius: '2px',
                            backgroundColor: i <= INCIDENT_SEVERITY_WEIGHTS[incident.severity]
                              ? severityColor[incident.severity]
                              : '#E9ECEF',
                          }}
                        />
                      ))}
                    </div>
                  </span>
                  <span>
                    <StatusBadge color={STATUS_BADGE_COLORS[incident.status]}>
                      {INCIDENT_STATUS_LABELS[incident.status]}
                    </StatusBadge>
                  </span>
                  <span className="text-truncate">{incident.reporter || '-'}</span>
                  <span className="small">{formatDateTime(incident.occurredAt)}</span>
                  <span className="small">{formatDateTime(incident.slaDueAt)}</span>
                </div>
              ))
            ) : (
              <div className="text-center py-5 text-muted bg-white" style={{ borderRadius: '18px' }}>
                등록된 장애 내역이 없습니다.
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
            <span className="text-muted small">총 {stats.received + stats.inProgress + stats.done}건</span>
          </div>

          <CompactStatGroup
            tiles={[
              { pastel: STAT_PASTELS.received, tagLabel: 'NEW', value: stats.received, title: '접수', note: '새로 등록됨' },
              { pastel: STAT_PASTELS.inProgress, tagLabel: 'PROG', value: stats.inProgress, title: '처리 중', note: '대응 진행 중' },
              { pastel: STAT_PASTELS.done, tagLabel: 'DONE', value: stats.done, title: '해결', note: '조치 완료' },
            ]}
          />

          <div className="d-flex justify-content-between align-items-end mb-3">
            <h2 className="fw-bold mb-0" style={{ fontSize: '18px' }}>슬랙 알림 완료</h2>
          </div>
          <AlertList items={SLACK_ALERT_ITEMS} />
        </div>
      </div>

      {selectedIncident && (
        <IncidentDetailModal
          incident={selectedIncident}
          onClose={() => setSelectedIncident(null)}
          onUpdated={() => {
            fetchIncidents();
            fetchStats();
          }}
        />
      )}

      {showCreateModal && (
        <IncidentCreateModal
          onClose={() => setShowCreateModal(false)}
          onUpdated={() => {
            fetchIncidents();
            fetchStats();
          }}
        />
      )}
    </div>
  );
}

export default IncidentListPage;
