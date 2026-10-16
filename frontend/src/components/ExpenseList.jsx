import { Download, LoaderCircle, Mail } from "lucide-react";
import React, { useState } from "react";
import TransactionInfoCard from "./TransactionInfoCard";
import moment from "moment";

const PAGE_SIZE = 6;

function ExpenseList({ transactions, onDelete, onDownload, onEmail }) {
  const [isEmailing, setIsEmailing] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);

  const totalPages = Math.ceil(transactions.length / PAGE_SIZE);
  const paginatedTransactions = transactions.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE,
  );

  const handleEmail = async () => {
    setIsEmailing(true);
    try {
      await onEmail();
    } finally {
      setIsEmailing(false);
    }
  };

  const handleDownload = async () => {
    setIsDownloading(true);
    try {
      await onDownload();
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <div className="card">
      <div className="flex items-center justify-between">
        <h5 className="text-lg">Expense Sources</h5>
        <div className="flex items-center justify-end gap-2">
          <button
            disabled={isEmailing || isDownloading}
            className="card-btn"
            onClick={handleEmail}
          >
            {isEmailing ? (
              <>
                <LoaderCircle size={15} className="w-4 h-4 animate-spin" />{" "}
                Emailing...
              </>
            ) : (
              <>
                <Mail size={15} className="text-base" /> Email
              </>
            )}
          </button>
          <button
            disabled={isEmailing || isDownloading}
            className="card-btn"
            onClick={handleDownload}
          >
            {isDownloading ? (
              <>
                <LoaderCircle size={15} className="w-4 h-4 animate-spin" />{" "}
                Downloading...
              </>
            ) : (
              <>
                <Download size={15} className="text-base" /> Download
              </>
            )}
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2">
        {paginatedTransactions.map((expense) => (
          <TransactionInfoCard
            key={expense.id}
            title={expense.name}
            icon={expense.icon}
            date={moment(expense.date).format("Do MMM YYYY")}
            amount={expense.amount}
            type="expense"
            onDelete={() => onDelete(expense.id)}
          />
        ))}
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-end gap-2 mt-4">
          <button
            className="card-btn"
            disabled={currentPage === 1}
            onClick={() => setCurrentPage((prev) => prev - 1)}
          >
            Prev
          </button>
          <span className="text-sm text-gray-600">
            {currentPage} / {totalPages}
          </span>
          <button
            className="card-btn"
            disabled={currentPage === totalPages}
            onClick={() => setCurrentPage((prev) => prev + 1)}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}

export default ExpenseList;
