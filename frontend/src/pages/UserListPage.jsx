import { useState, useEffect } from 'react';
import {getUser, getUserStats} from "../api/usersApi"
import StatCard from '../components/StatCard';
import UserDetailModal from '../components/UserDetailModal';


function UserListPage() {
    const [users, setUsers] = useState([]);
    const [name, setName] = useState("");
    const [status, setStatus] = useState("");
    const [page, setPage] = useState(0);
    const [loginId, setLoginId] = useState("");
    const [states, setStates] = useState({active: 0, dormant: 0, withdrawn: 0 });
    const [totalPages, setTotalPages] = useState(0);
    const [selectedUser, setSelectedUser] = useState(null);

    useEffect(() => {
    getUser({name, status, page, size: 10})
     .then(res => {
         setUsers(res.data.content || []);
         setTotalPages(res.data.totalPages);
         })
     .catch(err => {
                console.error(err);
                setUsers([]);
           });
    }, [page, status]);
    //"page 또는 status 값이 바뀔 때마다, 이 안의 코드를 다시 실행해라"

    useEffect(() => {
     getUserStats().then(res => setStates(res.data));
    }, []);

    useEffect(() => {
          fetchStats();
      }, []);

    const fetchStats = () => {
        getUserStats().then(res => setStates(res.data));
    };

    const handleSearch = async() => {
        setPage(0);
        const res = await getUser({name, loginId, status, page: 0, size: 10});
        setUsers(res.data.content);
    }

  return (
    <div>
      <div className="row mb-4">
        <div className="col-4"><StatCard icon="✅" count={states.active} label="활동 회원" /></div>
        <div className="col-4"><StatCard icon="⚡" count={states.dormant} label="휴면 회원" /></div>
        <div className="col-4"><StatCard icon="🔴" count={states.withdrawn} label="탈퇴 회원" /></div>
      </div>

      <div className="d-flex mb-3">
          <input
              className="form-control me-2"
              placeholder="이름 검색"
              value={name}
              onChange={(e) => setName(e.target.value)} />
          <input
              className="form-control me-2"
              placeholder="아이디 검색"
              value={loginId}
              onChange={(e) => setLoginId(e.target.value)} />
          <select className="form-select me-2" style={{width: '150px'}}
            value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">전체 상태</option>
            <option value="ACTIVE">활성</option>
            <option value="DORMANT">휴면</option>
            <option value="WITHDRAWN">탈퇴</option> </select>
          <button className="btn btn-primary" onClick={handleSearch}>검색</button>
      </div>

      <table className="table table-hover">
        <thead>
            <tr>
              <th>번호</th>
              <th>이름</th>
              <th>아이디</th>
              <th>전화번호</th>
              <th>이메일</th>
              <th>상태</th>
              <th>가입일</th>
            </tr>
        </thead>
        {/*  .map((user, index) => ...) */}
        {/*  .map()이 배열을 하나씩 돌면서, "지금 이 순간의 회원 한 명을 user라고 부를게, 몇 번째인지는 index라고 부를게 */}
        <tbody>
            {users.map((user, index) => (
            <tr key={user.id} onClick={() => setSelectedUser(user)} style ={{cursor: 'pointer'}}>
                <td>{page * 10 + index + 1}</td>
                <td>{user.maskedName}</td>
                <td>{user.loginId}</td>
                <td>{user.maskedPhone}</td>
                <td>{user.maskedEmail}</td>
                <td>{user.status}</td>
                <td>{user.createdAt}</td>
            </tr>
            ))}
        </tbody>
      </table>

      {selectedUser && (
        <UserDetailModal
            user={selectedUser}
            onClose={() => setSelectedUser(null)}
            onUpdated={() => {getUser({name, status, page, size:10})
            .then(res => setUsers(res.data.content));
            fetchStats();
            }}
        />
      )}

            <div className="d-flex justify-content-center mt-3">
                <button className="btn btn-outline-secondary me-2" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</button>
                <span className="align-self-center mx-2">{page + 1} / {totalPages} 페이지</span>
                <button className="btn btn-outline-secondary" disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>다음</button>
            </div>
    </div>
  );
}

export default UserListPage;
