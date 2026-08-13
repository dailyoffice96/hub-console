export const formatDateTime = (value) => {
  if (!value) return '';
  const [datePart, timePart] = value.split('T');
  if (!timePart) return datePart;
  const [hh, mm] = timePart.split(':');
  return `${datePart} ${hh}:${mm}`;
};

export const formatDate = (value) => {
  if (!value) return '';
  return value.split('T')[0];
};

// 1,284 / 12.9K / 4.2M처럼 큰 값은 축약하고, 일 단위 통계처럼 작은 값은 그대로 보여준다.
export const formatCompactNumber = (value) => {
  if (value === null || value === undefined) return '-';
  const abs = Math.abs(value);
  if (abs >= 1_000_000) return `${(value / 1_000_000).toFixed(1)}M`;
  if (abs >= 1_000) return `${(value / 1_000).toFixed(1)}K`;
  return value.toLocaleString('ko-KR');
};