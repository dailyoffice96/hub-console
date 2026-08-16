import { useState } from 'react';
import SystemModal from '../components/SystemModal';
import AdminCreateModal from '../components/AdminCreateModal';
import RowTable from '../components/common/RowTable';
import NumberBadge from '../components/common/NumberBadge';
import StatusBadge from '../components/common/StatusBadge';
import GradientStatGrid from '../components/stats/GradientStatGrid';
import { TABLE_TEXT_COLOR } from '../constants/designTokens';
import addMemberImg from '../images/3d.png';
import { useAdminList, PAGE_SIZE } from '../hooks/useAdminList';

// 관리자 목록 행의 컬럼 비율 (헤더와 데이터 행이 동일하게 사용)
const ADMIN_ROW_GRID = '0.5fr 1.2fr 1.4fr 1fr 1.2fr 1.2fr 1fr';

// "관리자 추가" 버튼은 구분되는 파란색으로
const ADD_BTN_COLOR = '#2563EB';

// 원래 색상(primary=전체, danger=잠김, warning=대표)에 맞춘 그라데이션 + "더보기" 카드용 남색
const STAT_GRADIENTS = {
    all: 'linear-gradient(135deg, #93C5FD 0%, #2563EB 100%)',      // 원래 bg-primary/text-primary
    locked: 'linear-gradient(135deg, #FCA5A5 0%, #DC2626 100%)',   // 원래 bg-danger/text-danger
    super: 'linear-gradient(135deg, #FDE68A 0%, #D97706 100%)',    // 원래 bg-warning/text-warning
    more: 'linear-gradient(135deg, #AEC0EC 0%, #4C5F91 100%)',
};

// 잠금 상태 배지 색상 (부드러운 파스텔 톤)
const LOCK_STATUS_COLORS = {
    locked: { bg: '#FEE2E2', text: '#B91C1C' },
    normal: { bg: '#DCFCE7', text: '#166534' },
};

// 리스트가 눌릴 때 페이지 전체가 아니라 표 안쪽에서만 가로 스크롤되게 하는 최소 너비
const ROW_MIN_WIDTH = '760px';

// 통계 카드 하단 흰색 영역 (노트 텍스트 + 제목 + 화살표) — GradientStatGrid의 footer로 그대로 넘김
function StatFooter({ title, note }) {
    return (
        <>
            <div>
                <div className="text-muted" style={{ fontSize: '14px' }}>{note}</div>
                <div className="fw-bold text-dark">{title}</div>
            </div>
            <span className="text-primary fw-semibold small">›</span>
        </>
    );
}

