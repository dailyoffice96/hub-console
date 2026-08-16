import { useRef } from 'react';
import UserDetailModal from '../components/UserDetailModal';
import { USER_STATUS_COLORS } from '../constants/statusColors';
import addMemberImg from '../images/3d.png';
import RowTable from '../components/common/RowTable';
import NumberBadge from '../components/common/NumberBadge';
import StatusBadge from '../components/common/StatusBadge';
import GradientStatGrid from '../components/stats/GradientStatGrid';
import { useUserList, PAGE_SIZE } from '../hooks/useUserList';

// 회원 목록 행의 컬럼 비율 (헤더와 데이터 행이 동일하게 사용)
const USER_ROW_GRID = '0.5fr 1.4fr 1.4fr 1.2fr 2fr 1fr 1fr';

const STAT_GRADIENTS = {
    active: 'linear-gradient(135deg, #6EE7B7 0%, #059669 100%)',
    dormant: 'linear-gradient(135deg, #FDE68A 0%, #D97706 100%)',
    withdrawn: 'linear-gradient(135deg, #FCA5A5 0%, #DC2626 100%)',
    more: 'linear-gradient(135deg, #AEC0EC 0%, #4C5F91 100%)',
};

// 리스트가 눌릴 때 페이지 전체가 아니라 표 안쪽에서만 가로 스크롤되게 하는 최소 너비
const ROW_MIN_WIDTH = '760px';

// GradientStatGrid의 footer 자리에 그대로 넘기는 하단 영역이다.
function StatFooter({ title, note }) {
    return (
        <>
            <div>
                {/* 최소 폰트 크기 14px 기준 준수 */}
                <div className="text-muted" style={{ fontSize: '14px' }}>{note}</div>
                <div className="fw-bold text-dark">{title}</div>
            </div>
            <span className="text-primary fw-semibold small">›</span>
        </>
    );
}

