package com.flexswu.flexswu.jwt;

import com.flexswu.flexswu.entity.User;
import com.flexswu.flexswu.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 로그인, 회원가입, 액세스 토큰 재발급은 필터 건너뜀
        return  path.startsWith("/user/login") ||
                path.startsWith("/user/signup") ||
                path.startsWith("/user/refresh") ||
                path.startsWith("/user/test");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        //Bearer 필수
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            //토큰 유효성 검사 / access 토큰인지, 유효한지 만료되지 않았는지 검사
            if (jwtUtil.validateToken(token) == TokenStatus.AUTHENTICATED) {
                String identify = jwtUtil.extractIdentify(token);
                User user = userRepository.findByIdentify(identify).orElse(null);
                //인증 객체 생성 + security에 사용자 인증 등록
                if (user != null) {
                    CustomUserDetails userDetails = new CustomUserDetails(
                            user.getId(),
                            user.getIdentify(),
                            user.getUsername(),
                            user.getPassword()
                    );
                    //인증 객체 생성
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);//인증 완료
                }
            }
        }
        //다음 필터로 넘김
        filterChain.doFilter(request, response);

    }


}