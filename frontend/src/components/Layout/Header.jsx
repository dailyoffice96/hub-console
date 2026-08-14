import {useLocation, useNavigate} from "react-router-dom";
import { logout } from '../../api/authApi';
import {useState, useEffect} from "react";
import axiosInstance from '../../api/axiosInstance';
import { LuBell } from "react-icons/lu";
import { getIncidents } from "../../api/incidentApi";
import ChangePasswordModal from '../ChangePasswordModal';

function Header() {
    const location = useLocation();
    const navigate = useNavigate();
    const [myInfo, setMyInfo] = useState("");
    const [urgentCount, setUrgentCount] = useState(0);
    const [showPasswordModal, setShowPasswordModal] = useState(false);

    const pageTitles = {
        '/users': '회원관리',
        '/admins': '관리자 목록',
        '/inquiries': '문의사항',
        '/incidents': '장애사항',
        '/incidents/monitoring': '장애 모니터링',
        '/auditLog': 'Audit Log',
        '/auditLog/analyze': '이상현상 분석'
    };

    const title = pageTitles[location.pathname] || 'SM Console';

    const handleLogout = async() => {
     await logout();
     navigate('/');
    }

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
    <div className="p-3 d-flex justify-content-between align-items-center border-bottom" style={{ boxShadow: '0 2px 4px rgba(0, 0, 0, 0.02)' }}>
       <h2 className="mb-0" style={{ fontFamily: 'Paperlogy' }}>{title}</h2>

       <div className="d-flex align-items-center">
         <div
             onClick={() => navigate('/incidents')}
             className="me-3"
             style={{ position: 'relative', display: 'inline-block', cursor: 'pointer' }}
             title={`SLA 임박 장애 ${urgentCount}건`}
         >
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
                     }}
                 >
                     {urgentCount}
                 </span>
             )}
         </div>
         <span className="me-3">{myInfo?.name} · {myInfo?.role}</span>
         <button className="btn btn-outline-secondary btn-sm me-2" onClick={() => setShowPasswordModal(true)}>비밀번호 변경</button>
         <button className="btn btn-outline-secondary btn-sm" onClick={handleLogout}>로그아웃</button>
       </div>

       {showPasswordModal && (
           <ChangePasswordModal onClose={() => setShowPasswordModal(false)} />
       )}
    </div>
    )
}

export default Header;