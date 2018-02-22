package uk.ac.ebi.biostd.webapp.application.security.rest;

import static uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType.SUBMIT;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.biostd.webapp.application.common.utils.PlainFileFormat;
import uk.ac.ebi.biostd.webapp.application.common.utils.WebUtils;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.security.common.ISecurityService;
import uk.ac.ebi.biostd.webapp.application.security.entities.ActivateRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.LoginRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.ResetPasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignInRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ProjectsDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.mappers.PermissionMapper;
import uk.ac.ebi.biostd.webapp.application.security.rest.mappers.ProjectMapper;

@AllArgsConstructor
@Controller
public class SecurityController {

    private static final String SECURITY_COOKIE_NAME = "BIOSTDSESS";

    private final ProjectMapper projectMapper;
    private final ISecurityService securityService;
    private final PermissionMapper permissionMapper;

    @GetMapping("/atthost")
    @PreAuthorize("isAuthenticated()")
    public ProjectsDto getProjects(@AuthenticationPrincipal User user) {
        return projectMapper.getProjectsDto(securityService.getAllowedProjects(user.getId(), SUBMIT));
    }

    @PostMapping(value = "/auth/checkAccess", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getPermissions(@ModelAttribute LoginRequest loginInfo) {
        Map<String, String> permissions = permissionMapper.getPermissionMap(securityService.getPermissions(loginInfo));
        return PlainFileFormat.asPlainFile(permissions);
    }

    @PostMapping(value = "/auth/signin")
    public void signIn(@ModelAttribute SignInRequest signInRequestDto, HttpServletResponse response) {
        String token = securityService.signIn(signInRequestDto.getLogin(), signInRequestDto.getPassword());
        response.addCookie(new Cookie(SECURITY_COOKIE_NAME, token));
    }

    @PostMapping(value = "/auth/signout")
    public void signOut(@RequestParam("sessid") String securityKey, HttpServletResponse response) {
        securityService.signOut(securityKey);
        response.addCookie(WebUtils.newExpiredCookie(SECURITY_COOKIE_NAME));
    }

    @PostMapping(value = "/auth/signup")
    public void signUp(@RequestBody SignUpRequest signUpRequest) {
        User user = User.builder()
                .email(signUpRequest.getEmail())
                .fullName(signUpRequest.getUsername())
                .auxProfileInfo(signUpRequest.getAux()).build();
        securityService.addUser(user, signUpRequest.getActivationURL());
    }

    @PostMapping(value = "/auth/activate/{activationKey}")
    public void activate(
            @ModelAttribute ActivateRequest activateRequest,
            @PathVariable String activationKey,
            HttpServletResponse response) throws IOException {
        String redirectUrl = securityService.activate(activationKey) ?
                activateRequest.getSuccessURL() : activateRequest.getFailURL();
        response.sendRedirect(redirectUrl);
    }

    @PostMapping(value = "/auth/passreset")
    public void resetPassword(@ModelAttribute ResetPasswordRequest request, HttpServletResponse response)
            throws IOException {
        boolean passwordReset = securityService.resetPassword(request.getKey(), request.getPassword());
        String redirectUrl = passwordReset ? request.getSuccessURL() : request.getFailURL();
        response.sendRedirect(redirectUrl);
    }
}
