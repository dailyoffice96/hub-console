import { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { Doughnut, Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement
} from 'chart.js';
import { getIncidents, getIncidentSeverityStats, getIncidentStats } from '../api/incidentApi';

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement);

const severityColor = { CRITICAL: '#E63946', HIGH: '#F4A261', MEDIUM: '#F9DFA0', LOW: '#94D2BD' };
const severityLabel = { CRITICAL: '심각', HIGH: '높음', MEDIUM: '중간', LOW: '낮음' };

const statusColor = { received: '#457B9D', inProgress: '#1D3557', done: '#2A9D8F' };
const statusLabel = { received: '접수', inProgress: '진행중', done: '완료' };

const formatDateTime = (value) => {
  if (!value) return '-';
  const date = new Date(value);
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')} 발생`;
};

const getElapsedTime = (value) => {
  if (!value) return '';
  const diffMinutes = Math.floor((new Date() - new Date(value)) / (1000 * 60));
  if (diffMinutes < 60) return `${diffMinutes}분 경과`;
  const hours = Math.floor(diffMinutes / 60);
  const minutes = diffMinutes % 60;
  return `${hours}시간 ${minutes}분 경과`;
};

function IncidentMonitoringPage() {
  const [incidents, setIncidents] = useState([]);
  const [severityStats, setSeverityStats] = useState({ critical: 0, high: 0, medium: 0, low: 0 });
  const [statusStats, setStatusStats] = useState({ received: 0, inProgress: 0, done: 0 });
  const stompClient = useRef(null);

  const fetchData = () => {
      getIncidents({ page: 0, size: 7 }).then(res => setIncidents(res.data.content));
      getIncidentSeverityStats().then(res => setSeverityStats(res.data));
      getIncidentStats().then(res => setStatusStats(res.data));
  };

  useEffect(() => {
    fetchData();

    const client = new Client({
      // 재연결마다 새 SockJS 소켓을 만들어야 한다. 소켓 인스턴스를 밖에서 한 번만 만들어 캡처하면
      // 최초 연결이 끊긴 뒤 stompjs가 재연결을 시도할 때마다 이미 닫힌 소켓을 재사용하게 되어
      // reconnectDelay를 걸어도 실제로는 재연결이 안 된다.
      webSocketFactory: () => new SockJS('http://localhost:9000/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        client.subscribe('/topic/incidents', () => {
          fetchData();
        });
      },
      onWebSocketClose: () => {
        console.warn('[incidents] WebSocket 연결이 끊어졌습니다. 재연결을 시도합니다.');
      },
      onStompError: (frame) => {
        console.error('[incidents] STOMP 오류:', frame.headers?.message, frame.body);
      },
      onWebSocketError: (event) => {
        console.error('[incidents] WebSocket 오류:', event);
      },
    });
    client.activate();
    stompClient.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  const total = severityStats.critical + severityStats.high + severityStats.medium + severityStats.low;

  const donutData = {
    labels: ['심각', '높음', '중간', '낮음'],
    datasets: [
      {
        data: [severityStats.critical, severityStats.high, severityStats.medium, severityStats.low],
        backgroundColor: [severityColor.CRITICAL, severityColor.HIGH, severityColor.MEDIUM, severityColor.LOW],
        borderWidth: 0,
      },
    ],
  };

  const donutOptions = {
    plugins: { legend: { display: false } },
    cutout: '65%',
    maintainAspectRatio: false,
  };

  const barData = {
    labels: ['접수', '진행중', '완료'],
    datasets: [
      {
        data: [statusStats.received, statusStats.inProgress, statusStats.done],
        backgroundColor: [statusColor.received, statusColor.inProgress, statusColor.done],
        borderRadius: 5,
      },
    ],
  };

  const barOptions = {
    plugins: { legend: { display: false } },
    scales: {
      x: { grid: { display: false } },
      y: { grid: { color: '#f1f1f1' }, ticks: { stepSize: 1 } }
    },
    maintainAspectRatio: false,
  };

  return (
    <div className="container-fluid px-4 py-3">
      <div className="row g-4 mx-1">
        {/* 심각도 현황 + 상태별 처리 현황 */}
        <div className="col-md-5 d-flex flex-column gap-4 px-0">
          {/* 심각도 현황*/}
          <div className="card border shadow-sm p-4 rounded-4 bg-white">
            <h6 className="fw-bold mb-3 text-dark">심각도 현황</h6>
            <div className="d-flex align-items-center justify-content-between">
              <div style={{ position: 'relative', height: '150px', width: '150px', flexShrink: 0 }}>
                <Doughnut data={donutData} options={donutOptions} />
                <div style={{
                  position: 'absolute', top: '50%', left: '50%',
                  transform: 'translate(-50%, -50%)', textAlign: 'center'
                }}>
                  <div style={{ fontSize: '22px', fontWeight: 'bold' }}>{total}</div>
                  <div style={{ fontSize: '11px', color: '#888' }}>전체</div>
                </div>
              </div>

              <div className="ms-3 flex-grow-1">
                {Object.entries(severityStats).map(([key, value]) => (
                  <div key={key} className="d-flex justify-content-between align-items-center mb-2">
                    <span style={{ fontSize: '14px', fontWeight: '600' }}>
                      <span className="me-2" style={{
                        display: 'inline-block', width: 9, height: 9, borderRadius: '50%',
                        backgroundColor: severityColor[key.toUpperCase()]
                      }}></span>
                      {severityLabel[key.toUpperCase()]}
                    </span>
                    <span style={{ fontSize: '14px' }} className="fw-bold">
                      {value}건 <span className="text-muted fw-normal" style={{ fontSize: '11px' }}>({total > 0 ? Math.round(value / total * 100) : 0}%)</span>
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* 상태별 처리 현황 */}
          <div className="card border shadow-sm p-4 rounded-4 bg-white flex-grow-1 d-flex flex-column justify-content-between">
            <h6 className="fw-bold mb-3 text-dark">상태별 처리 현황</h6>

            <div style={{ height: '135px', width: '100%' }}>
              <Bar data={barData} options={barOptions} />
            </div>

            <div className="d-flex justify-content-around align-items-center mt-3 pt-3 border-top">
              {Object.entries(statusStats).map(([key, value]) => (
                <div key={key} className="d-flex align-items-center">
                  <span className="me-2" style={{
                    display: 'inline-block', width: 9, height: 9, borderRadius: '50%',
                    backgroundColor: statusColor[key]
                  }}></span>
                  <span style={{ fontSize: '13px', fontWeight: '600' }} className="me-1">{statusLabel[key]}</span>
                  <span style={{ fontSize: '13px' }} className="fw-bold text-muted">({value}건)</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 실시간 장애 목록 */}
        <div className="col-md px-0 ms-md-4">
          <div className="card border shadow-sm p-4 rounded-4 bg-white h-100">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h6 className="fw-bold mb-0 text-dark">실시간 장애 목록 (최근 7건)</h6>
                <span className="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 px-2 py-1">● 실시간 연결됨</span>
            </div>
            <ul className="list-group list-group-flush">
              {incidents.map(incident => (
                <li key={incident.id} className="list-group-item d-flex justify-content-between align-items-center py-3 px-0 border-light">
                  <div>
                    <span className="badge me-2" style={{ backgroundColor: severityColor[incident.severity] }}>
                      {severityLabel[incident.severity]}
                    </span>
                    <span className="fw-semibold text-dark">{incident.title}</span>
                    <div className="text-danger small mt-1">
                      {formatDateTime(incident.occurredAt)}
                    </div>
                  </div>
                  <div className="text-end">
                    <small className="text-muted d-block fw-semibold">{incident.status}</small>
                    <small className="text-muted">{getElapsedTime(incident.occurredAt)}</small>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}

export default IncidentMonitoringPage;