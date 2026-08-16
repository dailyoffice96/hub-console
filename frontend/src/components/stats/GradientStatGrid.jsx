import { PRIMARY_NAVY, SOFT_SHADOW } from '../../constants/designTokens';

// 상단은 그라데이션(또는 단색) 컬러 영역, 하단은 흰색 영역으로 구성된 통계 카드 한 장입니다.
// footer 자리에는 페이지마다 다른 하단 내용(노트+제목+화살표, 혹은 제목+전일 대비 등)을 그대로 넘기면 됩니다.
function StatGradientTile({ background, tagLabel, value, unit, footer }) {
    return (
        <div className="card border-0 rounded-4 overflow-hidden h-100" style={{ boxShadow: SOFT_SHADOW }}>
            {/* 상단 컬러 영역 */}
            <div
                className="position-relative overflow-hidden d-flex flex-column justify-content-center px-4"
                style={{ background, height: '100px' }}
            >
                <span
                    className="position-absolute rounded-circle bg-white bg-opacity-25"
                    style={{ width: '110px', height: '110px', right: '-30px', top: '-40px' }}
                />
                <span
                    className="position-absolute rounded-circle bg-white bg-opacity-10"
                    style={{ width: '70px', height: '70px', right: '20px', bottom: '-40px' }}
                />
                <span className="text-white-50 small fw-semibold position-relative" style={{ letterSpacing: '0.06em' }}>
                    {tagLabel}
                </span>
                <div className="text-white fw-bold position-relative" style={{ fontSize: '30px', lineHeight: 1.2 }}>
                    {value}
                    {unit && <span className="fs-6 fw-normal opacity-75 ms-1">{unit}</span>}
                </div>
            </div>

            {/* 하단 흰색 영역 — 페이지마다 다른 내용은 footer로 넘겨받음 */}
            <div className="bg-white p-3 d-flex justify-content-between align-items-center">
                {footer}
            </div>
        </div>
    );
}

// 통계 카드 그리드 맨 끝에 붙는 "더보기" 카드 (회원/관리자 목록에서 사용)
function MoreStatTile({ image, label = '전체 통계', buttonLabel = '+ 더보기', background, onClick }) {
    return (
        <div
            className="card border-0 rounded-4 overflow-hidden h-100 position-relative"
            style={{ background, cursor: 'pointer', minHeight: '164px', boxShadow: SOFT_SHADOW }}
            onClick={onClick}
            role="button"
        >
            {image && (
                <img
                    src={image}
                    alt=""
                    className="position-absolute bottom-0"
                    style={{ right: '2px', height: '85%', width: 'auto' }}
                />
            )}

            <div className="position-relative h-100 d-flex flex-column justify-content-center ps-4" style={{ maxWidth: '60%' }}>
                <span className="fw-bold mb-2" style={{ fontSize: '15px', color: PRIMARY_NAVY }}>{label}</span>
                <span
                    className="d-inline-flex align-items-center justify-content-center bg-white text-primary fw-bold rounded-pill px-3 py-2"
                    style={{ fontSize: '14px', width: 'fit-content' }}
                >
                    {buttonLabel}
                </span>
            </div>
        </div>
    );
}

// 회원/관리자/감사로그 목록의 "현황" 통계 카드 그리드입니다.
// tiles           : StatGradientTile에 넘길 props 배열 [{ background, tagLabel, value, unit, footer }]
// columnClassName : 카드 하나가 차지할 bootstrap 컬럼 클래스 (기본은 4열 그리드용 col-6 col-lg-3)
// moreTile        : 넘기면 그리드 맨 끝에 "더보기" 카드가 하나 더 붙습니다. (안 넘기면 표시 안 함)
export default function GradientStatGrid({ tiles, columnClassName = 'col-6 col-lg-3', moreTile }) {
    return (
        <div className="row g-4 mb-3">
            {tiles.map((tile, index) => (
                <div className={columnClassName} key={tile.key ?? index}>
                    <StatGradientTile {...tile} />
                </div>
            ))}

            {moreTile && (
                <div className={columnClassName}>
                    <MoreStatTile {...moreTile} />
                </div>
            )}
        </div>
    );
}
