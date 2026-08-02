export const formatDateTime = (value) => {
  if (!value) return '';
  const [datePart, timePart] = value.split('T');
  if (!timePart) return datePart;
  const [hh, mm] = timePart.split(':');
  return `${datePart} ${hh}:${mm}`;
};