package com.cdl.basicauth.config;import com.cdl.basicauth.data.model.CustomAppUser;import io.jsonwebtoken.Claims;import io.jsonwebtoken.Jwts;import io.jsonwebtoken.SignatureAlgorithm;import lombok.Data;import org.springframework.security.core.userdetails.UserDetails;import org.springframework.stereotype.Component;import java.util.Date;import java.util.HashMap;import java.util.Map;import java.util.concurrent.TimeUnit;import java.util.function.Function;@Componentpublic class JwtUtils {    private String jwtSigninKey ="secret";    public String extractUsername(String token){        return extractClaim(token, Claims::getSubject);    }    public Date extractExpiration(String token){ return extractClaim(token, Claims::getExpiration);}    public boolean hasClaim(String token, String claimName){        final Claims claims = extractAllClaims(token);        return claims.get(claimName) != null;    }    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){        final Claims claims = extractAllClaims(token);        return claimsResolver.apply(claims);    }    private Claims extractAllClaims(String token ){        return Jwts.parser().setSigningKey(jwtSigninKey).parseClaimsJws(token).getBody();    }    private Boolean isTokenExpired(String token){        return extractExpiration(token).before(new Date());    }    public String generateToken(UserDetails customAppUser){        Map<String, Object>claims= new HashMap<>();        return createToken(claims,customAppUser);    }    public String generateToken(CustomAppUser customAppUser, Map<String, Object>claims){return createToken(claims, customAppUser);}    public String createToken(Map<String, Object> claims, UserDetails customAppUser){        return Jwts.builder().setClaims(claims)                .setSubject(customAppUser.getUsername())                .claim("authorities", customAppUser.getAuthorities())                .setIssuedAt(new Date(System.currentTimeMillis()))                .setExpiration(new Date(System.currentTimeMillis()+ TimeUnit.HOURS.toMillis(24)))                .signWith(SignatureAlgorithm.HS256, jwtSigninKey).compact();    }    public boolean isTokenValid(String token, UserDetails userDetails){        final String username = extractUsername(token);        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));    }}