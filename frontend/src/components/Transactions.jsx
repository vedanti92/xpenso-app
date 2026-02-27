import { ArrowRight } from "lucide-react";
import React from "react";
import TransactionInfoCard from "./TransactionInfoCard";
import moment from "moment";

function Transactions({ transactions, onMore, type, title }) {
  return (
    <div className="card">
      <div className="flex items-center justify-between">
        <h5 className="text-lg">{title}</h5>
        <button className="card-btn" onClick={onMore}>
          More <ArrowRight size={15} className="text-base" />
        </button>
      </div>

      <div className="mt-6">
        {transactions?.slice(0, 5).map((item) => (
          <TransactionInfoCard
            key={item.id}
            title={item.name}
            icon={item.icon}
            date={moment(item.date).format("Do MMM YYYY")}
            amount={item.amount}
            type={type}
            hideDeleteBtn
          />
        ))}
      </div>
    </div>
  );
}

export default Transactions;
