import { useState } from 'react';
import { LuChevronDown, LuChevronUp } from 'react-icons/lu';
import { useAuditLogList, PAGE_SIZE } from '../hooks/useAuditLogList';
import { formatDateTime, formatDate, formatCompactNumber } from '../utils/format';
import RowTable from '../components/common/RowTable';
import NumberBadge from '../components/common/NumberBadge';
import StatusBadge from '../components/common/StatusBadge';
import GradientStatGrid from '../components/stats/GradientStatGrid';
import { SOFT_SHADOW, TABLE_HEADER_TEXT, TABLE_TEXT_COLOR } from '../constants/designTokens';

const actionLabels = { CREATE: '등록', UPDATE: '수정', DELETE: '삭제' };
const targetTypeLabels = { INQUIRY: '문의', INCIDENT: '장애', ADMIN: '관리자' };

// 변경상태(action) 배지 색상 — UserListPage의 USER_STATUS_COLORS와 같은 방식
const ACTION_BADGE_COLORS = {
    CREATE: { bg: '#D1FAE5', text: '#059669' },
    UPDATE: { bg: '#FEF3C7', text: '#D97706' },
    DELETE: { bg: '#FEE2E2', text: '#DC2626' },
};

// 하루(밀리초) - 두 target_date가 실제로 "전일"인지(중간에 집계가 비어있지 않은지) 확인할 때 씀
const ONE_DAY_MS = 24 * 60 * 60 * 1000;

// 감사로그 행의 컬럼 비율 (헤더와 데이터 행이 동일하게 사용)
const LOG_ROW_GRID = '0.5fr 1.1fr 1.3fr 1fr 2.6fr 1.3fr';

// 일별 통계 이력 목록이 이 높이를 넘으면 그 안에서만 스크롤됨 (대략 카드 3장 정도)
const DAILY_HISTORY_MAX_HEIGHT = '320px';

// 리스트가 눌릴 때 페이지 전체가 아니라 표 안쪽에서만 가로 스크롤되게 하는 최소 너비
const ROW_MIN_WIDTH = '760px';

// UserListPage와 동일한 색상군 (그라데이션 대신 단색)
const FLAT_COLORS = {
    users: '#4C5F91',
    inquiries: '#D97706',
    incidents: '#DC2626',
};

// 통계 카드 하단 흰색 영역 (제목 + 전일 대비) — GradientStatGrid의 footer로 그대로 넘김
function StatFooter({ title, delta, deltaGoodDirection }) {
    let deltaColorClass = 'text-muted';
    if (delta !== null && delta !== 0 && deltaGoodDirection) {
        const isGood = deltaGoodDirection === 'up' ? delta > 0 : delta < 0;
        deltaColorClass = isGood ? 'text-success' : 'text-danger';
    }

    return (
        <>
            <div className="fw-bold text-dark">{title}</div>
            {delta !== null ? (
                <span className={`small fw-semibold ${deltaColorClass}`}>
                    {delta > 0 ? `+${delta}` : delta} 전일 대비
                </span>
            ) : (
                <span className="small text-muted">전일 대비 없음</span>
            )}
        </>
    );
}

