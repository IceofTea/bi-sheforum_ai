package com.henry.forum.admin.controller;


import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.Root;
import com.henry.forum.admin.service.RootService;
import com.henry.forum.admin.util.TokenUtil;
import com.henry.forum.admin.util.TokenUtil.Token;

/**
 * 管理员
 * @author 14435
 *
 */
@RestController
@RequestMapping("/api/roots")
@CrossOrigin(origins = {"*"})
public class RootController {
	@Autowired
	private RootService rootService;
	/**
	 * 登录
	 */
	/**
	 * 返回一张图片
	 * Token:会话保持的技术,session，cookie不能跨域
	 * 实际上就是一个字符串,由服务器生成,交给客户端使用，
	 * 通过加密的方式将登录用户的信息,保存在Token中，
	 * 客户端再次进行请求时，可以带着Token请求，
	 * 服务器从Token中解密登录用户信息，
	 * 优势:1.不需要将数据保存到内存中
	 * 2.不受跨域请求的限制,前后端分离,分布系统中,Token广泛应用在会话保持上.
	 * 可逆的加密算法
	 * 
	 */
	@GetMapping
	public APIResult login(Root root,String vercode,String token,
			HttpSession session) {
		APIResult apiResult = new APIResult();
		String oldtoken = TokenUtil.getcode(token);
		// 1.获取参数
		//判断vercode是否正确
		Token tknToken= TokenUtil.parseToken(token);
		if(tknToken.getStatus()==1)
		{
			apiResult.setStatus(403);
			apiResult.setMsg("验证码过期");
			return apiResult;
		}
		if(!vercode.toUpperCase().equals(oldtoken))
		{
			apiResult.setStatus(404);
			apiResult.setMsg("验证码错误");
			return apiResult;
		}
		// 2.调用service
		
		try {
			Root loginRoot= rootService.login(root);
			loginRoot.setPassword(null);
			String loginToken= TokenUtil.createToken(loginRoot.getRootname());
			apiResult.setStatus(200);
			apiResult.setMsg(loginToken);
			apiResult.setData(loginRoot);
		} catch (Exception e) {
			e.printStackTrace();
			apiResult.setStatus(404);
			apiResult.setMsg(e.getMessage());
		}
		// 3.返回数据
		
		return apiResult;
	}
	/**
	 * 注册
	 */
	@PostMapping
	public APIResult register(@RequestBody Root root) {
		return APIResult.ok(rootService.register(root));
	}
	@GetMapping("/one")
	public APIResult byname(String name) {
		return APIResult.ok(rootService.selectByName(name));
	}
	/**
	 * 退出
	 */
	@DeleteMapping
	public APIResult logout(HttpSession session) {
		session.invalidate();
		APIResult apiResult=new APIResult();
		apiResult.setMsg("退出");
		apiResult.setStatus(204);
		return apiResult;
	}
	
	/**
	 * 修改
	 */
	@PutMapping
	public APIResult edit(@RequestBody Root root1) {
		return APIResult.ok(rootService.edit(root1));
	}
}
