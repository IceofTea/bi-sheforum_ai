export const formatRelativeTime = (timestamp) => {
  if (!timestamp && timestamp !== 0) return '';
  
  let date;
  try {
    if (typeof timestamp === 'number') {
      date = new Date(timestamp);
    } else if (typeof timestamp === 'string') {
      if (/^\d+$/.test(timestamp)) {
        date = new Date(parseInt(timestamp));
      } else {
        const str = timestamp
          .replace(/年/g, '-')
          .replace(/月/g, '-')
          .replace(/号/g, '')
          .replace(/\s+/, ' ')
          .trim();
        date = new Date(str);
        if (isNaN(date.getTime())) {
          date = new Date(parseInt(timestamp));
        }
      }
    } else {
      date = new Date(timestamp);
    }
    if (isNaN(date.getTime())) return '';
  } catch (e) {
    return '';
  }
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / (1000 * 60));
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}小时前`;
  
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}天前`;
  
  const thisYear = now.getFullYear();
  if (date.getFullYear() === thisYear) {
    return `${date.getMonth() + 1}月${date.getDate()}日`;
  }
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
};

export const formatDateTime = (timestamp) => {
  if (!timestamp && timestamp !== 0) return '';
  let date;
  try {
    if (typeof timestamp === 'number') {
      date = new Date(timestamp);
    } else if (typeof timestamp === 'string') {
      if (/^\d+$/.test(timestamp)) {
        date = new Date(parseInt(timestamp));
      } else {
        const str = timestamp
          .replace(/年/g, '-')
          .replace(/月/g, '-')
          .replace(/号/g, '')
          .replace(/\s+/, ' ')
          .trim();
        date = new Date(str);
        if (isNaN(date.getTime())) {
          date = new Date(parseInt(timestamp));
        }
      }
    } else {
      date = new Date(timestamp);
    }
    if (isNaN(date.getTime())) return '';
  } catch (e) {
    return '';
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};