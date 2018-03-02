package uk.ac.ebi.biostd.webapp.application.security.rest;

import static uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType.ATTACH;

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
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.common.utils.PlainFileFormat;
import uk.ac.ebi.biostd.webapp.application.common.utils.WebUtils;
import uk.ac.ebi.biostd.webapp.application.security.entities.ChangePasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.LoginRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.ResetPasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignInRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.LoginResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ProjectsDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.mappers.PermissionMapper;
import uk.ac.ebi.biostd.webapp.application.security.rest.mappers.ProjectMapper;
import uk.ac.ebi.biostd.webapp.application.security.rest.model.UserData;
import uk.ac.ebi.biostd.webapp.application.security.service.ISecurityService;
import uk.ac.ebi.biostd.webapp.application.submission.ISubmissionService;

@AllArgsConstructor
@Controller
public class SecurityController {

    private static final String SECURITY_COOKIE_NAME = "BIOSTDSESS";

    private final ProjectMapper projectMapper;
    private final ISecurityService securityService;
    private final ISubmissionService submissionService;

    private final PermissionMapper permissionMapper;

    @GetMapping("/atthost")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody ProjectsDto getProjects(@AuthenticationPrincipal User user) {
        return projectMapper.getProjectsDto(submissionService.getAllowedProjects(user.getId(), ATTACH));
    }

    @PostMapping(value = "/auth/checkAccess", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getPermissions(@ModelAttribute LoginRequest loginInfo) {
        Map<String, String> permissions = permissionMapper.getPermissionMap(securityService.getPermissions(loginInfo));
        return PlainFileFormat.asPlainFile(permissions);
    }

    @PostMapping(value = "/auth/signin")
    public @ResponseBody LoginResponseDto sign(@RequestBody SignInRequest request, HttpServletResponse response) {
        UserData userData = securityService.signIn(request.getLogin(), request.getPassword());
        response.addCookie(new Cookie(SECURITY_COOKIE_NAME, userData.getToken()));
        return permissionMapper.getLoginResponse(userData);
    }

    @PostMapping(value = "/auth/signout")
    public @ResponseBody void signOut(@RequestParam("sessid") String securityKey, HttpServletResponse response) {
        securityService.signOut(securityKey);
        response.addCookie(WebUtils.newExpiredCookie(SECURITY_COOKIE_NAME));
    }

    @PostMapping(value = "/auth/signup")
    public @ResponseBody void signUp(@RequestBody SignUpRequest signUpRequest) {
        securityService.addUser(signUpRequest);
    }

    @PostMapping(value = "/auth/activate/{activationKey}")
    public @ResponseBody void activate(@PathVariable String activationKey) {
        securityService.activate(activationKey);
    }

    @PostMapping(value = "/auth/passreset")
    public @ResponseBody void resetPassword(@RequestBody ChangePasswordRequest request) {
        securityService.resetPassword(request.getKey(), request.getPassword());
    }

    @PostMapping(value = "/auth/passrstreq")
    public @ResponseBody void resetPassword(@RequestBody ResetPasswordRequest request) {
        securityService.resetPasswordRequest(request.getEmail(), request.getResetURL());
    }
}
