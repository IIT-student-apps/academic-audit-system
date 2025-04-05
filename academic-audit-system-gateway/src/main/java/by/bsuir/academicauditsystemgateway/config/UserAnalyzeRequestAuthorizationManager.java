package by.bsuir.academicauditsystemgateway.config;

import by.bsuir.academicauditsystemgateway.entity.UserRole;
import by.bsuir.academicauditsystemgateway.service.DocumentAnalyzeRequestService;
import by.bsuir.academicauditsystemgateway.utils.RequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Component
public class UserAnalyzeRequestAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private static final short RESOURCE_ID_QUERY_POSITION = 4;

    private final DocumentAnalyzeRequestService analyzeRequestService;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        UUID userDataId = UUID.fromString(request.getRequestURI().split("/")[RESOURCE_ID_QUERY_POSITION]);
        Long userId = (Long) context.getRequest().getAttribute(RequestAttributes.USER_ID);
        boolean isGranted = analyzeRequestService.getRequestDtoById(userDataId).getUserId().equals(userId) ||
                request.getAttribute(RequestAttributes.USER_ROLE).equals(UserRole.ROLE_ADMIN);

        return new AuthorizationDecision(isGranted);
    }
}
