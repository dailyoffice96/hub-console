export const USER_STATUS_COLORS = {
    ACTIVE: { bg: '#d1e7dd', text: '#0f5132' },
    DORMANT: { bg: '#fff3cd', text: '#664d03' },
    WITHDRAWN: { bg: '#f8d7da', text: '#842029' }
 }

export const INCIDENT_SEVERITY_LABELS = { LOW: '낮음', MEDIUM: '중간', HIGH: '높음', CRITICAL: '크리티컬' };
export const INCIDENT_SEVERITY_WEIGHTS = { LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 };
export const INCIDENT_STATUS_LABELS = { RECEIVED: '접수', IN_PROGRESS: '처리중', DONE: '해결' };

// 통계 대시보드(막대그래프/점 표시)에서 쓰는 단색. 통계 응답 필드명(received/inProgress/done)을
// 그대로 키로 쓰므로 위 라벨/배지 색상과 대소문자가 다르다 — 서로 다른 용도라 통일하지 않는다.
export const INCIDENT_STATUS_COLORS = { received: '#457B9D', inProgress: '#1D3557', done: '#2A9D8F' };

// 목록/상세 화면의 StatusBadge(배경+글자색 쌍)에서 쓰는 색. IncidentListPage에 각자 따로
// 있던 것을 여기로 모았다.
export const INCIDENT_STATUS_BADGE_COLORS = {
  RECEIVED: { bg: '#DBEAFE', text: '#1D4ED8' },
  IN_PROGRESS: { bg: '#FEF3C7', text: '#B45309' },
  DONE: { bg: '#DCFCE7', text: '#166534' },
};

// 심각도 단색 (작은 막대/점 표시용). IncidentListPage/IncidentMonitoringPage에 각자 따로
// 있던 것을 여기로 모았다.
export const INCIDENT_SEVERITY_COLORS = { LOW: '#94D2BD', MEDIUM: '#F9DFA0', HIGH: '#F4A261', CRITICAL: '#E63946' };

// 심각도 배지 색 (배경+글자색 쌍). IncidentDetailModal에 따로 있던 것을 여기로 모았다.
export const INCIDENT_SEVERITY_BADGE_COLORS = {
  LOW: { bg: '#d1e7dd', text: '#0f5132' },
  MEDIUM: { bg: '#fff3cd', text: '#664d03' },
  HIGH: { bg: '#ffe5d0', text: '#b45309' },
  CRITICAL: { bg: '#f8d7da', text: '#842029' },
};



export const INQUIRY_STATUS_LABELS = { WAITING: '대기', IN_PROGRESS: '처리중', DONE: '완료' };
export const INQUIRY_STATUS_TYPELABELS = { ACCOUNT: '계정문의', PAYMENT: '결제문의', TECHNICAL: '기술문의', SERVICE: '서비스문의', ETC: '기타' };
export const INQUIRY_STATUS_COLORS = {
WAITING: { bg: '#fff3cd', text: '#664d03' },
IN_PROGRESS: { bg: '#cfe2ff', text: '#084298' },
DONE: { bg: '#d1e7dd', text: '#0f5132' }
};

