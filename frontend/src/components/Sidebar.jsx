import { Link, useLocation } from "react-router-dom";
import { useState } from "react";
import {
    LuUsers,
    LuShieldCheck,
    LuMessageSquare,
    LuTriangleAlert,
    LuFileText,
    LuChevronDown,
    LuChevronRight,
    LuActivity,
    LuBrain,
} from "react-icons/lu";
import sidebarBg from "../images/sidebar-bg.jpg";

function Sidebar() {
    const location = useLocation();
    const [openMenu, setOpenMenu] = useState(null);

    const toggleMenu = (key) => {
        setOpenMenu(openMenu === key ? null : key);
    };

    const simpleMenus = [
        { path: "/users", label: "회원관리", icon: <LuUsers size={20} /> },
        { path: "/admins", label: "관리자목록", icon: <LuShieldCheck size={20} /> },
        { path: "/inquiries", label: "문의사항", icon: <LuMessageSquare size={20} /> },
    ];

    const groupMenus = [
        {
            key: "incident",
            label: "장애관리",
            icon: <LuTriangleAlert size={20} />,
            children: [
                { path: "/incidents", label: "장애목록", icon: <LuFileText size={18} /> },
                { path: "/incidents/monitoring", label: "장애 모니터링", icon: <LuActivity size={18} /> },
            ],
        },
        {
            key: "auditLog",
            label: "감사로그",
            icon: <LuFileText size={20} />,
            children: [
                { path: "/auditLog", label: "감사로그 목록", icon: <LuFileText size={18} /> },
                { path: "/auditLog/analyze", label: "이상행위 분석", icon: <LuBrain size={18} /> },
            ],
        },
    ];

    const isGroupActive = (group) =>
        group.children.some((child) => location.pathname === child.path);

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
                <h5 className="mb-5 text-white fs-3 fw-bold">⬡ OpsHub</h5>
            </Link>

            <ul className="nav flex-column gap-2">
                {/* 단일 메뉴들 */}
                {simpleMenus.map((menu) => (
                    <li className="nav-item" key={menu.path}>
                        <Link
                            className={`nav-link text-white fs-5 py-3 px-3 d-flex align-items-center justify-content-between gap-2 ${location.pathname === menu.path ? 'bg-primary rounded' : ''}`}
                            to={menu.path}
                        >
                            <span className="d-flex align-items-center gap-2">
                                {menu.icon}
                                {menu.label}
                            </span>
                            <LuChevronRight size={16} style={{ opacity: 0.5 }} />
                        </Link>
                    </li>
                ))}

                {/* 그룹 메뉴들 (하위메뉴 있음) */}
                {groupMenus.map((group) => {
                    const isOpen = openMenu === group.key || isGroupActive(group);
                    return (
                        <li className="nav-item" key={group.key}>
                            {/* 그룹 헤더 - 색 변화 없이 항상 흰 텍스트 */}
                            <div
                                onClick={() => toggleMenu(group.key)}
                                className="nav-link text-white fs-5 py-3 px-3 d-flex align-items-center justify-content-between gap-2"
                                style={{ cursor: 'pointer' }}
                            >
                                <span className="d-flex align-items-center gap-2">
                                    {group.icon}
                                    {group.label}
                                </span>
                                {isOpen ? <LuChevronDown size={16} /> : <LuChevronRight size={16} />}
                            </div>

                            {/* 하위 메뉴 영역 */}
                            {isOpen && (
                                <ul
                                    className="nav flex-column mt-1 gap-1 py-2"
                                    style={{
                                        backgroundColor: 'rgba(255, 255, 255, 0.08)',
                                        borderRadius: '10px',
                                    }}
                                >
                                    {group.children.map((child) => (
                                        <li className="nav-item" key={child.path}>
                                            <Link
                                                className={`nav-link text-white fs-6 py-2 px-3 ms-2 d-flex align-items-center gap-2 ${location.pathname === child.path ? 'bg-primary rounded' : ''}`}
                                                to={child.path}
                                            >
                                                {child.icon}
                                                {child.label}
                                            </Link>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </li>
                    );
                })}
            </ul>
        </div>
    );
}

export default Sidebar;