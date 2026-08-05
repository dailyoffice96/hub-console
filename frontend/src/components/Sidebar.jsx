import { Link, useLocation } from "react-router-dom";
import {
    LuUsers,
    LuShieldCheck,
    LuMessageSquare,
    LuTriangleAlert,
    LuFileText,
} from "react-icons/lu";
import sidebarBg from "../images/sidebar-bg.png";

function Sidebar() {
    const location = useLocation();

    const menus = [
        { path: "/users", label: "회원관리", icon: <LuUsers size={20} /> },
        { path: "/admins", label: "관리자목록", icon: <LuShieldCheck size={20} /> },
        { path: "/inquiries", label: "문의사항", icon: <LuMessageSquare size={20} /> },
        { path: "/incidents", label: "장애목록", icon: <LuTriangleAlert size={20} /> },
        { path: "/auditLog", label: "감사로그", icon: <LuFileText size={20} /> },
    ];

    return (
        <div
            className="vh-100 p-3 w-100 d-flex flex-column"
            style={{
                maxWidth: '25rem',
                backgroundImage: `url(${sidebarBg})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
            }}
        >
            <Link to="/" className="text-decoration-none text-center d-block">
                <h5 className="mb-5 text-white fs-3 fw-bold">⬡ SM Console</h5>
            </Link>

            <ul className="nav flex-column gap-2">
                {menus.map((menu) => (
                    <li className="nav-item" key={menu.path}>
                        <Link
                            className={`nav-link text-white fs-5 py-3 px-3 d-flex align-items-center gap-2 ${location.pathname === menu.path ? 'bg-primary rounded' : ''}`}
                            to={menu.path}
                        >
                            {menu.icon}
                            {menu.label}
                        </Link>
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default Sidebar;