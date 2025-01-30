package org.advisor.member.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.advisor.global.exceptions.UnAuthorizedException;
import org.advisor.global.libs.Utils;
import org.advisor.member.MemberInfo;
import org.advisor.member.services.MemberInfoService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private final JwtProperties properties;
    private final MemberInfoService infoService;
    private final Utils utils;

    private Key key;

    // 생성자에서 JwtProperties, MemberInfoService, Utils 의존성 주입
    public TokenService(JwtProperties properties, MemberInfoService infoService, Utils utils) {
        this.properties = properties;
        this.infoService = infoService;
        this.utils = utils;

        // JWT secret key 설정
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 토큰 생성
     * @param email
     * @return
     */
    public String create(String email) {
        MemberInfo memberInfo = (MemberInfo) infoService.loadUserByUsername(email);

        // 권한을 문자열로 연결하여 토큰에 포함
        String authorities = memberInfo.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.joining("||"));

        int validTime = properties.getValidTime() * 1000;
        Date expirationDate = new Date(System.currentTimeMillis() + validTime); // 토큰 만료 시간 설정

        return Jwts.builder()
                .setSubject(memberInfo.getEmail())
                .claim("authorities", authorities)
                .claim("username", memberInfo.getUsername()) // 예시: 사용자 이름 저장
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(expirationDate)
                .compact();
    }

    /**
     * 토큰으로 인증 처리(로그인 처리)
     * @param token
     * @return
     */
    public Authentication authenticate(String token) {
        // 토큰 유효성 검사
        validate(token);

        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody(); // payload를 가져옵니다.

        String email = claims.getSubject();
        String authorities = (String) claims.get("authorities");

        List<SimpleGrantedAuthority> _authorities = Arrays.stream(authorities.split("\\|\\|"))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // memberInfo에 권한 세팅
        MemberInfo memberInfo = (MemberInfo) infoService.loadUserByUsername(email);
        memberInfo.setAuthorities(_authorities);

        // 인증 객체 생성 후, SecurityContext에 설정
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                memberInfo, null, _authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication); // 로그인 처리

        return authentication;
    }

    /**
     * HTTP 요청에서 Authorization 헤더를 사용해 인증 처리
     * @param request
     * @return
     */
    public Authentication authenticate(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader)) {
            return null; // 인증 헤더가 없으면 인증을 건너뛰고 null 반환
        }

        String token = authHeader.substring(7); // "Bearer " 부분 제거

        return authenticate(token);
    }

    /**
     * JWT 토큰 검증
     * @param token
     */
    public void validate(String token) {
        String errorCode = null;
        Exception error = null;
        try {
            // 토큰 유효성 검사
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            errorCode = "JWT.malformed";
            error = e;
        } catch (ExpiredJwtException e) {
            errorCode = "JWT.expired";
            error = e;
        } catch (UnsupportedJwtException e) {
            errorCode = "JWT.unsupported";
            error = e;
        } catch (Exception e) {
            errorCode = "JWT.error";
            error = e;
        }

        if (StringUtils.hasText(errorCode)) {
            // errorCode에 해당하는 메시지 반환
            throw new UnAuthorizedException(utils.getMessage(errorCode));
        }

        if (error != null) {
            error.printStackTrace();  // 예외 발생 시 로그 출력
        }
    }

    /**
     * Refresh Token을 사용하여 새로운 Access Token 생성 (Optional)
     * @param refreshToken
     * @return
     */
    public String refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        validate(refreshToken);

        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        String email = claims.getSubject();

        // 기존의 email과 권한 정보를 기반으로 새로운 Access Token 생성
        return create(email);
    }

    /**
     * JWT의 유효성 및 만료 여부를 확인하는 추가적인 유틸리티 메서드
     * @param token
     * @return true if valid, false if expired or invalid
     */
    public boolean isValidToken(String token) {
        try {
            validate(token);  // validate 메서드 호출로 토큰을 검증
            return true;
        } catch (UnAuthorizedException e) {
            return false;
        }
    }
}
