package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.entity.User;
import by.bsuir.academicauditsystemgateway.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {

    private JwtService jwtService;

    @Value("${jwt.secret}")
    private String secret;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        jwtService.setSecret(secret);
        jwtService.setExpiration(3600000L);
    }

    @Test
    void testGenerateAndValidateToken() {
        Long userId = 1L;
        String username = "testuser";
        String password = "testpassword";
        UserRole userRole = UserRole.ROLE_ADMIN;

        String token = jwtService.generateToken(new User(userId, username, password, userRole));

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, new User(userId, username, password, userRole)));
    }

    @Test
    void testExtractClaims() {
        Long userId = 1L;
        String username = "testuser";
        String password = "testpassword";
        UserRole userRole = UserRole.ROLE_ADMIN;
        String token = jwtService.generateToken(new User(userId, username, password, userRole));

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testuser", claims.getSubject());
        assertEquals(UserRole.ROLE_ADMIN.toString(), claims.get("role"));
    }
}
