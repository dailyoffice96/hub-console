import { PRIMARY_NAVY } from '../../constants/designTokens';

// 목록 표 맨 앞 "번호" 컬럼에 쓰는 동그란 순번 배지입니다.
// 몇 번인지는 각 페이지에서 계산해서 넘겨주면 됩니다. (예: page * size + index + 1)
export default function NumberBadge({ number }) {
    return (
        <span
            className="d-inline-flex align-items-center justify-content-center fw-bold text-white rounded-circle"
            style={{
                width: '32px',
                height: '32px',
                fontSize: '14px', // 최소 폰트 크기 14px 기준 준수
                background: PRIMARY_NAVY,
            }}
        >
            {number}
        </span>
    );
}
