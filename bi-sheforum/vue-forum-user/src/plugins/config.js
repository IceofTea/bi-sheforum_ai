
let URL = import.meta.env.VITE_SERVER_UP;
// 生产环境（VITE_SERVER_UP 为空）下使用相对路径，前后端同源
export const VERCODE_URL = URL ? URL + ':4477' : '';
export const THREAD_URL = URL ? URL + ':9000/upload/' : '/upload/';