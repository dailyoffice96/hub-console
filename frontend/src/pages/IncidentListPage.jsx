import { useState, useEffect } from 'react';
import StatCard from '../components/StatCard';
import { getIncidents, getIncidentStats } from '../api/incidentApi';
import IncidentCreateModal from '../components/IncidentCreateModal';
import IncidentDetailModal from '../components/IncidentDetailModal';
import { formatDateTime } from '../utils/format';

const statusLabels = { RECEIVED: '접수', IN_PROGRESS: '처리중', DONE: '해결' };
const severityLabels = { LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 };
const severityColor = { LOW: '#94D2BD', MEDIUM: '#F9DFA0', HIGH: '#F4A261', CRITICAL: '#E63946' };

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
    getIncidents({ reporterName, status, severity, page, size: 10 })
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
    <div className="container-fluid px-4 py-3">
      {/* 상단 통계 카드 (실무형 플랫 화이트 디자인) */}
      <div className="row g-4 mb-4">
        <div className="col-md-4">
          <div className="card border shadow-sm p-4 rounded-4 bg-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <span className="text-muted small fw-semibold d-block mb-1">접수</span>
                <h3 className="fw-bold mb-0 text-dark">{stats.received} <span className="fs-6 fw-normal text-muted">건</span></h3>
              </div>
              <div className="bg-primary bg-opacity-10 text-primary p-3 rounded-3 fw-bold">NEW</div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card border shadow-sm p-4 rounded-4 bg-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <span className="text-muted small fw-semibold d-block mb-1">처리 중</span>
                <h3 className="fw-bold mb-0 text-warning">{stats.inProgress} <span className="fs-6 fw-normal text-muted">건</span></h3>
              </div>
              <div className="bg-warning bg-opacity-10 text-warning p-3 rounded-3 fw-bold">PROG</div>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card border shadow-sm p-4 rounded-4 bg-white">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <span className="text-muted small fw-semibold d-block mb-1">해결</span>
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
              placeholder="등록자 이름 검색"
              value={reporterName}
              onChange={(e) => setReporterName(e.target.value)}
            />
            <select
              className="form-select"
              style={{ width: '150px', height: '44px', borderRadius: '8px' }}
              value={status}
              onChange={(e) => setStatus(e.target.value)}
            >
              <option value="">전체 상태</option>
              <option value="RECEIVED">접수</option>
              <option value="IN_PROGRESS">처리중</option>
              <option value="DONE">해결</option>
            </select>
            <select
              className="form-select"
              style={{ width: '150px', height: '44px', borderRadius: '8px' }}
              value={severity}
              onChange={(e) => setSeverity(e.target.value)}
            >
              <option value="">전체 심각도</option>
              <option value="LOW">낮음</option>
              <option value="MEDIUM">중간</option>
              <option value="HIGH">높음</option>
              <option value="CRITICAL">크리티컬</option>
            </select>
            <button
              className="btn btn-primary px-4 fw-semibold shadow-sm"
              style={{ height: '44px', borderRadius: '8px' }}
              onClick={handleSearch}
            >
              검색
            </button>
            <button
              className="btn btn-danger px-4 fw-semibold ms-auto shadow-sm"
              style={{ height: '44px', borderRadius: '8px' }}
              onClick={() => setShowCreateModal(true)}
            >
              등록
            </button>
          </div>
        </div>

        <div className="table-responsive mb-0">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light text-secondary small text-uppercase">
              <tr>
                <th className="py-3 ps-4">번호</th>
                <th className="py-3">제목</th>
                <th className="py-3">심각도</th>
                <th className="py-3">상태</th>
                <th className="py-3">등록자</th>
                <th className="py-3">발생일시</th>
                <th className="py-3 pe-4">SLA 기한</th>
              </tr>
            </thead>
            <tbody>
              {incidents.length > 0 ? (
                incidents.map((incident, index) => (
                  <tr key={incident.id} onClick={() => setSelectedIncident(incident)} style={{ cursor: 'pointer' }}>
                    <td className="ps-4 fw-medium text-muted">{page * 10 + index + 1}</td>
                    <td className="fw-semibold text-dark">{incident.title}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '2px' }}>
                        {[1, 2, 3, 4].map((i) => (
                          <div
                            key={i}
                            style={{
                              width: '16px',
                              height: '11px',
                              borderRadius: '2px',
                              marginTop: '8px',
                              backgroundColor: i <= severityLabels[incident.severity]
                                ? severityColor[incident.severity]
                                : '#E9ECEF'}} />
                        ))}
                      </div>
                    </td>
                    <td>
                      <span className="badge bg-light text-dark border px-2 py-1">
                        {statusLabels[incident.status]}
                      </span>
                    </td>
                    <td className="text-muted">{incident.reporter || '-'}</td>
                    <td className="text-muted small">{formatDateTime(incident.occurredAt)}</td>
                    <td className="pe-4 text-muted small">{formatDateTime(incident.slaDueAt)}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="7" className="text-center py-5 text-muted">등록된 장애 내역이 없습니다.</td>
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