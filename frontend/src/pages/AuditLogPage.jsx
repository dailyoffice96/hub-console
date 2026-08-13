import { useState, useEffect } from 'react';
import { getAuditLogs, downloadAuditLog } from '../api/auditLogApi';
import { getDailyStats } from '../api/dailyStatsApi';
import { formatDateTime, formatDate, formatCompactNumber } from '../utils/format';

const actionLabels = { CREATE: '등록', UPDATE: '수정', DELETE: '삭제' };
const targetTypeLabels = { INQUIRY: '문의', INCIDENT: '장애', ADMIN: '관리자' };

// 하루(밀리초) - 두 target_date가 실제로 "전일"인지(중간에 집계가 비어있지 않은지) 확인할 때 씀
const ONE_DAY_MS = 24 * 60 * 60 * 1000;

function AuditLogPage() {
    const [logs, setLogs] = useState([]);
    const [adminName, setAdminName] = useState("");
    const [action, setAction] = useState("");
    const [targetType, setTargetType] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [dailyStats, setDailyStats] = useState([]);

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
        fetchLogs();
      };

    const latestStats = dailyStats.length > 0 ? dailyStats[dailyStats.length - 1] : null;
    const previousStats = dailyStats.length > 1 ? dailyStats[dailyStats.length - 2] : null;
    // 하루 앞선 날짜일 때만 "전일 대비"로 표시한다 - 집계가 하루 이상 비어있으면(스케줄러가 하루를
    // 건너뛴 경우 등) 오해를 부르니 그럴 땐 델타를 아예 안 보여준다.
    const hasConsecutiveDelta = !!(latestStats && previousStats
      && new Date(latestStats.targetDate) - new Date(previousStats.targetDate) === ONE_DAY_MS);
    const deltaOf = (key) => (hasConsecutiveDelta ? latestStats[key] - previousStats[key] : null);
    const recentStats = [...dailyStats].reverse().slice(0, 7); // 최근 날짜가 위로 오게

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

  return (
    <div className="container-fluid px-4 py-3">
      {/* 상단 안내 섹션 */}
      <div className="row mx-2 mb-4">
        <div className="col-12 px-0">
          <div className="card border shadow-sm p-4 rounded-4 bg-white">
            <h5 className="fw-bold text-dark mb-1">시스템 감사 로그 관리</h5>
            <p className="text-muted small mb-0">관리자의 주요 시스템 변경 이력을 조회하고 엑셀 파일로 다운로드할 수 있습니다.</p>
          </div>
        </div>
      </div>

      {/* 일별 통계 (신규 가입자/문의/장애) - DailyStatsService 스케줄러가 매일 자정에 집계한 값 */}
      <div className="row mx-2 mb-4">
        <div className="col-12 px-0">
          <div className="card border shadow-sm p-4 rounded-4 bg-white">
            <div className="d-flex align-items-baseline justify-content-between mb-3 flex-wrap gap-1">
              <h5 className="fw-bold text-dark mb-0">일별 통계</h5>
              {latestStats && (
                <span className="text-muted small">{formatDate(latestStats.targetDate)} 기준</span>
              )}
            </div>

            {!latestStats ? (
              <p className="text-muted small mb-0">아직 집계된 일별 통계가 없습니다.</p>
            ) : (
              <>
                <div className="row g-3">
                  <StatTile
                    label="신규 가입자"
                    value={latestStats.newUsers}
                    delta={deltaOf('newUsers')}
                    deltaGoodDirection="up"
                  />
                  <StatTile
                    label="신규 문의"
                    value={latestStats.newInquiries}
                    delta={deltaOf('newInquiries')}
                    deltaGoodDirection={null}
                  />
                  <StatTile
                    label="신규 장애"
                    value={latestStats.newIncidents}
                    delta={deltaOf('newIncidents')}
                    deltaGoodDirection="down"
                  />
                </div>

                {recentStats.length > 1 && (
                  <div className="table-responsive mt-3">
                    <table className="table table-sm mb-0">
                      <thead>
                        <tr className="text-muted small text-uppercase">
                          <th className="fw-semibold">날짜</th>
                          <th className="fw-semibold text-end">신규 가입자</th>
                          <th className="fw-semibold text-end">신규 문의</th>
                          <th className="fw-semibold text-end">신규 장애</th>
                        </tr>
                      </thead>
                      <tbody>
                        {recentStats.map((row) => (
                          <tr key={row.id}>
                            <td className="text-muted small">{formatDate(row.targetDate)}</td>
                            <td className="text-end">{formatCompactNumber(row.newUsers)}</td>
                            <td className="text-end">{formatCompactNumber(row.newInquiries)}</td>
                            <td className="text-end">{formatCompactNumber(row.newIncidents)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>

      {/* 검색 및 테이블 통합 박스 (mx-2로 바깥쪽 여백 확보) */}
      <div className="card border shadow-sm rounded-4 bg-white overflow-hidden mx-2">
        <div className="p-4 border-bottom bg-light bg-opacity-25">
          <div className="d-flex flex-wrap gap-2 align-items-center justify-content-between">
            <div className="d-flex flex-wrap gap-2 align-items-center flex-grow-1">
              <input
                className="form-control"
                style={{ maxWidth: '200px', height: '44px', borderRadius: '8px' }}
                placeholder="담당자 이름 검색"
                value={adminName}
                onChange={(e) => setAdminName(e.target.value)}
              />
              <select
                className="form-select"
                style={{ width: '150px', height: '44px', borderRadius: '8px' }}
                value={action}
                onChange={(e) => setAction(e.target.value)}
              >
                <option value="">변경 상태</option>
                <option value="CREATE">등록</option>
                <option value="UPDATE">수정</option>
                <option value="DELETE">삭제</option>
              </select>
              <select
                className="form-select"
                style={{ width: '150px', height: '44px', borderRadius: '8px' }}
                value={targetType}
                onChange={(e) => setTargetType(e.target.value)}
              >
                <option value="">전체 대상</option>
                <option value="INQUIRY">문의</option>
                <option value="INCIDENT">장애</option>
                <option value="ADMIN">관리자</option>
              </select>
              <button
                className="btn btn-primary px-4 fw-semibold shadow-sm"
                style={{ height: '44px', borderRadius: '8px' }}
                onClick={handleSearch}
              >
                검색
              </button>
            </div>
            <div>
              <button
                className="btn btn-success px-4 fw-semibold shadow-sm"
                style={{ height: '44px', borderRadius: '8px' }}
                onClick={handleDownload}
              >
                엑셀 다운
              </button>
            </div>
          </div>
        </div>

        <div className="table-responsive mb-0">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light text-secondary small text-uppercase">
              <tr>
                <th className="py-3 ps-4">번호</th>
                <th className="py-3">담당자</th>
                <th className="py-3">변경타입</th>
                <th className="py-3">변경상태</th>
                <th className="py-3">내용</th>
                <th className="py-3 pe-4">일시</th>
              </tr>
            </thead>
            <tbody>
              {logs.length > 0 ? (
                logs.map((log, index) => (
                  <tr key={log.id}>
                    <td className="ps-4 fw-medium text-muted">{page * 15 + index + 1}</td>
                    <td className="fw-semibold text-dark">{log.admin || '-'}</td>
                    <td>
                      <span className="badge bg-light text-dark border px-2 py-1">
                        {targetTypeLabels[log.targetType]} #{log.targetId}
                      </span>
                    </td>
                    <td>
                      <span className="badge bg-secondary bg-opacity-10 text-secondary border px-2 py-1">
                        {actionLabels[log.action]}
                      </span>
                    </td>
                    <td className="text-dark">{log.detail}</td>
                    <td className="pe-4 text-muted small">{formatDateTime(log.createdAt)}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" className="text-center py-5 text-muted">조회된 감사 로그가 없습니다.</td>
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
    </div>
  );
}

// deltaGoodDirection: 'up'(늘어나는 게 좋음, 예: 가입자) | 'down'(줄어드는 게 좋음, 예: 장애) | null(중립, 예: 문의)
function StatTile({ label, value, delta, deltaGoodDirection }) {
  let deltaColorClass = 'text-muted';
  if (delta !== null && delta !== 0 && deltaGoodDirection) {
    const isGood = deltaGoodDirection === 'up' ? delta > 0 : delta < 0;
    deltaColorClass = isGood ? 'text-success' : 'text-danger';
  }

  return (
    <div className="col-12 col-md-4">
      <div className="border rounded-4 p-3 h-100 bg-light bg-opacity-25">
        <div className="text-muted small mb-1">{label}</div>
        <div className="d-flex align-items-baseline flex-wrap gap-2">
          <span className="fs-3 fw-bold text-dark">{formatCompactNumber(value)}</span>
          {delta !== null && (
            <span className={`small fw-semibold ${deltaColorClass}`}>
              {delta > 0 ? `+${delta}` : delta} 전일 대비
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

export default AuditLogPage;