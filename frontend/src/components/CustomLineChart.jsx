import React from "react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from "recharts";

function CustomTooltip({ active, payload, label }) {
  if (active && payload && payload.length) {
    const item = payload[0].payload;

    return (
      <div className="p-4 bg-white rounded-2xl shadow-lg border border-gray-100 text-sm min-w-[160px]">
        <p className="font-bold text-sm text-gray-900 mb-2">{item.month}</p>

        <div className="w-full h-[1px] bg-gray-100 my-2"></div>

        <div className="mb-2">
          <span className="font-bold text-sm text-gray-800">Total: </span>
          <span className="font-bold text-[#2563EB] text-sm text-base">
            ₹{item.totalAmount.toLocaleString()}
          </span>
        </div>

        <div className="w-full h-[1px] bg-gray-100 my-2"></div>

        <p className="text-gray-500 font-medium text-xs mb-1">Details:</p>
        {item.items && item.items.length > 0 ? (
          item.items.map((i, idx) => (
            <div key={idx} className="flex justify-between items-center mb-1">
              <span className="text-gray-700 text-xs">{i.categoryName}:</span>
              <span className="text-gray-700 font-medium">
                ₹{i.amount.toLocaleString()}
              </span>
            </div>
          ))
        ) : (
          <div className="text-gray-400 text-xs">No category details.</div>
        )}
      </div>
    );
  }
  return null;
}

export default function IncomeOverview({ data }) {
  if (!data || data.length === 0)
    return (
      <div className="h-64 flex items-center justify-center text-gray-400">
        No data available.
      </div>
    );

  return (
    <div className="w-full h-full bg-white rounded-xl p-4">
      <div className="h-[300px] w-full">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart
            data={data}
            margin={{ top: 20, right: 20, left: 0, bottom: 0 }}
          >
            <defs>
              <linearGradient id="colorBlue" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#2563EB" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#2563EB" stopOpacity={0} />
              </linearGradient>
            </defs>

            <CartesianGrid
              vertical={false}
              strokeDasharray="3 3"
              stroke="#f3f4f6"
            />

            <XAxis
              dataKey="month"
              tick={{ fill: "#6B7280", fontSize: 12, fontWeight: 500 }}
              axisLine={false}
              tickLine={false}
              dy={10}
            />

            <YAxis
              tick={{ fill: "#6B7280", fontSize: 12 }}
              axisLine={false}
              tickLine={false}
              tickFormatter={(value) => `₹${value / 1000}k`}
            />

            <Tooltip
              content={<CustomTooltip />}
              cursor={{ stroke: "#E5E7EB", strokeWidth: 2 }}
            />

            <Area
              type="monotone"
              dataKey="totalAmount"
              stroke="#2563EB"
              strokeWidth={3}
              fill="url(#colorBlue)"
              fillOpacity={1}
              activeDot={{
                r: 6,
                fill: "#2563EB",
                stroke: "white",
                strokeWidth: 2,
              }}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
