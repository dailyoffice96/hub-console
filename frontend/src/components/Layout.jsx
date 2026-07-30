import Header from "./Header"
import Sidebar from "./Sidebar"
import {Outlet} from "react-router-dom"


/* flex-grow-0"남은 공간이 있어도, 늘어나지 마라" (원래 크기만큼만)
flex-grow-1"남은 공간을 최대한 늘려서 다 차지해라"
d-flex 써야 함 flex-grow-1 자체가 작동을 안 함 */

function Layout() {
  return(
   <div className="d-flex">
       <Sidebar />
       <div className="flex-grow-1">
           <Header />
           <div className="p-4">
               <Outlet />
           </div>
       </div>
   </div>
  )
}

export default Layout;