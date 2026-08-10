import {useLocation, useNavigate} from "react-router-dom";
import { logout } from '../api/authApi';
import {useState, useEffect} from "react";
import axiosInstance from '../api/axiosInstance';
import { LuBell } from "react-icons/lu";
import { getIncidents } from "../api/incidentApi";



function Header() {
    const location = useLocation();
    const navigate = useNavigate();
    const [myInfo, setMyInfo] = useState("");
    const [urgentCount, setUrgentCount] = useState(0);


    const pageTitles = {
        '/users': '회원관리',
        '/admins': '관리자 목록',
        '/inquiries': '문의사항',
        '/incidents': '장애사항',
        '/auditLog': 'Audit Log',
    };

    const title = pageTitles[location.pathname] || 'SM Console';

    const handleLogout = async() => {
     await logout();
     navigate('/');
    }

    //화면이 맨 처음 만들어질 때 딱 한 번, 서버의 /api/me라는 주소로 조회 요청을 보내고,
    //응답이 오면 그 데이터를 myInfo에 저장해라
    useEffect(() => {
        axiosInstance.get('/api/me').then(res => setMyInfo(res.data));
    }, []);


     useEffect(() => {
        getIncidents({ page: 0, size: 100 }).then(res => {
            const today = new Date();
            const urgent = res.data.content.filter(incident => {
                if (incident.status === 'RESOLVED') return false;
                const dueDate = new Date(incident.slaDueAt);
                const hoursLeft = (dueDate - today) / (1000 * 60 * 60);
                return hoursLeft <= 24;
            });
            setUrgentCount(urgent.length);
        });
     }, []);


    return(
    <div className="p-3 d-flex justify-content-between align-items-center">
       <h4 className="mb-0" style={{ fontFamily: 'Paperlogy' }}>{title}</h4>

       <div className="d-flex align-items-center">

       <div
           onClick={() => navigate('/incidents')}
           className="me-3"
           style={{ position: 'relative', display: 'inline-block', cursor: 'pointer' }}
           title={`SLA 임박 장애 ${urgentCount}건`}>

           <LuBell size={22} />
           {urgentCount > 0 && (
               <span
                   className="badge bg-danger rounded-circle"
                   style={{
                       position: 'absolute',
                       top: '-6px',
                       right: '-6px',
                       fontSize: '10px',
                       padding: '3px 6px'
                   }}>
                   {urgentCount}
               </span>
           )}
       </div>
       <span className="me-3">{myInfo?.name} · {myInfo?.role}</span>
       <button className="btn btn-outline-secondary btn-sm" onClick={handleLogout}>로그아웃</button>
       </div>

    </div>
    )
}

export default Header;
