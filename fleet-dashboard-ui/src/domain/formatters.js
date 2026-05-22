/**
 * Dashboard domain formatting utilities.
 *
 * @author Nilabro Saha
 */
/**
 * Formats date/time values into operator-friendly local timestamp text.
 *
 * @param {Date|string|null|undefined} dateInput incoming timestamp value
 * @returns {string} formatted timestamp or fallback text
 */
export function formatLocalTimestamp(dateInput) {
  if (!dateInput) {
    return "-";
  }

  const date = dateInput instanceof Date ? dateInput : new Date(dateInput);
  if (Number.isNaN(date.getTime())) {
    return String(dateInput);
  }

  const day = String(date.getDate()).padStart(2, "0");
  const month = date.toLocaleString(undefined, { month: "short" });
  const year = date.getFullYear();

  let hours = date.getHours();
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");
  const meridiem = hours >= 12 ? "pm" : "am";

  hours = hours % 12;
  hours = hours === 0 ? 12 : hours;

  return `${day}-${month}-${year} ${hours}:${minutes}:${seconds} ${meridiem}`;
}
