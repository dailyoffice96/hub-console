import Header from "./Header"
import Sidebar from "./Sidebar"
import { Outlet } from "react-router-dom"

function Layout() {
  return (
   // 화면 높이를 딱 100vh로 고정하고 넘치는 건 이 바깥 레벨에서는 안 잘리게(overflow 없음),
   // 대신 아래 본문 영역이 "남는 높이만큼만" 차지하도록 해서 헤더/사이드바는 항상 그대로 보이고
   // 페이지마다 있는 표(RowTable)가 자기 영역 안에서만 세로 스크롤되게 만든다.
   <div className="d-flex" style={{ height: '100vh' }}>
       <Sidebar />
       <div
         className="flex-grow-1 d-flex flex-column"
         style={{
             backgroundColor: '#E3E6ED',
             height: '100vh',
             minWidth: 0   // flex 아이템 기본값(min-width:auto) 때문에 안쪽 표의 최소 너비가
                            // 그대로 이 영역을 넓혀버려서 화면 전체가 가로 스크롤되던 문제 방지
         }}
       >
           <Header />
           {/* 헤더를 뺀 나머지 세로 공간을 여기가 다 차지한다. overflowY:auto는 각 페이지 표가
               다 못 담을 만큼 창이 작을 때를 위한 최후의 보조 장치일 뿐, 평소엔 안 쓰인다. */}
           <div className="p-4 flex-grow-1" style={{ minHeight: 0, overflowY: 'auto' }}>
               <Outlet />
           </div>
       </div>
   </div>
  )
}

export default Layout;
