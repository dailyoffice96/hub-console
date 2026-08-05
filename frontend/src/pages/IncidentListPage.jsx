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
    <div>
      <div className="row mb-4">
        <div className="col-4"><StatCard icon="🆕" count={stats.received} label="접수" /></div>
        <div className="col-4"><StatCard icon="🔄" count={stats.inProgress} label="처리 중" /></div>
        <div className="col-4"><StatCard icon="✅" count={stats.done} label="해결" /></div>
      </div>

      <div className="d-flex mb-3">
        <input
          className="form-control me-2"
          placeholder="등록자 이름 검색"
          value={reporterName}
          onChange={(e) => setReporterName(e.target.value)} />
        <select className="form-select me-2" style={{ width: '150px' }}
          value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">전체 상태</option>
          <option value="RECEIVED">접수</option>
          <option value="IN_PROGRESS">처리중</option>
          <option value="DONE">해결</option>
        </select>
        <select className="form-select me-2" style={{ width: '150px' }}
          value={severity} onChange={(e) => setSeverity(e.target.value)}>
          <option value="">전체 심각도</option>
          <option value="LOW">낮음</option>
          <option value="MEDIUM">중간</option>
          <option value="HIGH">높음</option>
          <option value="CRITICAL">크리티컬</option>
        </select>
        <button className="btn btn-primary me-2" onClick={handleSearch}>검색</button>
        <button className="btn btn-danger ms-auto" onClick={() => setShowCreateModal(true)}>등록</button>
      </div>

      <table className="table table-hover">
        <thead>
          <tr>
            <th>번호</th>
            <th>제목</th>
            <th>심각도</th>
            <th>상태</th>
            <th>등록자</th>
            <th>발생일시</th>
            <th>SLA 기한</th>
          </tr>
        </thead>
        <tbody>
          {incidents.map((incident, index) => (
            <tr key={incident.id} onClick={() => setSelectedIncident(incident)} style={{ cursor: 'pointer' }}>
              <td>{page * 10 + index + 1}</td>
              <td>{incident.title}</td>
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
               </div></td>
              <td>{statusLabels[incident.status]}</td>
              <td>{incident.reporter || '-'}</td>
              <td>{formatDateTime(incident.occurredAt)}</td>
              <td>{formatDateTime(incident.slaDueAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>

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

      <div className="d-flex justify-content-center mt-3">
        <button className="btn btn-outline-secondary me-2" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</button>
        <span className="align-self-center mx-2">{page + 1} / {totalPages} 페이지</span>
        <button className="btn btn-outline-secondary" disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>다음</button>
      </div>
    </div>
  );
}

export default IncidentListPage;