// src/utils/dateUtils.js
export const formatToDate = (input) => {
  const today = new Date();
  const currentYear = today.getFullYear().toString();
  const currentMonth = String(today.getMonth() + 1).padStart(2, '0');

  input = input.replace(/\D/g, '');

  let y = '', m = '', d = '';

  if (input.length === 8) {
    y = input.slice(0, 4);
    m = input.slice(4, 6);
    d = input.slice(6, 8);
  } else if (input.length === 6) {
    y = '20' + input.slice(0, 2);
    m = input.slice(2, 4);
    d = input.slice(4, 6);
  } else if (input.length === 4) {
    y = currentYear;
    m = input.slice(0, 2);
    d = input.slice(2, 4);
  } else if (input.length === 1 || input.length === 2) {
    y = currentYear;
    m = currentMonth;
    d = input.padStart(2, '0');
  } else {
    return input;
  }

  const yearNum = parseInt(y, 10);
  const monthNum = Math.max(1, Math.min(parseInt(m, 10), 12));
  const getLastDay = (year, month) => new Date(year, month, 0).getDate();
  const lastDay = getLastDay(yearNum, monthNum);
  const dayNum = Math.max(1, Math.min(parseInt(d, 10), lastDay));

  const fixedMonth = String(monthNum).padStart(2, '0');
  const fixedDay = String(dayNum).padStart(2, '0');

  return `${yearNum}-${fixedMonth}-${fixedDay}`;
};