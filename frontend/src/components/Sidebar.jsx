import React, { useContext } from "react";
import { AppContext } from "../context/AppContext";
import { User } from "lucide-react";
import { SIDEBAR_DATA } from "../assets/assets";
import { useNavigate } from "react-router-dom";

function Sidebar({ activeMenu }) {
  const { user } = useContext(AppContext);
  const navigate = useNavigate();

  return (
    <div className="w-65 h-[calc(100vh-60px)] bg-white border-gray-200/50 p-5 fixed top[60px] z-20">
      <div className="flex flex-col items-center justify-center gap-3 mt-3 mb-7">
        {user.profileImageUrl ? (
          <img
            src={user.profileImageUrl || ""}
            alt="Profile Photo"
            className="w-20 h-20 bg-slate-400 rounded-full"
          />
        ) : (
          <User className="w-20 h-20 text-xl" />
        )}
        <h5 className="text-gray-950 font-medium leading-6">
          {user.fullName || ""}
        </h5>
      </div>
      {SIDEBAR_DATA.map((item, index) => (
        <button
          onClick={() => navigate(item.path)}
          key={`menu_${index}`}
          className={`w-full flex items-center gap-4 text-[15px] py-3 px-6 rounded-lg mb-3 cursor-pointer ${
            activeMenu == item.label ? "text-white bg-blue-500" : ""
          }`}
        >
          <item.icon className="text-xl" />
          {item.label}
        </button>
      ))}
    </div>
  );
}

export default Sidebar;
