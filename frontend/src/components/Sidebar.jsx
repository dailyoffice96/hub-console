import {Link} from "react-router-dom"


function Sidebar() {
  return (
    <div className="vh-100 p-3" style={{ maxWidth: '220px' }}>
      <h5 className="mb-4">SM Console</h5>
      <ul className="nav flex-column">
        <li className="nav-item">
            <Link className="nav-link text-white" to="/users">
            회원관리</Link></li>
        <li className="nav-item">
            <Link className="nav-link text-white" to="/admins">
            관리자목록</Link></li>
        <li className="nav-item">
            <Link className="nav-link text-white" to="/inquiries">
            문의시항</Link></li>
        <li className="nav-item">
            <Link className="nav-link text-white" to="/incidents">
            장애목록</Link></li>
        <li className="nav-item">
            <Link className="nav-link text-white" to="/audit-logs">
            Audit Log</Link></li>
      </ul>
    </div>
  );
}

export default Sidebar;
