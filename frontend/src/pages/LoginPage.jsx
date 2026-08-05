import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/authapi";
import { getSystemSetting } from "../api/systemSettingApi";
import { Container, Carousel } from "react-bootstrap";
import image1 from "../images/11.jpg";
import image2 from "../images/22.jpg";
import image3 from "../images/33.jpg";

function LoginPage() {
    const [loginId, setLoginId] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [systemSetting, setSystemSetting] = useState(null);
    const [showMaintenancePopup, setShowMaintenancePopup] = useState(true);
    const [isExpanded, setIsExpanded] = useState(false);

    const navigate = useNavigate();

    useEffect(() => {
        getSystemSetting().then(res => {
            setSystemSetting(res.data);
        });
    }, []);

    const isUnderMaintenance = () => {
        if (!systemSetting) return false;

        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const today = `${year}-${month}-${day}`;

        return today >= systemSetting.startAt && today <= systemSetting.endAt;
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        console.log('로그인 시도중입니다.');

        try {
            await login(loginId, password);
            navigate("/users");
        }
        catch (error) {
            if (error.response?.status === 401) {
                const failCount = error.response.data?.failCount;
                if (failCount >= 5) {
                    setError("로그인 5회 실패로 계정이 잠겼습니다. 관리자에게 문의하세요.");
                } else if (failCount) {
                    setError(`아이디 또는 비밀번호가 틀렸습니다.\n(${failCount}회 실패, 5회 실패 시 계정이 잠깁니다)`);
                } else {
                    setError("아이디 또는 비밀번호가 틀렸습니다.");
                }
            } else if (error.response?.status === 403) {
                setError(error.response.data?.message || "현재 시스템 점검 중이라서 로그인이 불가합니다.");
            }
        }
    };

    return (
        <div style={{ position: 'relative', minHeight: '100vh' }}>
            {/* 배경: 캐러셀이 화면 전체를 채움 */}
            <div style={{ position: 'absolute', inset: 0, zIndex: 0 }}>
                <Carousel indicators={true} controls={true} interval={4000}>
                    <Carousel.Item>
                        <img
                            className="d-block w-100"
                            src={image1}
                            alt="1번째 이미지"
                            style={{ height: '100vh', objectFit: 'cover', objectPosition: 'center top' }}
                        />
                    </Carousel.Item>
                    <Carousel.Item>
                        <img
                            className="d-block w-100"
                            src={image2}
                            alt="2번째 이미지"
                            style={{ height: '100vh', objectFit: 'cover', objectPosition: 'center top' }}
                        />
                    </Carousel.Item>
                    <Carousel.Item>
                        <img
                            className="d-block w-100"
                            src={image3}
                            alt="3번째 이미지"
                            style={{ height: '100vh', objectFit: 'cover', objectPosition: 'center top' }}
                        />
                    </Carousel.Item>
                </Carousel>
            </div>

            {/* 파스텔 그라데이션 오버레이 */}
            <div style={{
                position: 'absolute',
                inset: 0,
                background: 'linear-gradient(180deg, rgba(173,216,255,0.3) 0%, rgba(255,240,230,0.15) 100%)',
                zIndex: 1,
            }} />

            {/* 로그인 폼: 배경 위에 떠 있음, 오른쪽 끝 정렬 */}
            <div style={{
                position: 'relative',
                zIndex: 2,
                display: 'flex',
                alignItems: 'stretch',
                justifyContent: 'flex-end',
                height: '100vh'
            }}>

                {/* 화살표 버튼 - 폼 너비와 연동되어 같이 움직임 */}
                <button
                    onClick={() => setIsExpanded(!isExpanded)}
                    style={{
                        position: 'absolute',
                        top: '50%',
                        right: isExpanded ? '82%' : '33rem',
                        transform: 'translateY(-50%)',
                        width: '32px',
                        height: '56px',
                        borderTopLeftRadius: '28px',
                        borderBottomLeftRadius: '28px',
                        borderTopRightRadius: '4px',
                        borderBottomRightRadius: '4px',
                        backgroundColor: 'rgba(255, 255, 255, 0.95)',
                        border: 'none',
                        color: '#1B2A4A',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '16px',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
                        cursor: 'pointer',
                        zIndex: 3,
                        transition: 'right 0.3s ease',
                    }}
                >
                    {isExpanded ? '›' : '‹'}
                </button>

                {/* 로그인 폼 */}
                <div
                    className="shadow rounded-4 p-4 d-flex flex-column justify-content-center"
                    style={{
                        width: '100%',
                        maxWidth: isExpanded ? '82%' : '33rem',
                        transition: 'max-width 0.3s ease',
                        padding: '0',
                        backgroundColor: 'rgba(255, 255, 255, 0.95)'
                    }}>
                    <h2>Login Page</h2>
                    <p className="text-muted">내부 직원 및 관리자 전용 관리 콘솔</p>
                    <form onSubmit={handleLogin}>
                        <label className="form-label">아이디</label>
                        <input
                            className="form-control mb-3"
                            type="text"
                            placeholder="아이디를 입력해 주세요"
                            value={loginId}
                            onChange={(e) => setLoginId(e.target.value)} />

                        <label className="form-label">비밀번호</label>
                        <input
                            className="form-control mb-3"
                            type="password"
                            placeholder="비밀번호를 입력해 주세요"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)} />

                        {error && <p className="text-danger" style={{ whiteSpace: 'pre-line' }}>{error}</p>}
                        <button type="submit" className="btn btn-primary w-100 mt-2">로그인</button>
                    </form>
                </div>
            </div>

            {/* ✅ 점검 팝업 - 최상위 레벨에 독립적으로 위치 */}
            {isUnderMaintenance() && showMaintenancePopup && (
                <div style={{
                    position: 'fixed',
                    top: '100px',
                    left: '100px',
                    width: '360px',
                    zIndex: 1000,
                }}>
                    <div className="card border-0 shadow" style={{ borderRadius: '16px' }}>
                        <div className="card-body p-4 text-center">
                            <div className="d-flex justify-content-end mb-2">
                                <button
                                    type="button"
                                    className="btn-close"
                                    onClick={() => setShowMaintenancePopup(false)}></button>
                            </div>

                            <div style={{ fontSize: '48px' }}>⚠️</div>
                            <h4 className="fw-bold mt-2">지금은 시스템 점검 중입니다.</h4>
                            <p className="text-muted small mb-3">
                                안정적인 서비스 제공을 위해 아래와 같이 점검 작업을 진행 중입니다.
                            </p>
                            <div className="bg-light rounded p-3 text-start mb-3">
                                <p className="mb-1 small"><strong>■ 점검 일시:</strong> {systemSetting.startAt} ~ {systemSetting.endAt}</p>
                                <p className="mb-0 small"><strong>■ 점검 영향:</strong> {systemSetting.message}</p>
                            </div>
                            <p className="text-muted small mb-0">
                                점검 시간은 작업 상황에 따라 조정될 수 있습니다.<br />
                                이용에 불편을 드려 죄송합니다.
                            </p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default LoginPage;