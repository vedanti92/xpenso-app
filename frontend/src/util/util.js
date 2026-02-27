export const addThousandsSeparator = (num) => {
  if (num == null || isNaN(num)) return "";

  // convert number to string to handle decimals
  const numStr = num.toString();
  const parts = numStr.split("."); // split integer and decimal parts

  let integerPart = parts[0];
  let decimalPart = parts[1];

  // regex for Indian numbering system
  // it handles first 3 digits, then every 2 digits
  const lastThree = integerPart.substring(integerPart.length - 3);
  const otherNumbers = integerPart.substring(0, integerPart.length - 3);

  if (otherNumbers !== "") {
    // apply comma after every 2 digits for the 'otherNumbers' part
    const formattedOtherNumbers = otherNumbers.replace(
      /\B(?=(\d{2})+(?!\d))/g,
      ","
    );
    integerPart = formattedOtherNumbers + "," + lastThree;
  } else {
    integerPart = lastThree; // no change if less than 4 digits
  }

  // combine integer and decimal parts
  return decimalPart ? `${integerPart}.${decimalPart}` : integerPart;
};

export function prepareLineChartData(transactions = []) {
  if (!Array.isArray(transactions)) return [];

  const grouped = {};

  transactions.forEach((t) => {
    const date = t.date;

    if (!date) return;

    if (!grouped[date]) {
      grouped[date] = {
        date,
        totalAmount: 0,
        items: [],
      };
    }

    grouped[date].totalAmount += Number(t.amount);
    grouped[date].items.push(t);
  });

  const result = Object.values(grouped).map((entry) => {
    const dt = new Date(entry.date);

    const day = dt.getDate();
    const suffix =
      day === 1 ? "st" : day === 2 ? "nd" : day === 3 ? "rd" : "th";

    entry.month = `${day}${suffix} ${dt.toLocaleString("default", {
      month: "short",
    })}`;

    return entry;
  });

  return result.sort((a, b) => new Date(a.date) - new Date(b.date));
}
