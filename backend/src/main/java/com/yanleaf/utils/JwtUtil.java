package com.yanleaf.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
	private final SecretKey key;

	@Value ("${jwt.expire-ms}")
	private final long expireMs;

	public JwtUtil(@Value("${jwt.secret}") String secret,
				   @Value("${jwt.expire-ms}") long expireMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expireMs = expireMs;
	}


	/**
	 * 用户登录用：role 默认 USER
	 */
	public String generateToken (Long userId, String username) {
		return generateToken (userId, username, "USER");
	}

	/**
	 * 管理员登录用：role = ADMIN
	 */
	public String generateToken (Long userId, String username, String role) {
		return Jwts.builder ()
				.setSubject (String.valueOf (userId))
				.claim ("username", username)
				.claim ("role", role)
				.setIssuedAt (new Date ())
				.setExpiration (new Date (System.currentTimeMillis () + expireMs))
				.signWith (key, SignatureAlgorithm.HS256)
				.compact ();
	}

	public String getRole (String token) {
		Claims claims = Jwts.parserBuilder ()
				.setSigningKey (key)
				.build ()
				.parseClaimsJws (token)
				.getBody ();
		return claims.get ("role", String.class);
	}

	public Long getUserId (String token) {
		Claims claims = Jwts.parserBuilder ()
				.setSigningKey (key)
				.build ()
				.parseClaimsJws (token)
				.getBody ();
		return Long.valueOf (claims.getSubject ());
	}

	public boolean validate (String token) {
		try {
			Jwts.parserBuilder ().setSigningKey (key).build ().parseClaimsJws (token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}