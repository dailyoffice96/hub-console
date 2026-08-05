import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/authapi";
import { getSystemSetting } from "../api/systemSettingApi";
import image1 from "../images/11.png";
import image2 from "../images/22.png";
import image3 from "../images/33.png";
import "../css/Login.css";

const brandSlides = [
    { image: image1, title: "회원 관리", desc: "회원 정보를 안전하게 관리합니다" },
    { image: image2, title: "문의 대응", desc: "신속한 CS 대응 시스템을 제공합니다" },
    { image: image3, title: "장애 관리", desc: "장애를 빠르게 추적하고 해결합니다" },
];
function LoginPage() {
    const [loginId, setLoginId] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [systemSetting, setSystemSetting] = useState(null);
    const [showMaintenancePopup, setShowMaintenancePopup] = useState(true);
    const [imgIndex, setImgIndex] = useState(0);

    const navigate = useNavigate();

    useEffect(() => {
        getSystemSetting().then(res => {
            setSystemSetting(res.data);
        });
    }, []);

    useEffect(() => {
        const timer = setInterval(() => {
            setImgIndex(prev => (prev + 1) % brandSlides.length);
        }, 4000);
        return () => clearInterval(timer);
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
        <div className="fullpage-bg">
            <div className="brand-overlay">
                <div className="logo">⬡ SM <span>Console</span></div>
                 <h2 className="brand-title">{brandSlides[imgIndex].title}</h2>
                <hr/>
                 <p className="mb-3"  style={{ fontSize: '24px' }}>{brandSlides[imgIndex].desc}</p>
                <p className="mt-3">
                    SM Console 운영팀
                    <br/>
                    support@smconsole.io
                </p>
            </div>

            {/* 중앙 이미지 슬라이더 */}
            <div className="brand-slider">
                <img src={brandSlides[imgIndex].image} alt="" />
            </div>

            <div className="login-card-wrapper">
                <div className="login-box">
                    <h2 className="fw-bold">⬡ SM <span>Console</span></h2>
                    <p className="text-secondary mb-4">내부 직원 및 관리자 전용 관리 콘솔</p>

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

                    <small className="copyright d-block text-center">© 2026 SM Console. All rights reserved.</small>
                </div>
            </div>

            {isUnderMaintenance() && showMaintenancePopup && (
                <div style={{ position: 'fixed', top: '100px', left: '100px', width: '360px', zIndex: 1000 }}>
                    <div className="card border-0 shadow" style={{ borderRadius: '16px' }}>
                        <div className="card-body p-4 text-center">
                            <div className="d-flex justify-content-end mb-2">
                                <button type="button" className="btn-close" onClick={() => setShowMaintenancePopup(false)}></button>
                            </div>
                            <div style={{ fontSize: '48px' }}>⚠️</div>
                            <h4 className="fw-bold mt-2">지금은 시스템 점검 중입니다.</h4>
                            <p className="text-muted small mb-3">안정적인 서비스 제공을 위해 아래와 같이 점검 작업을 진행 중입니다.</p>
                            <div className="bg-light rounded p-3 text-start mb-3">
                                <p className="mb-1 small"><strong>■ 점검 일시:</strong> {systemSetting.startAt} ~ {systemSetting.endAt}</p>
                                <p className="mb-0 small"><strong>■ 점검 영향:</strong> {systemSetting.message}</p>
                            </div>
                            <p className="text-muted small mb-0">점검 시간은 작업 상황에 따라 조정될 수 있습니다.<br />이용에 불편을 드려 죄송합니다.</p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default LoginPage;