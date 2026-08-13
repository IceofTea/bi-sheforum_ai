package com.henry.forum.admin.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.util.TokenUtil;
import com.henry.forum.admin.util.VercodeUtil;


@Controller
//@RequestMapping("/api")
@CrossOrigin(origins = {"*"})
public class VercodeController {
	/**
	 * 返回一张验证码图片
	 * @throws IOException 
	 */
	@GetMapping("/vercode")
	@ResponseBody
	public APIResult vercode(HttpServletResponse response) {
		VercodeUtil vercode = new VercodeUtil();
		String vercodeString= vercode.getRandomString(4);
//		2.使用token+jwt
		//将字符串转化成token
		String strtoken= TokenUtil.createToken(vercodeString);
		APIResult apiResult=new APIResult();
		apiResult.setStatus(200);
		apiResult.setMsg(strtoken); 	
	 	return apiResult;
	}
	@GetMapping("/vercode/{token}")
	public void vercode(@PathVariable("token") String strToken,HttpServletResponse response) {
		//验证token是否过期
		TokenUtil.parseToken(strToken);
		//解析token成字符串
		String vercodesString = TokenUtil.getcode(strToken);
		
		VercodeUtil vercode = new VercodeUtil();
		vercode.getRandcode(response,vercodesString);

	}
}
