import { useState, useEffect } from 'react';
import axiosInstance from './axiosInstance';

function UserListPage() {
    const [members, serMembers] = useState("");
    const [name, setName] = useState("");
    const [page, setPage] = useState(0);

    useEffect(() => {

    }, [page]);

    const handleSearch = () => {

    }

  return (
    <div>
      <h1>User List Page</h1>
    </div>
  );
}

export default UserListPage;
