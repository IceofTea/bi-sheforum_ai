package com.henry.forum.admin.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.UserInfo;
import com.henry.forum.admin.service.UserInfoService;
import com.henry.forum.admin.util.TokenUtil;
import com.henry.forum.admin.util.TokenUtil.Token;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"*"})
public class UserInfoController {
	@Autowired
	private UserInfoService userInfoService;
	/**
	 * 登录
	 * @param pageSize
	 * @param pageIndex
	 * @return
	 */
	@GetMapping
	public APIResult login(UserInfo userInfo, String vercode, String token, HttpSession session) {
		Token tknToken = TokenUtil.parseToken(token);
		if (tknToken.getStatus() == 1) {
			return APIResult.notFound("验证码过期");
		}
		String oldtoken = TokenUtil.getcode(token);
		if (!vercode.toUpperCase().equals(oldtoken)) {
			return APIResult.notFound("验证码错误");
		}

		try {
			UserInfo loginRoot = userInfoService.login(userInfo);
			loginRoot.setPassword(null);
			userInfoService.updateLoginStats(loginRoot.getId());
			userInfoService.addExperience(loginRoot.getId(), 5);
			userInfoService.addPoints(loginRoot.getId(), 1);
			String loginToken = TokenUtil.createToken(loginRoot.getUsername());
			loginRoot = userInfoService.selectById(loginRoot.getId());
			APIResult result = APIResult.ok(loginRoot);
			result.setMsg(loginToken);
			return result;
		} catch (Exception e) {
			return APIResult.notFound(e.getMessage());
		}
	}
	/**
	 * 登录（POST，密码在请求体中）
	 */
	@PostMapping("/login")
	public APIResult loginPost(@RequestBody Map<String, String> params) {
		String username = params.get("username");
		String password = params.get("password");

		if (username == null || password == null) {
			return APIResult.notFound("用户名或密码不能为空");
		}

		try {
			UserInfo loginUser = new UserInfo();
			loginUser.setUsername(username);
			loginUser.setPassword(password);
			UserInfo result = userInfoService.login(loginUser);
			result.setPassword(null);
			userInfoService.updateLoginStats(result.getId());
			userInfoService.addExperience(result.getId(), 5);
			userInfoService.addPoints(result.getId(), 1);
			result = userInfoService.selectById(result.getId());
			String loginToken = TokenUtil.createToken(result.getUsername());
			APIResult apiResult = APIResult.ok(result);
			apiResult.setMsg(loginToken);
			return apiResult;
		} catch (Exception e) {
			return APIResult.notFound(e.getMessage());
		}
	}

	/**
	 * 注册
	 * @throws Exception 
	 */
	@PostMapping("/register")
	public APIResult register(@RequestBody UserInfo userInfo) {
		try {
			return APIResult.ok(userInfoService.register(userInfo));
		} catch (Exception e) {
			return APIResult.notFound(e.getMessage());
		}
	}
	/**
	 * 退出
	 */
	@DeleteMapping
	public APIResult logout(HttpSession session) {
		session.invalidate();
		return APIResult.ok("退出成功");
	}

	@GetMapping("/page")
	public APIResult list(Integer pageSize, Integer pageIndex) {
		return APIResult.ok(userInfoService.getForPage(pageIndex, pageSize));
	}
	
	@GetMapping("/one")
	public APIResult one(@RequestParam("id") Integer id) {
		try {
			return APIResult.ok(userInfoService.selectById(id));
		} catch (Exception e) {
			e.printStackTrace();
			return APIResult.notFound("获取用户失败: " + e.getMessage());
		}
	}

	@PostMapping("/update")
	public APIResult edit(@RequestBody UserInfo userInfo) {
		return APIResult.ok(userInfoService.edit(userInfo));
	}
	@PostMapping
	public APIResult updatestatus(UserInfo userInfo) {
		return APIResult.ok(userInfoService.edit(userInfo));
	}
	
	@GetMapping("/stats")
	public APIResult getUserStats(Integer id) {
		UserInfo user = userInfoService.selectById(id);
		if (user == null) {
			return APIResult.notFound("用户不存在");
		}
		return APIResult.ok(user);
	}
	
	@GetMapping("/rankings")
	public APIResult getRankings(String type) {
		return APIResult.ok(userInfoService.getRankings(type));
	}

	@GetMapping("/byusername")
	public APIResult byUsername(String username) {
		if (username == null || username.isEmpty()) {
			return APIResult.notFound("用户名不能为空");
		}
		return APIResult.ok(userInfoService.selectByUsername(username));
	}

	@GetMapping("/bot")
	public APIResult getBotUser() {
		UserInfo bot = userInfoService.selectByUsername("智能助手");
		if (bot == null) {
			UserInfo newBot = new UserInfo();
			newBot.setUsername("智能助手");
			newBot.setPassword("bot123456");
			newBot.setNickname("校园小助手");
			newBot.setRealname("智能助手");
			newBot.setLevel(99);
			newBot.setHead("default_avatar.png");
			try {
				bot = userInfoService.register(newBot);
			} catch (Exception e) {
				return APIResult.notFound("创建机器人用户失败: " + e.getMessage());
			}
		}
		return APIResult.ok(bot);
	}
}
