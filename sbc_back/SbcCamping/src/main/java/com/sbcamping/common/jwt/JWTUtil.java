package com.sbcamping.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JWTUtil {

    // 암호키
    private static final String key = "**********";

    // 토큰 생성
    public static String generateToken(Map<String, Object> valueMap, int min) {
        SecretKey key;
        try {
            key = Keys.hmacShaKeyFor(JWTUtil.key.getBytes("UTF-8"));
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

        return Jwts.builder()
                .setHeader(Map.of("typ", "JWT"))
                .setClaims(valueMap) // memberDTO
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(key).compact();

    }

    // 토큰 검증
    public static Map<String, Object> validateToken(String token) {
        Map<String, Object> claim;
        try {
            // JWT 서명 검증을 위한 비밀 키 생성
            SecretKey key = Keys.hmacShaKeyFor(JWTUtil.key.getBytes("UTF-8"));
            // 파싱 및 검증 실패시 에러
            claim = Jwts.parserBuilder()
                    .setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody(); // 토큰에 포함된 클레임 추출
            //log.info("--------validateToken claim : {}", claim);
        } catch (MalformedJwtException e) { // 전달되는 토큰의 값이 유효하지 않을 때 발생
            throw new CustomJWTException("MalFormed");
        } catch (ExpiredJwtException e) { // 유효기간 초과
            throw new CustomJWTException("Expired");
        } catch (InvalidClaimException e) { // 클레임이 유효하지 않음
            throw new CustomJWTException("Invalid");
        } catch (JwtException e){
            throw new CustomJWTException("JWT 사용자 정의 예외");
        } catch (Exception e) {
            throw new CustomJWTException("JWT ERROR");
        }
        return claim;
    }

}
