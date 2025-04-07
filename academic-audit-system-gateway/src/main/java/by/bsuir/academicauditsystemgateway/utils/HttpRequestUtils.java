package by.bsuir.academicauditsystemgateway.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class HttpRequestUtils {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    public static final String EMPTY_STR = "";

    public static String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HEADER_NAME);
        return (!StringUtils.isEmpty(authHeader) &&
                StringUtils.startsWithIgnoreCase(authHeader, BEARER_PREFIX)) ?
                authHeader.substring(BEARER_PREFIX.length()) : EMPTY_STR;
    }

}
