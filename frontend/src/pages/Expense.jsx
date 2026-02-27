import React, { useEffect, useState } from "react";
import Dashboard from "../components/Dashboard";
import { useUser } from "../hooks/useUser";
import axiosConfig from "../util/axiosConfig";
import { API_ENDPOINTS } from "../util/apiEndpoints";
import toast from "react-hot-toast";
import ExpenseList from "../components/ExpenseList";
import Model from "../components/Model";
import AddExpenseForm from "../components/AddExpenseForm";
import DeleteAlert from "../components/DeleteAlert";
import ExpenseOverview from "../components/ExpenseOverview";

function Expense() {
  const [expenseData, setExpenseData] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);

  const [openAddExpenseModel, setOpenAddExpenseModel] = useState(false);
  const [openDeleteAlert, setOpenDeleteAlert] = useState({
    show: false,
    data: null,
  });

  const fetchExpenseDetails = async () => {
    if (loading) return;

    setLoading(true);

    try {
      const response = await axiosConfig.get(API_ENDPOINTS.GET_ALL_EXPENSES);
      if (response.status === 200) {
        setExpenseData(response.data);
      }
    } catch (error) {
      console.error("Failed to fetch expense details: ", error);
      toast.error(
        error.response.data.message || "Failed to fetch expense details."
      );
    } finally {
      setLoading(false);
    }
  };

  // fetch categories for expense
  const fetchExpenseCategories = async () => {
    try {
      const response = await axiosConfig.get(
        API_ENDPOINTS.CATEGORY_BY_TYPE("expense")
      );
      if (response.status === 200) {
        setCategories(response.data);
      }
    } catch (error) {
      console.log("Failed to fetch the expense categories: ", error);
      toast.error(
        error.data.response.message || "Failed to fetch the expense categories."
      );
    }
  };

  // save the expense details
  const handleAddExpense = async (expense) => {
    const { name, amount, date, icon, categoryId } = expense;

    if (!name.trim()) {
      toast.error("Please enter an expense source.");
      return;
    }

    if (!amount || isNaN(amount) || Number(amount) <= 0) {
      toast.error("Please enter a valid amount greater than zero.");
      return;
    }

    if (!date) {
      toast.error("Please enter a date.");
      return;
    }
    const today = new Date().toISOString().split("T")[0];
    if (date > today) {
      toast.error("Please select a valid date that is not in the future.");
      return;
    }

    if (!categoryId || categoryId === "") {
      toast.error("Please select a category.");
      return;
    }

    try {
      const response = await axiosConfig.post(API_ENDPOINTS.ADD_EXPENSE, {
        name,
        amount: Number(amount),
        date,
        icon,
        categoryId,
      });
      if (response.status === 201) {
        setOpenAddExpenseModel(false);
        toast.success("Expense added successfully!");
        fetchExpenseDetails();
        fetchExpenseCategories();
      }
    } catch (error) {
      console.error("Error adding expense: ", error);
      toast.error(error.response.data.message || "Failed to add expense.");
    }
  };

  // delete expense
  const deleteExpense = async (id) => {
    try {
      await axiosConfig.delete(API_ENDPOINTS.DELETE_EXPENSE(id));
      setOpenDeleteAlert({ show: false, data: null });
      toast.success("Expense entry deleted successfully!");
      fetchExpenseDetails();
    } catch (error) {
      console.error("Error deleting expense: ", error);
      toast.error(error.response.data.message || "Failed to delete expense.");
    }
  };

  const handleDownloadExpenseDetails = async () => {
    try {
      const response = await axiosConfig.get(
        API_ENDPOINTS.EXPENSE_EXCEL_DOWNLOAD,
        { responseType: "blob" }
      );
      let filename = "expense_details.xlsx";
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", filename);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success("Expense details downloaded successfully!");
    } catch (error) {
      console.error("Error downloading expense details: ", error);
      toast.error(
        error.response.data.message || "Failed to download expense details."
      );
    }
  };

  const handleEmailExpenseDetails = async () => {
    try {
      const response = await axiosConfig.get(API_ENDPOINTS.EMAIL_EXPENSE);
      if (response.status === 200) {
        toast.success("Expense details emailed successfully!");
      }
    } catch (error) {
      console.error("Error emailing expense details: ", error);
      toast.error(
        error.response.data.message || "Failed to email expense details."
      );
    }
  };

  useEffect(() => {
    fetchExpenseDetails();
    fetchExpenseCategories();
  }, []);

  useUser();

  return (
    <div>
      <Dashboard activeMenu="Expense">
        <div className="my-5 mx-auto">
          <div className="grid grid-cols-1 gap-6">
            <div>
              {/* Overview for expense with line chart */}
              <ExpenseOverview
                transactions={expenseData}
                onAddExpense={() => setOpenAddExpenseModel(true)}
              />
            </div>

            <ExpenseList
              transactions={expenseData}
              onDelete={(id) => setOpenDeleteAlert({ show: true, data: id })}
              onDownload={handleDownloadExpenseDetails}
              onEmail={handleEmailExpenseDetails}
            />

            {/* Add expense model */}
            <Model
              isOpen={openAddExpenseModel}
              onClose={() => setOpenAddExpenseModel(false)}
              title="Add Expense"
            >
              <AddExpenseForm
                onAddExpense={(expense) => handleAddExpense(expense)}
                categories={categories}
              />
            </Model>

            {/* Delete expense model */}
            <Model
              isOpen={openDeleteAlert.show}
              onClose={() => setOpenDeleteAlert({ show: false, data: null })}
              title="Delete Expense"
            >
              <DeleteAlert
                content="Are you sure you want to delete this expense entry? This action cannot be undone."
                onDelete={() => deleteExpense(openDeleteAlert.data)}
              />
            </Model>
          </div>
        </div>
      </Dashboard>
    </div>
  );
}

export default Expense;