function AdminListPage() {
    // 데이터 조회/상태는 전부 useAdminList 훅이 들고 있고, 여기선 화면만 그린다.
    const {
        admins, name, setName, role, setRole, myRole, page, setPage, totalPages, stats,
        fetchAdmins, fetchStats, handleSearch, handleUnlock, handleDelete,
    } = useAdminList();

    const [showSystemModal, setShowSystemModal] = useState(false);
    const [showCreateModal, setShowCreateModal] = useState(false);

    // 통계 "더보기" 클릭 — 필요에 맞게 라우팅/모달 오픈 등으로 교체하세요.
    const handleViewMoreStats = () => {
        console.log("전체 통계 보기");
    };

    return (
        // d-flex flex-column + height:100% — Layout이 준 세로 공간을 그대로 받아서, 아래 표
        // 영역(flex-grow-1)만 남는 공간을 채우게 한다. 그 외 섹션은 원래 크기 그대로 고정.
        <div className="container-fluid px-4 d-flex flex-column" style={{ height: '100%', minHeight: 0 }}>

            <div className="d-flex justify-content-between align-items-end mb-3">
                <h2 className="fw-bold mb-0" style={{ fontSize: '18px' }}>현황</h2>
                <span className="text-muted small">총 {stats.totalCount}명</span>
            </div>
            <GradientStatGrid
                tiles={[
                    {
                        background: STAT_GRADIENTS.all,
                        tagLabel: 'ALL',
                        value: stats.totalCount,
                        unit: '명',
                        footer: <StatFooter title="전체 관리자" note="등록된 계정" />,
                    },
                    {
                        background: STAT_GRADIENTS.locked,
                        tagLabel: 'LOCK',
                        value: stats.lockedCount,
                        unit: '명',
                        footer: <StatFooter title="잠긴 계정" note="로그인 잠금" />,
                    },
                    {
                        background: STAT_GRADIENTS.super,
                        tagLabel: 'SUPER',
                        value: stats.superAdminCount,
                        unit: '명',
                        footer: <StatFooter title="대표 관리자" note="최상위 권한" />,
                    },
                ]}
                moreTile={{
                    background: STAT_GRADIENTS.more,
                    image: addMemberImg,
                    onClick: handleViewMoreStats,
                }}
            />

            <div className="d-flex justify-content-between align-items-end mb-3">
                <h2 className="fw-bold mb-0" style={{ fontSize: '18px' }}>관리자 목록</h2>
            </div>

            {/* 공통 RowTable 적용 (검색 영역 + 헤더 + 데이터 행 + 페이징을 카드 하나로 통합)
                flex-grow-1 + minHeight:0 — 위쪽 섹션들을 뺀 나머지 세로 공간을 표가 전부 차지하고,
                그 안에서만(데이터 행 부분) 세로 스크롤이 생기게 한다. */}
            <div className="flex-grow-1 d-flex flex-column mb-4" style={{ minHeight: 0 }}>
            <RowTable
                onSearch={handleSearch}
                filters={
                    <>
                        <input
                            className="form-control rounded-pill border-2"
                            style={{ maxWidth: '220px', height: '42px' }}
                            placeholder="이름 검색"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                        />
                        <select
                            className="form-select rounded-pill border-2"
                            style={{ width: '150px', height: '42px' }}
                            value={role}
                            onChange={(e) => setRole(e.target.value)}
                        >
                            <option value="">전체 직급</option>
                            <option value="SUPER_ADMIN">대표</option>
                            <option value="ADMIN">팀장</option>
                            <option value="STAFF">직원</option>
                        </select>
                    </>
                }
                rightActions={
                    myRole === 'SUPER_ADMIN' && (
                        <>
                            <button
                                className="btn rounded-pill px-4 fw-semibold shadow-sm text-white"
                                style={{ height: '42px', background: ADD_BTN_COLOR, border: 'none' }}
                                onClick={() => setShowCreateModal(true)}
                            >
                                관리자 추가
                            </button>
                            <button
                                className="btn btn-outline-danger rounded-pill px-4 fw-semibold shadow-sm"
                                style={{ height: '42px' }}
                                onClick={() => setShowSystemModal(true)}
                            >
                                시스템 점검 설정
                            </button>
                        </>
                    )
                }
                headers={['번호', '이름', '아이디', '직급', '잠금여부', '입사일', { label: '관리', className: 'text-end' }]}
                gridTemplateColumns={ADMIN_ROW_GRID}
                minWidth={ROW_MIN_WIDTH}
                page={page}
                totalPages={totalPages}
                onPageChange={setPage}
            >
                {admins.length > 0 ? (
                    admins.map((admin, index) => (
                        <div
                            key={admin.id}
                            className="d-grid align-items-center bg-white shadow-sm px-3 py-3"
                            style={{ gridTemplateColumns: ADMIN_ROW_GRID, columnGap: '10px', borderRadius: '18px' }}
                        >
                            <span>
                                <NumberBadge number={page * PAGE_SIZE + index + 1} />
                            </span>
                            <span className="fw-semibold">{admin.name}</span>
                            <span className="text-truncate">{admin.loginId}</span>
                            <span>
                                {/* Bootstrap .badge 기본 글자색이 흰색이라 bg-light 위에서 안 보였음 — 명시적으로 색 지정 */}
                                <span className="badge rounded-pill bg-light border px-3 py-2 fw-semibold" style={{ color: TABLE_TEXT_COLOR }}>
                                    {admin.role}
                                </span>
                            </span>
                            <span>
                                {admin.isLocked ? (
                                    <StatusBadge
                                        color={LOCK_STATUS_COLORS.locked}
                                        onClick={() => handleUnlock(admin.id)}
                                        title="클릭하면 잠금 해제"
                                    >
                                        🔒 잠김 (해제)
                                    </StatusBadge>
                                ) : (
                                    <StatusBadge color={LOCK_STATUS_COLORS.normal}>정상</StatusBadge>
                                )}
                            </span>
                            <span className="small">{admin.createdAt}</span>
                            <span className="text-end">
                                {myRole === 'SUPER_ADMIN' && (
                                    <button
                                        className="btn btn-sm btn-outline-danger px-3 rounded-pill"
                                        onClick={() => handleDelete(admin.id)}
                                    >
                                        탈퇴
                                    </button>
                                )}
                            </span>
                        </div>
                    ))
                ) : (
                    <div className="text-center py-5 text-muted bg-white" style={{ borderRadius: '18px' }}>
                        검색된 관리자가 없습니다.
                    </div>
                )}
            </RowTable>
            </div>

            {showSystemModal && (
                <SystemModal onClose={() => setShowSystemModal(false)} />
            )}

            {showCreateModal && (
                <AdminCreateModal
                    onClose={() => setShowCreateModal(false)}
                    onCreated={() => { fetchAdmins(); fetchStats(); }}
                />
            )}
        </div>
    );
}

export default AdminListPage;
