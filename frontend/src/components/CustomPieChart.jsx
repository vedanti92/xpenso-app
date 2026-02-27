import React from "react";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts";

export default function CustomPieChart({
  data = [],
  label = "",
  totalAmount = "",
  colors = [],
  showTextAnchor = false,
}) {
  const amount = Number(totalAmount);

  return (
    <div className="w-full h-64 relative">
      <ResponsiveContainer>
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={65}
            outerRadius={100}
            paddingAngle={3}
            dataKey="amount"
            cornerRadius={5}
          >
            {data.map((item, index) => (
              <Cell
                key={`cell-${index}`}
                fill={colors[index % colors.length]}
              />
            ))}
          </Pie>

          <Tooltip formatter={(value, name) => [`₹${value}`, name]} />
        </PieChart>
      </ResponsiveContainer>

      {data.some((item) => item.amount > 0) && (
        <div className="flex items-center justify-center gap-6 mt-10 flex-wrap">
          {data.map((item, index) => (
            <div key={index} className="flex items-center gap-2">
              <span
                className="w-3 h-3 rounded-full inline-block"
                style={{ backgroundColor: colors[index % colors.length] }}
              ></span>
              <p className="text-sm text-gray-700">{item.name}</p>
            </div>
          ))}
        </div>
      )}

      {showTextAnchor && (
        <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
          <p className="text-xs text-gray-400">{label}</p>
          <p className="text-lg font-semibold">{totalAmount}</p>
        </div>
      )}
    </div>
  );
}
