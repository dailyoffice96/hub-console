export const USER_STATUS_COLORS = {
    ACTIVE: { bg: '#d1e7dd', text: '#0f5132' },
    DORMANT: { bg: '#fff3cd', text: '#664d03' },
    WITHDRAWN: { bg: '#f8d7da', text: '#842029' }
 }

export const INCIDENT_SEVERITY_LABELS = { LOW: '낮음', MEDIUM: '중간', HIGH: '높음', CRITICAL: '크리티컬' };
export const INCIDENT_SEVERITY_WEIGHTS = { LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 };
export const INCIDENT_STATUS_LABELS = { RECEIVED: '접수', IN_PROGRESS: '처리중', DONE: '해결' };
export const INCIDENT_STATUS_COLORS= { received: '#457B9D', inProgress: '#1D3557', done: '#2A9D8F' };



export const INQUIRY_STATUS_LABELS = { WAITING: '대기', IN_PROGRESS: '처리중', DONE: '완료' };
export const INQUIRY_STATUS_TYPELABELS = { ACCOUNT: '계정문의', PAYMENT: '결제문의', TECHNICAL: '기술문의', SERVICE: '서비스문의', ETC: '기타' };
export const INQUIRY_STATUS_COLORS = {
WAITING: { bg: '#fff3cd', text: '#664d03' },
IN_PROGRESS: { bg: '#cfe2ff', text: '#084298' },
DONE: { bg: '#d1e7dd', text: '#0f5132' }
};

