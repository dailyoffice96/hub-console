import {useState} from "react"
import {useNavigate} from "react-router-dom"
import {login} from "../api/authapi"

function LoginPage() {
    const [loginId, setLoginId] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const navigate = useNavigate();

    const handleLogin = async(e) => {
        e.preventDefault(); //새로고침 방지
        console.log('로그인 시도중입니다.');

        try{
            await login(loginId, password)
            navigate("/users");
        }
        catch(error){
            if(error.response?.status === 401){setError("로그인 실패");}
            else{
                setError("로그인 중 오류가 발생했습니다.");
                }
        }
    }


  return (
    <div className="container-fluid vh-100">
      <div className="row h-100">

        {/* 왼쪽: 이미지 영역 (8칸) */}
        <div className="col-8 d-flex align-items-center justify-content-center">
            {/* 이미지 또는 캐러셀 */}
        </div>

        {/* 오른쪽: 로그인 폼 영역 (4칸) */}
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
                    onChange={(e) => setLoginId(e.target.value)}/>

                <label className="form-label">비밀번호</label>
                <input
                    className="form-control mb-3"
                    type="password"
                    placeholder="비밀번호를 입력해 주세요"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}/>

                {/* 로그인 실패 시 에러 메시지 표시 */}
                {error && <p className="text-danger">{error}</p>}

                <button type="submit" className="btn btn-primary w-100 mt-2">로그인</button>
            </form>
            </div>
        </div>

      </div>
    </div>
  );
}

export default LoginPage;
