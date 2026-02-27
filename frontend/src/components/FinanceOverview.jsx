import React from "react";
import CustomPieChart from "./CustomPieChart";
import { addThousandsSeparator } from "../util/util";

function FinanceOverview({ totalBalance, totalIncome, totalExpense }) {
  const COLORS = ["#A5B4FC", "#F9A8D4", "#7DD3FC"];

  const balanceData = [
    { name: "Total Balance", amount: totalBalance },
    { name: "Total Income", amount: totalIncome },
    { name: "Total Expense", amount: totalExpense },
  ];

  return (
    <div className="card">
      <div className="flex items-center justify-between">
        <h5 className="text-lg">Financial Overview</h5>
      </div>

      <CustomPieChart
        data={balanceData}
        label="Total Balance"
        totalAmount={`₹${addThousandsSeparator(totalBalance)}`}
        colors={COLORS}
        showTextAnchor
      />
    </div>
  );
}

export default FinanceOverview;