function AuditLogPage() {
    // 데이터 조회/상태는 전부 useAuditLogList 훅이 들고 있고, 여기선 화면만 그린다.
    const {
        logs, adminName, setAdminName, action, setAction, targetType, setTargetType,
        page, setPage, totalPages, dailyStats,
        handleSearch, handleDownload,
    } = useAuditLogList();

    const [showHistory, setShowHistory] = useState(false); // 이력 목록은 기본으로 접어둬서 화면을 덜 차지하게 함

    const latestStats = dailyStats.length > 0 ? dailyStats[dailyStats.length - 1] : null;
    const previousStats = dailyStats.length > 1 ? dailyStats[dailyStats.length - 2] : null;
    // 하루 앞선 날짜일 때만 "전일 대비"로 표시한다 - 집계가 하루 이상 비어있으면(스케줄러가 하루를
    // 건너뛴 경우 등) 오해를 부르니 그럴 땐 델타를 아예 안 보여준다.
    const hasConsecutiveDelta = !!(latestStats && previousStats
        && new Date(latestStats.targetDate) - new Date(previousStats.targetDate) === ONE_DAY_MS);
    const deltaOf = (key) => (hasConsecutiveDelta ? latestStats[key] - previousStats[key] : null);
    // 최근 날짜가 맨 위로 오는 탑다운 순서. 전부 보여주는 대신 목록 자체를 스크롤로 훑어보게 한다.
    const historyStats = [...dailyStats].reverse();

    return (
        // d-flex flex-column + height:100% — Layout이 준 세로 공간을 그대로 받아서, 아래 로그
        // 표 영역(flex-grow-1)만 남는 공간을 채우게 한다. 그 외 섹션은 원래 크기 그대로 고정.
        <div className="container-fluid px-4 d-flex flex-column" style={{ height: '100%', minHeight: 0 }}>

            <div className="d-flex justify-content-between align-items-end mb-3">
                <div className="d-flex align-items-center gap-2">
                    <h2 className="fw-bold mb-0" style={{ fontSize: '18px' }}>일별 통계</h2>
                    {/* 화살표를 누르면 아래 날짜별 이력 목록이 펼쳐지고/접힌다 */}
                    <button
                        type="button"
                        className="btn btn-sm btn-light rounded-circle d-flex align-items-center justify-content-center p-0 border"
                        style={{ width: '26px', height: '26px' }}
                        onClick={() => setShowHistory((prev) => !prev)}
                        title={showHistory ? '이력 접기' : '이력 펼치기'}
                    >
                        {showHistory ? <LuChevronUp size={16} /> : <LuChevronDown size={16} />}
                    </button>
                </div>
                {latestStats && (
                    <span className="text-muted small">{formatDate(latestStats.targetDate)} 기준</span>
                )}
            </div>

            {!latestStats ? (
                <div className="card border-0 rounded-4 bg-white p-4 mb-5 text-muted small" style={{ boxShadow: SOFT_SHADOW }}>
                    아직 집계된 일별 통계가 없습니다.
                </div>
            ) : (
                <>
                    <GradientStatGrid
                        columnClassName="col-12 col-md-4"
                        tiles={[
                            {
                                background: FLAT_COLORS.users,
                                tagLabel: 'USERS',
                                value: formatCompactNumber(latestStats.newUsers),
                                unit: '명',
                                footer: <StatFooter title="신규 가입자" delta={deltaOf('newUsers')} deltaGoodDirection="up" />,
                            },
                            {
                                background: FLAT_COLORS.inquiries,
                                tagLabel: 'INQUIRIES',
                                value: formatCompactNumber(latestStats.newInquiries),
                                unit: '건',
                                footer: <StatFooter title="신규 문의" delta={deltaOf('newInquiries')} deltaGoodDirection={null} />,
                            },
                            {
                                background: FLAT_COLORS.incidents,
                                tagLabel: 'INCIDENTS',
                                value: formatCompactNumber(latestStats.newIncidents),
                                unit: '건',
                                footer: <StatFooter title="신규 장애" delta={deltaOf('newIncidents')} deltaGoodDirection="down" />,
                            },
                        ]}
                    />

                    {showHistory && historyStats.length > 1 && (
                        <div className="card border-0 rounded-4 overflow-hidden mb-5" style={{ boxShadow: SOFT_SHADOW }}>
                            {/* 좌우 컬럼 대신 날짜 카드 안에서 위→아래로 읽히게(탑다운) 해서 가로 스크롤이
                                필요 없다. 최근 날짜가 맨 위로 쌓이고, 길어지면 이 안에서만 스크롤된다. */}
                            <div
                                className="p-3 d-flex flex-column gap-2"
                                style={{ background: '#FAFAFD', maxHeight: DAILY_HISTORY_MAX_HEIGHT, overflowY: 'auto' }}
                            >
                                {historyStats.map((row) => (
                                    <div key={row.id} className="bg-white shadow-sm px-3 py-2" style={{ borderRadius: '14px' }}>
                                        <div className="fw-bold" style={{ color: TABLE_HEADER_TEXT, fontSize: '14px' }}>
                                            {formatDate(row.targetDate)}
                                        </div>
                                        <div style={{ color: TABLE_TEXT_COLOR }}>
                                            <div className="d-flex justify-content-between py-1" style={{ borderTop: '1px solid #F1F2F8' }}>
                                                <span className="small">신규 가입자</span>
                                                <span className="fw-semibold small">{formatCompactNumber(row.newUsers)}명</span>
                                            </div>
                                            <div className="d-flex justify-content-between py-1" style={{ borderTop: '1px solid #F1F2F8' }}>
                                                <span className="small">신규 문의</span>
                                                <span className="fw-semibold small">{formatCompactNumber(row.newInquiries)}건</span>
                                            </div>
                                            <div className="d-flex justify-content-between py-1" style={{ borderTop: '1px solid #F1F2F8' }}>
                                                <span className="small">신규 장애</span>
                                                <span className="fw-semibold small">{formatCompactNumber(row.newIncidents)}건</span>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </>
            )}

            <div className="d-flex justify-content-between align-items-end mb-3">
                <h2 className="fw-bold mb-0" style={{ fontSize: '18px' }}>감사 로그 목록</h2>
            </div>

            {/* 공통 RowTable 적용 (검색 영역 + 헤더 + 데이터 행 + 페이징을 카드 하나로 통합)
                flex-grow-1 + minHeight:0 — 위쪽 섹션들을 뺀 나머지 세로 공간을 표가 전부 차지하고,
                그 안에서만(데이터 행 부분) 세로 스크롤이 생기게 한다. */}
            <div className="flex-grow-1 d-flex flex-column mb-4" style={{ minHeight: 0 }}>
            <RowTable
                onSearch={handleSearch}
                filters={
                    <>
                        <input
                            className="form-control rounded-pill border-2"
                            style={{ maxWidth: '200px', height: '42px' }}
                            placeholder="담당자 이름 검색"
                            value={adminName}
                            onChange={(e) => setAdminName(e.target.value)}
                        />
                        <select
                            className="form-select rounded-pill border-2"
                            style={{ width: '150px', height: '42px' }}
                            value={action}
                            onChange={(e) => setAction(e.target.value)}
                        >
                            <option value="">변경 상태</option>
                            <option value="CREATE">등록</option>
                            <option value="UPDATE">수정</option>
                            <option value="DELETE">삭제</option>
                        </select>
                        <select
                            className="form-select rounded-pill border-2"
                            style={{ width: '150px', height: '42px' }}
                            value={targetType}
                            onChange={(e) => setTargetType(e.target.value)}
                        >
                            <option value="">전체 대상</option>
                            <option value="INQUIRY">문의</option>
                            <option value="INCIDENT">장애</option>
                            <option value="ADMIN">관리자</option>
                        </select>
                    </>
                }
                rightActions={
                    <button
                        className="btn btn-outline-success rounded-pill px-3 fw-semibold shadow-sm"
                        style={{ height: '42px' }}
                        onClick={handleDownload}
                    >
                        엑셀 다운
                    </button>
                }
                headers={['번호', '담당자', '변경타입', '변경상태', '내용', '일시']}
                gridTemplateColumns={LOG_ROW_GRID}
                minWidth={ROW_MIN_WIDTH}
                page={page}
                totalPages={totalPages}
                onPageChange={setPage}
            >
                {logs.length > 0 ? (
                    logs.map((log, index) => (
                        <div
                            key={log.id}
                            className="d-grid align-items-center bg-white shadow-sm px-3 py-3"
                            style={{ gridTemplateColumns: LOG_ROW_GRID, columnGap: '10px', borderRadius: '18px' }}
                        >
                            <span>
                                <NumberBadge number={page * PAGE_SIZE + index + 1} />
                            </span>
                            <span className="fw-semibold">{log.admin || '-'}</span>
                            <span>
                                {/* Bootstrap .badge 기본 글자색이 흰색이라 bg-light 위에서 안 보였음 — 명시적으로 색 지정 */}
                                <span className="badge bg-light border px-2 py-1 fw-semibold" style={{ color: TABLE_TEXT_COLOR }}>
                                    {targetTypeLabels[log.targetType]} #{log.targetId}
                                </span>
                            </span>
                            <span>
                                <StatusBadge color={ACTION_BADGE_COLORS[log.action]}>{actionLabels[log.action]}</StatusBadge>
                            </span>
                            <span className="text-truncate">{log.detail}</span>
                            <span className="small">{formatDateTime(log.createdAt)}</span>
                        </div>
                    ))
                ) : (
                    <div className="text-center py-5 text-muted bg-white" style={{ borderRadius: '18px' }}>
                        조회된 감사 로그가 없습니다.
                    </div>
                )}
            </RowTable>
            </div>
        </div>
    );
}

export default AuditLogPage;
