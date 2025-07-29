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

    @Value("${jwt.access-exp-time}")
    private long ACCESS_EXP_TIME;

    @Value("${jwt.refresh-exp-time}")
    private long REFRESH_EXP_TIME;

    //access token 발급
    public String generateAccessToken(User user) {
        return Jwts.builder() //jwt 빌더로 토큰 생성
                .setSubject(user.getIdentify()) //id
                .claim("username", user.getUsername())
                .claim("type", "access") //access 타입 명시
                .setIssuedAt(new Date()) //생성 시점
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXP_TIME)) //만료 시점
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256) //서명
                .compact();
    }
    //refresh token 발급
    public String generateRefreshToken(User user) {
//        유효 시간을 다음 날 오전 9시로 설정하는 코드
//        TimeZone seoulTz = TimeZone.getTimeZone("Asia/Seoul");
//        Calendar calendar = Calendar.getInstance(seoulTz);
//
//        calendar.set(Calendar.HOUR_OF_DAY, 9);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//
//        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
//            calendar.add(Calendar.DATE, 1);
//        }
        return Jwts.builder()
                .setSubject(user.getIdentify())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXP_TIME))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    //액세스 토큰 유효 검사 메서드
    public TokenStatus validateToken(String token) {
        try {//token 파싱
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            //type이 access가 아니면, 에러
            String type = claims.get("type", String.class);
            if (!"access".equals(type)) {
                return TokenStatus.INVALID;
            }
            return TokenStatus.AUTHENTICATED;
        } catch (ExpiredJwtException e) {//유효 시간 지남
            return TokenStatus.EXPIRED;
        } catch (JwtException e) {//서명 위조, 잘못된 형식
            return TokenStatus.INVALID;
        }
    }

    //리프레시 토큰 유효성 검사
    public TokenStatus validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            //type이 refresh가 아니면 에러
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

    //액세스 토큰에서 sub 추출
    public String extractIdentify(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    //리프레시 토큰에서 sub 추출
    public String extractIdentifyFromRefresh(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}