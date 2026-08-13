package com.henry.forum.admin.util;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class TokenUtil {
	private static String privateKey = "J0eXAi0iKV1QiLCJThbGcioiIU";
	
	private static final String ISSUER = "skg";

	public static class Token {
		private String code;
		private long timestamp;
		private Integer status = 0;
		public Token() {}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		public Integer getStatus() {
			return status;
		}

		public void setStatus(Integer status) {
			this.status = status;
		}

	}

	/**
	 * 生成token
	 */

	public static String createToken(String code) {
		// 设置更长的过期时间：7天 (7 * 24 * 60 * 60 * 1000)
		Date expire = new Date(new Date().getTime() + 7*24*60*60 * 1000);
		Token token = new Token();
		token.setCode(code);
		token.setTimestamp(System.currentTimeMillis());
		Algorithm algorithm = Algorithm.HMAC256(privateKey);
		JWTCreator.Builder builder = 
				JWT.create()
				.withClaim("code", token.getCode())
				.withClaim("timestamp", token.getTimestamp())
				.withIssuer(ISSUER)
				.withExpiresAt(expire);
		return builder.sign(algorithm);
	}

	/*
	 * 验证token是否过期
	 */
	public static Token parseToken(String strtoken) {
		Token token=new Token();
		// 设置签名的加密算法：HMAC256
		try {
			Algorithm algorithm = Algorithm.HMAC256("J0eXAi0iKV1QiLCJThbGcioiIU");
			JWTVerifier verifier = JWT.require(algorithm).build(); // Reusable verifier instance
			DecodedJWT jwt = verifier.verify(strtoken);
			return token;
		} 
        catch (Exception e) {
			e.printStackTrace();
			token.setStatus(1);
			return token;
		}
		
	}

	/**
	 * 解析Token
	 */
	public static String getcode(String strtoken) {
		
		try {
			DecodedJWT jwt = JWT.decode(strtoken);
			return jwt.getClaim("code").asString();
		} catch (JWTDecodeException e) {
			e.printStackTrace();
		}
		return null;
	}

}
