import axios from 'axios'
import router from '@/router/index'
//配置BaseURL
axios.defaults.baseURL = import.meta.env.VITE_SERVER;

// 添加请求拦截器
axios.interceptors.request.use(function (config) {
	// 在发送请求之前做些什么
	// var token = sessionStorage.token;
	// config.headers.Authorization = `Bearer ${token}`;
	return config;
}, function (error) {
	// 对请求错误做些什么
	return Promise.reject(error);
});

// 添加响应拦截器
axios.interceptors.response.use(function (response) {
	// 2xx 范围内的状态码都会触发该函数。
	// 抽取data中的数据返回给下一步
	return response.data;
}, function (error) {
	// 超出 2xx 范围的状态码都会触发该函数。
	// 对响应错误做点什么
	// return Promise.reject(error);
	// let { config: { url }, response: { status } } = error;
	// switch (status) {
	// 	case 401:
	// 		//消息通知
	// 		ElNotification.error({
	// 			title: `错误：${status}`,
	// 			message: 'token已过期，请重新登录',
	// 		});
	// 		//获取当前页面的路由
	// 		let { fullPath } = router.currentRoute.value;
	// 		//强制跳转
	// 		router.replace({ name: 'Login', query: { redirect: fullPath } });
	// 		// console.log(router, fullPath);
	// 		break;
	// 	case 404:
	// 		//消息通知
	// 		ElNotification.error({
	// 			title: `错误：${status}`,
	// 			message: `前台API接口地址：${url},地址不存在`,
	// 		});
	// 		break;
	// 	case 500:
	// 		//消息通知
	// 		ElNotification.error({
	// 			title: `错误：${status}`,
	// 			message: `后台API接口地址：${url}错误,请先检查Network`,
	// 		});
	// 		break;
	// 	default:
	// 		ElNotification.error({
	// 			title: `错误：${status}`,
	// 			message: `API接口：${url}错误`,
	// 		});
	// 		break;
	// }
	// console.log(status);
});
