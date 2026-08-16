import { PRIMARY_NAVY, SOFT_SHADOW } from '../../constants/designTokens';

// 우측 레일에 쌓는 "최근 항목 알림" 카드 리스트 (장애의 슬랙 알림, 문의의 담당자 미배정 알림 등).
export default function AlertList({ items }) {
    if (!items || items.length === 0) return null;

    return (
        <div className="rounded-4 bg-white p-3 mb-4" style={{ boxShadow: SOFT_SHADOW }}>
            <div className="d-flex flex-column">
                {items.map((item, idx) => (
                    <div
                        key={item.id}
                        onClick={item.onClick}
                        className="d-flex align-items-center gap-3 py-3"
                        style={{
                            cursor: item.onClick ? 'pointer' : 'default',
                            borderTop: idx === 0 ? 'none' : '1px solid #F1F2F8',
                        }}
                    >
                        <span
                            className="d-flex align-items-center justify-content-center rounded-3 flex-shrink-0"
                            style={{ width: '36px', height: '36px', background: PRIMARY_NAVY, fontSize: '15px' }}
                        >
                            {item.icon}
                        </span>
                        <div className="flex-grow-1 text-truncate">
                            <div className="fw-bold text-dark text-truncate" style={{ fontSize: '14px' }}>
                                {item.title}
                            </div>
                            {item.badge && (
                                <span
                                    className="badge rounded-pill mt-1"
                                    style={{ background: item.badge.bg, color: item.badge.text, fontSize: '14px' }}
                                >
                                    {item.badge.label}
                                </span>
                            )}
                        </div>
                        <span className="fw-bold text-dark flex-shrink-0" style={{ fontSize: '14px' }}>
                            {item.meta}
                        </span>
                    </div>
                ))}
            </div>
        </div>
    );
}
