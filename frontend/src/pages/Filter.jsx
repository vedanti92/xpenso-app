import React, { useState } from "react";
import Dashboard from "../components/Dashboard";
import { useUser } from "../hooks/useUser";
import { Search } from "lucide-react";
import axiosConfig from "../util/axiosConfig";
import { API_ENDPOINTS } from "../util/apiEndpoints";
import toast from "react-hot-toast";
import TransactionInfoCard from "../components/TransactionInfoCard";
import moment from "moment";

function Filter() {
  const [type, setType] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [keyword, setKeyword] = useState("");
  const [sortField, setSortField] = useState("");
  const [sortOrder, setSortOrder] = useState("");
  const [transacttions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();

    // validation
    if (!type) {
      toast.error("Please select a transaction type.");
      return;
    }

    if (!sortField) {
      toast.error("Please select a sort field.");
      return;
    }

    if (!sortOrder) {
      toast.error("Please select a sort order.");
      return;
    }

    setLoading(true);
    try {
      const response = await axiosConfig.post(API_ENDPOINTS.APPLY_FILTER, {
        type,
        startDate,
        endDate,
        keyword,
        sortField,
        sortOrder,
      });
      setTransactions(response.data);
    } catch (error) {
      console.error("Error fetching filtered transactions: ", error);
      toast.error(error.message || "Failed to fetch filtered transactions");
    } finally {
      setLoading(false);
    }
  };

  useUser();

  return (
    <div>
      <Dashboard activeMenu="Filters">
        <div className="my-5 mx-auto">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-semibold">Filter Transactions</h2>
          </div>
          <div className="card p-4 mb-4">
            <div className="mb-4">
              <h5 className="text-lg font-semibold">Select Filters</h5>
              <p className="text-xs text-red-500">
                * To begin searching, make sure "Type", "Sort Field", and "Sort
                Order" are selected.
              </p>
            </div>
            <form className="grid grid-cols-1 sm:grid-cols-3 md:grid-cols-6 gap-4">
              <div>
                <label
                  htmlFor="type"
                  className="block text-sm font-medium mb-1"
                >
                  Type
                </label>
                <select
                  value={type}
                  onChange={(e) => setType(e.target.value)}
                  id="type"
                  className="w-full border rounded px-3 py-2"
                  defaultValue=""
                >
                  <option value="" disabled>
                    Select Type
                  </option>
                  <option value="income">Income</option>
                  <option value="expense">Expense</option>
                </select>
              </div>
              <div>
                <label
                  htmlFor="startdate"
                  className="block text-sm font-medium mb-1"
                >
                  Start Date
                </label>
                <input
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  id="startdate"
                  type="date"
                  className="w-full border rounded px-3 py-2"
                />
              </div>
              <div>
                <label
                  htmlFor="enddate"
                  className="block text-sm font-medium mb-1"
                >
                  End Date
                </label>
                <input
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  id="enddate"
                  type="date"
                  className="w-full border rounded px-3 py-2"
                />
              </div>
              <div>
                <label
                  htmlFor="sortfield"
                  className="block text-sm font-medium mb-1"
                >
                  Sort Field
                </label>
                <select
                  value={sortField}
                  onChange={(e) => setSortField(e.target.value)}
                  id="sortfield"
                  className="w-full border rounded px-3 py-2"
                  defaultValue=""
                >
                  <option value="" disabled>
                    Select Field
                  </option>
                  <option value="date">Date</option>
                  <option value="amount">Amount</option>
                  <option value="category">Category</option>
                </select>
              </div>
              <div>
                <label
                  htmlFor="sortorder"
                  className="block text-sm font-medium mb-1"
                >
                  Sort Order
                </label>
                <select
                  value={sortOrder}
                  onChange={(e) => setSortOrder(e.target.value)}
                  id="sortorder"
                  className="w-full border rounded px-3 py-2"
                  defaultValue=""
                >
                  <option value="" disabled>
                    Select Order
                  </option>
                  <option value="asc">Ascending</option>
                  <option value="desc">Descending</option>
                </select>
              </div>
              <div className="sm:col-span-1 md:col-span-1 flex items-end">
                <div className="w-full">
                  <label
                    htmlFor="keyword"
                    className="block text-sm font-medium mb-1"
                  >
                    Search
                  </label>
                  <input
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    id="keyword"
                    type="text"
                    placeholder="Search..."
                    className="w-full border rounded px-3 py-2"
                  />
                </div>
                <button
                  onClick={handleSearch}
                  className="ml-2 mb-1 p-2 bg-blue-500 hover:bg-blue-600 text-white rounded flex items-center justify-center cursor-pointer"
                >
                  <Search size={20} />
                </button>
              </div>
            </form>
          </div>
          <div className="card p-4">
            <div className="flex justify-between items-center mb-4">
              <h5 className="text-2xl font-semibold">Results</h5>
            </div>
            {transacttions.length === 0 && !loading ? (
              <p className="text-gray-500">
                Use the filters and hit the search button to view matching
                transactions.
              </p>
            ) : (
              ""
            )}
            {loading ? (
              <p className="text-gray-500">Loading Transactions...</p>
            ) : (
              ""
            )}
            {transacttions.map((transacttion) => (
              <TransactionInfoCard
                key={transacttion.id}
                title={transacttion.name}
                icon={transacttion.icon}
                date={moment(transacttion.date).format("Do MMM YYYY")}
                amount={transacttion.amount}
                type={type}
                hideDeleteBtn
              />
            ))}
          </div>
        </div>
      </Dashboard>
    </div>
  );
}

export default Filter;