function UserListPage() {
    // 데이터 조회/상태는 전부 useUserList 훅이 들고 있고, 여기선 화면만 그린다.
    const {
        users, name, setName, status, setStatus, page, setPage, loginId, setLoginId,
        states, totalPages, selectedUser,
        handleSearch, handleDownload, handleUpload,
        openUserDetail, closeUserDetail, handleUserUpdated,
    } = useUserList();

    const fileInputRef = useRef(null);

    const onFileSelected = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        handleUpload(file);
    };

    const handleViewMoreStats = () => {
        console.log("전체 통계 보기");
    };

    const totalMembers = states.active + states.dormant + states.withdrawn;

    return (
        // 표(flex-grow-1) 영역만 남는 세로 공간을 채우고, 그 안에서만 스크롤되게 하려고
        // 전체를 세로 flex로 만들었다. 다른 섹션은 원래 크기 그대로 고정.
        <div className="container-fluid px-4 d-flex flex-column" style={{ height: '100%', minHeight: 0 }}>

            <div className="d-flex justify-content-between align-items-end mb-3 flex-shrink-0">
                <h2 className="fw-bold mb-0" style={{ fontSize: '18px' }}>현황</h2>
                <span className="text-muted small">총 {totalMembers}명</span>
            </div>
            <GradientStatGrid
                tiles={[
                    {
                        background: STAT_GRADIENTS.active,
                        tagLabel: 'ACTIVE',
                        value: states.active,
                        unit: '명',
                        footer: <StatFooter title="활동 회원" note="정상 이용 중" />,
                    },
                    {
                        background: STAT_GRADIENTS.dormant,
                        tagLabel: 'DORMANT',
                        value: states.dormant,
                        unit: '명',
                        footer: <StatFooter title="휴면 회원" note="장기 미접속" />,
                    },
                    {
                        background: STAT_GRADIENTS.withdrawn,
                        tagLabel: 'LEAVE',
                        value: states.withdrawn,
                        unit: '명',
                        footer: <StatFooter title="탈퇴 회원" note="서비스 탈퇴" />,
                    },
                ]}
                moreTile={{
                    background: STAT_GRADIENTS.more,
                    image: addMemberImg,
                    onClick: handleViewMoreStats,
                }}
            />

            <div className="d-flex justify-content-between align-items-end mb-3">
                <h2 className="fw-bold mb-0" style={{ fontSize: '18px' }}>회원 목록</h2>
            </div>

            {/* 위쪽 섹션을 뺀 나머지 공간을 표가 다 차지하고, 데이터 행이 넘치면 표 안에서만 스크롤된다. */}
            <div className="flex-grow-1 d-flex flex-column mb-4" style={{ minHeight: 0 }}>
                <RowTable
                    onSearch={handleSearch}
                    filters={
                        <>
                            <input
                                className="form-control rounded-pill border-2"
                                style={{ maxWidth: '200px', height: '42px' }}
                                placeholder="이름 검색"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                            />
                            <input
                                className="form-control rounded-pill border-2"
                                style={{ maxWidth: '200px', height: '42px' }}
                                placeholder="아이디 검색"
                                value={loginId}
                                onChange={(e) => setLoginId(e.target.value)}
                            />
                            <select
                                className="form-select rounded-pill border-2"
                                style={{ width: '150px', height: '42px' }}
                                value={status}
                                onChange={(e) => setStatus(e.target.value)}
                            >
                                <option value="">전체 상태</option>
                                <option value="ACTIVE">활성</option>
                                <option value="DORMANT">휴면</option>
                                <option value="WITHDRAWN">탈퇴</option>
                            </select>
                        </>
                    }
                    rightActions={
                        <div className="d-flex gap-2">
                            <button className="btn btn-outline-success rounded-pill px-3 fw-semibold shadow-sm" onClick={handleDownload}>
                                엑셀 다운
                            </button>
                            <input
                                type="file"
                                ref={fileInputRef}
                                accept=".xlsx"
                                style={{ display: 'none' }}
                                onChange={onFileSelected}
                            />
                            <button className="btn btn-outline-danger rounded-pill px-3 fw-semibold shadow-sm" onClick={() => fileInputRef.current.click()}>
                                엑셀 업로드
                            </button>
                        </div>
                    }
                    headers={['번호', '이름', '아이디', '전화번호', '이메일', '상태', '가입일']}
                    gridTemplateColumns={USER_ROW_GRID}
                    minWidth={ROW_MIN_WIDTH}
                    page={page}
                    totalPages={totalPages}
                    onPageChange={setPage}
                >
                    {users.length > 0 ? (
                        users.map((user, index) => (
                            <div
                                key={user.id}
                                className="d-grid align-items-center bg-white shadow-sm px-3 py-3"
                                style={{ gridTemplateColumns: USER_ROW_GRID, columnGap: '10px', borderRadius: '18px', cursor: 'pointer' }}
                                onClick={() => openUserDetail(user.id)}
                            >
                                <span>
                                    <NumberBadge number={page * PAGE_SIZE + index + 1} />
                                </span>
                                <span className="fw-semibold">{user.maskedName}</span>
                                <span className="text-truncate">{user.loginId}</span>
                                <span className="text-truncate">{user.maskedPhone}</span>
                                <span className="text-truncate">{user.maskedEmail}</span>
                                <span>
                                    <StatusBadge color={USER_STATUS_COLORS[user.status]}>{user.status}</StatusBadge>
                                </span>
                                <span className="small">{user.createdAt}</span>
                            </div>
                        ))
                    ) : (
                        <div className="text-center py-5 text-muted bg-white" style={{ borderRadius: '18px' }}>
                            등록된 회원이 없습니다.
                        </div>
                    )}
                </RowTable>
            </div>

            {selectedUser && (
                <UserDetailModal
                    user={selectedUser}
                    onClose={closeUserDetail}
                    onUpdated={handleUserUpdated}
                />
            )}
        </div>
    );
}

export default UserListPage;
