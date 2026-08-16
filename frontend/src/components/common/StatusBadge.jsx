// 상태/액션 배지. 색상 맵은 도메인마다 달라서 공통화하지 않고 페이지 쪽에 두고, color로만 받는다.
export default function StatusBadge({ color, children, onClick, title }) {
    return (
        <span
            className="badge rounded-pill px-3 py-2 fw-semibold"
            style={{
                backgroundColor: color?.bg,
                color: color?.text,
                cursor: onClick ? 'pointer' : undefined,
            }}
            onClick={onClick}
            title={title}
        >
            {children}
        </span>
    );
}
