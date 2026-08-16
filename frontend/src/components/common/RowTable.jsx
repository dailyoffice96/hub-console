import { SOFT_SHADOW, SEARCH_BTN_COLOR, TABLE_HEADER_TEXT, TABLE_TEXT_COLOR } from '../../constants/designTokens';

// 목록 페이지 공통 컴포넌트. 검색 영역 + 표 헤더 + 데이터 행 + 페이징을 카드 한 장으로 묶는다.
// 세로 스크롤은 데이터 행 부분에서만 일어나도록 만들어져 있어서, 사용하는 쪽에서 이 컴포넌트를
// `flex: 1 1 auto; min-height: 0;`인 요소로 감싸야 실제로 동작한다 (안 감싸면 그냥 늘어난다).
export default function RowTable({
    filters,
    onSearch,
    rightActions,
    headers,
    gridTemplateColumns,
    minWidth,
    page,
    totalPages,
    onPageChange,
    children
}) {
    return (
        <div
            className="card border-0 rounded-4 overflow-hidden d-flex flex-column"
            style={{ boxShadow: SOFT_SHADOW, height: '100%', minHeight: 0 }}
        >

            {/* 검색 영역 - 표와 한 카드에 들어있고 구분선 하나로만 나뉜다 */}
            {(filters || onSearch || rightActions) && (
                <div className="p-4 border-bottom flex-shrink-0" style={{ background: '#FAFAFD' }}>
                    <div className="d-flex flex-wrap justify-content-between align-items-center gap-3">
                        <div className="d-flex flex-wrap gap-2 align-items-center">
                            {filters}

                            {onSearch && (
                                <button
                                    className="btn rounded-pill px-4 fw-semibold shadow-sm text-white"
                                    style={{ height: '42px', background: SEARCH_BTN_COLOR, border: 'none' }}
                                    onClick={onSearch}
                                >
                                    검색
                                </button>
                            )}
                        </div>

                        {rightActions && (
                            <div className="d-flex gap-2">
                                {rightActions}
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* 표 영역 - 가로 스크롤은 여기서, 세로 스크롤은 안쪽 데이터 행 부분에서만 일어난다 */}
            <div className="p-3 d-flex flex-column" style={{ background: '#FAFAFD', flex: '1 1 auto', minHeight: 0 }}>
                <div
                    style={{
                        overflowX: 'auto',
                        overflowY: 'hidden',
                        flex: '1 1 auto',
                        minHeight: 0,
                        display: 'flex',
                        flexDirection: 'column',
                    }}
                >
                    <div style={{ minWidth: minWidth, flex: '1 1 auto', minHeight: 0, display: 'flex', flexDirection: 'column' }}>
                        <div
                            className="d-none d-md-grid text-uppercase fw-bold px-3 pb-2 flex-shrink-0"
                            style={{
                                gridTemplateColumns: gridTemplateColumns,
                                columnGap: '10px',
                                letterSpacing: '0.04em',
                                color: TABLE_HEADER_TEXT,
                                fontSize: '14px',
                            }}
                        >
                            {headers.map((header, index) => (
                                <span key={index} className={header.className || ''}>
                                    {header.label || header}
                                </span>
                            ))}
                        </div>

                        {/* 헤더는 이 div 바깥이라 고정되고, 행이 많아지면 여기 안에서만 스크롤된다.
                            (상태 배지처럼 색이 의미 있는 부분은 자기 style의 color가 우선 적용됨) */}
                        <div
                            className="d-flex flex-column gap-2"
                            style={{ color: TABLE_TEXT_COLOR, flex: '1 1 auto', minHeight: 0, overflowY: 'auto' }}
                        >
                            {children}
                        </div>
                    </div>
                </div>
            </div>

            {/* 페이징 영역 */}
            <div className="d-flex flex-wrap justify-content-between align-items-center p-2 border-top bg-light bg-opacity-50 flex-shrink-0">
                <div className="d-flex justify-content-center align-items-center mx-auto w-100">
                    <button
                        className="btn btn-white border shadow-sm px-3 me-2 rounded-pill bg-white"
                        disabled={page === 0}
                        onClick={() => onPageChange(page - 1)}
                    >
                        이전
                    </button>
                    <span className="text-secondary small fw-bold mx-3">
                        {page + 1} / {totalPages || 1} 페이지
                    </span>
                    <button
                        className="btn btn-white border shadow-sm px-3 ms-2 rounded-pill bg-white"
                        disabled={page >= totalPages - 1 || totalPages === 0}
                        onClick={() => onPageChange(page + 1)}
                    >
                        다음
                    </button>
                </div>
            </div>
        </div>
    );
}
