import {useState} from 'react';
import {updateUser, dormantUser} from "../api/usersApi"

function UserDetailModal({ user, onClose, onUpdated }) {
    const [phone, setPhone] = useState(user.maskedPhone);
    const [email, setEmail] = useState(user.maskedEmail);

    const handleUpdate = async () => {
        await updateUser(user.id, {...user, maskedPhone: phone, maskedEmail: email});
        onUpdated();
        onClose();
    };

    const handleDormant = async () => {
        await dormantUser(user.id);
        onUpdated();
        onClose();
    };
  return (
    <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
        <div className="modal-dialog">
            <div className="modal-content p-4">
                <h5 className="mb-3">{user.maskedName}님의 회원 상세</h5>

                <p><strong>아이디:</strong> {user.loginId}</p>
                <p><strong>상태:</strong> {user.status}</p>
                <p><strong>가입일:</strong> {user.createdAt}</p>
            </div>
        </div>
    </div>
  );
}

export default UserDetailModal;
