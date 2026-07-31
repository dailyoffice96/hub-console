function StatCard({ icon, count, label }){
  return (
    <div className="card p-3 text-center">
      <div className="mb-2" style={{fontSize: '1.5rem'}}>{icon}</div>
      <h3 className="mb-0">{count}</h3>
      <p className="text-muted mb-0">{label}</p>
    </div>
  );
}


export default StatCard;
