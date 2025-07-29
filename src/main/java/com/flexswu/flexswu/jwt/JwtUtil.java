package com.flexswu.flexswu.jwt;

import com.flexswu.flexswu.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Component
public class JwtUtil {
    @Value("${JWT_SECRET}")
    private String SECRET_KEY;

    private final long ACCESS_EXP_TIME = 1000L * 60 * 60;  // 1시간
    //private final long ACCESS_EXP_TIME = 1000L * 60 * 10;  // 10분
    //private final long REFRESH_EXP_TIME = 1000L * 60 * 60 * 24;  // 24시간

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getIdentify())  // 학번
                .claim("username", user.getUsername())
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXP_TIME))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        TimeZone seoulTz = TimeZone.getTimeZone("Asia/Seoul");
        Calendar calendar = Calendar.getInstance(seoulTz);

        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }

        return Jwts.builder()
                .setSubject(user.getIdentify())  // 학번
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(calendar.getTime())
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }



    public TokenStatus validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String type = claims.get("type", String.class);
            if (!"access".equals(type)) {
                return TokenStatus.INVALID;
            }

            return TokenStatus.AUTHENTICATED;
        } catch (ExpiredJwtException e) {
            return TokenStatus.EXPIRED;
        } catch (JwtException e) {
            return TokenStatus.INVALID;
        }
    }


    public TokenStatus validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                return TokenStatus.INVALID;
            }

            return TokenStatus.AUTHENTICATED;
        } catch (ExpiredJwtException e) {
            return TokenStatus.EXPIRED;
        } catch (JwtException e) {
            return TokenStatus.INVALID;
        }
    }


    public String extractIdentify(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractIdentifyFromRefresh(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}