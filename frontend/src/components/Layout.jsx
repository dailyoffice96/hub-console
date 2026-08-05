import Header from "./Header"
import Sidebar from "./Sidebar"
import { Outlet } from "react-router-dom"
import bgImage from "../images/main-bg.png";

function Layout() {
  return (
   <div className="d-flex">
       <Sidebar />
       <div
         className="flex-grow-1"
         style={{
             backgroundColor: '#F4F6FA',   // 헤더+본문 공통 배경색
             minHeight: '100vh'
         }}
       >
           <Header />
           <div className="p-4">
               <Outlet />
           </div>
       </div>
   </div>
  )
}

export default Layout;


