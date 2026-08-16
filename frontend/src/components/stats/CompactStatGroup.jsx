import { PRIMARY_NAVY, SOFT_SHADOW } from '../../constants/designTokens';

// 표 옆 좁은 레일에 세로로 쌓는 파스텔 톤 통계 카드 한 줄입니다. (장애/문의 목록의 "현황" 영역)
// pastel: { bg, text } 형태의 파스텔 배경/글자 색상
function CompactStatTile({ pastel, tagLabel, value, unit = '건', title, note }) {
    return (
        <div className="rounded-4 p-3 d-flex align-items-center justify-content-between" style={{ background: pastel.bg }}>
            <div>
                <div className="fw-bold" style={{ fontSize: '15px', color: pastel.text }}>{title}</div>
                <div className="mt-1" style={{ fontSize: '14px', color: pastel.text, opacity: 0.75 }}>{note}</div>
            </div>

            <div className="d-flex align-items-center gap-2 flex-shrink-0">
                {/* 짧은 영문 태그(NEW/WAIT 등)를 담는 칩. 최소 폰트 14px 기준에 맞춰 칩 크기도 같이 키움 */}
                <span
                    className="d-flex align-items-center justify-content-center rounded-3 flex-shrink-0 px-2"
                    style={{ minWidth: '38px', height: '28px', background: PRIMARY_NAVY }}
                >
                    <span className="text-white fw-bold" style={{ fontSize: '14px' }}>{tagLabel}</span>
                </span>
                <span className="fw-bold" style={{ fontSize: '24px', color: pastel.text, lineHeight: 1 }}>
                    {value}
                    <span className="fw-normal ms-1" style={{ fontSize: '14px', opacity: 0.75 }}>{unit}</span>
                </span>
            </div>
        </div>
    );
}

// 통계 카드 여러 개를 흰 카드 하나로 감싸는 그룹 래퍼입니다.
// tiles: CompactStatTile에 넘길 props 배열 [{ pastel, tagLabel, value, unit, title, note }]
export default function CompactStatGroup({ tiles }) {
    return (
        <div className="rounded-4 bg-white p-3 mb-4" style={{ boxShadow: SOFT_SHADOW }}>
            <div className="d-flex flex-column gap-3">
                {tiles.map((tile, index) => (
                    <CompactStatTile key={tile.key ?? index} {...tile} />
                ))}
            </div>
        </div>
    );
}
