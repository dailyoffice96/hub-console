import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/authapi";
import { getSystemSetting } from "../api/systemSettingApi";

function LoginPage() {
    const [loginId, setLoginId] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [systemSetting, setSystemSetting] = useState(null);

    const navigate = useNavigate();

    // ✅ 컴포넌트 최상위로 이동
    useEffect(() => {
        getSystemSetting().then(res => {
            setSystemSetting(res.data);
        });
    }, []);

    // ✅ 이것도 최상위로 이동
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
        <div className="container-fluid vh-100">
            <div className="row h-100">

                {/* 왼쪽: 이미지 영역 */}
                <div className="col-8 d-flex align-items-center justify-content-center">
                    {/* 이미지 또는 캐러셀 */}
                </div>

                {/* 오른쪽: 로그인 폼 영역 */}
                <div className="col-4 d-flex align-items-center justify-content-center">
                    <div className="w-100" style={{ maxWidth: '300px' }}>
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

            </div>

            {/* ✅ 점검 팝업 - 최상위 레벨에 독립적으로 위치 */}
            {isUnderMaintenance() && (
                <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.6)' }}>
                    <div className="modal-dialog modal-dialog-centered">
                        <div className="modal-content border-0 shadow p-4 text-center" style={{ borderRadius: '16px' }}>
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