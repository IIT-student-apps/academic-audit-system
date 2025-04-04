package by.bsuir.academicauditsystemgateway.service;


import by.bsuir.academicauditsystemgateway.entity.User;
import by.bsuir.academicauditsystemgateway.entity.UserRole;
import by.bsuir.academicauditsystemgateway.utils.JwtClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret; // key in base64 format

    @Value("${jwt.expiration}")
    private Long expiration;

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractPassword(String token) {
        return extractClaim(token, claims -> claims.get(JwtClaims.USER_PASSWORD_CLAIM_NAME, String.class));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(JwtClaims.USER_ID_CLAIM_NAME, Long.class));
    }

    public UserRole extractRole(String token) {
        return UserRole.valueOf(extractClaim(token, claims -> claims.get(JwtClaims.USER_ROLE_CLAIM_NAME, String.class)));
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User customUserDetails) {
            claims.put(JwtClaims.USER_ID_CLAIM_NAME, customUserDetails.getId());
            // not sure, that it's secure, did it only for when user changes his password and previous token should be not actual
            claims.put(JwtClaims.USER_PASSWORD_CLAIM_NAME, customUserDetails.getPassword());
            claims.put(JwtClaims.USER_ROLE_CLAIM_NAME, customUserDetails.getRole());
        }
        return generateToken(claims, userDetails);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        final String password = extractPassword(token);
        final String userRole = extractRole(token).toString();
        return userName.equals(userDetails.getUsername()) &&
                password.equals(userDetails.getPassword()) &&
                userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.toString().equals(userRole)) &&
                !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        return Jwts.parserBuilder().
                setSigningKey(key).build().
                parseClaimsJws(token).
                getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
