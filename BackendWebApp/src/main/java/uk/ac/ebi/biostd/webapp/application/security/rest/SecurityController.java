package uk.ac.ebi.biostd.webapp.application.security.rest;

import static uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType.ATTACH;
import static uk.ac.ebi.biostd.webapp.application.security.rest.SecurityFilter.COOKIE_NAME;
import static uk.ac.ebi.biostd.webapp.application.security.rest.SecurityFilter.ENV_VAR;

import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.biostd.webapp.application.common.utils.PlainFileFormat;
import uk.ac.ebi.biostd.webapp.application.common.utils.WebUtils;
import uk.ac.ebi.biostd.webapp.application.configuration.ConfigProperties;
import uk.ac.ebi.biostd.webapp.application.security.RetryActivationRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.ChangePasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.LoginRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.ResetPasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ActivationResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.LoginResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.PassRequestResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.PermissionDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ProjectsDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ResetPassResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.SignInRequestDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.SignUpResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.SignoutRequestDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.SignoutResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.mappers.PermissionMapper;
import uk.ac.ebi.biostd.webapp.application.security.rest.mappers.ProjectMapper;
import uk.ac.ebi.biostd.webapp.application.security.rest.model.UserData;
import uk.ac.ebi.biostd.webapp.application.security.service.ISecurityService;
import uk.ac.ebi.biostd.webapp.application.submission.ISubmissionService;

@Controller
public class SecurityController {

    private final String cookieName;
    private final ProjectMapper projectMapper;
    private final ISecurityService securityService;
    private final ISubmissionService submissionService;

    private final PermissionMapper permissionMapper;

    public SecurityController(ConfigProperties config, ProjectMapper projectMapper,
            ISecurityService securityService, ISubmissionService submissionService, PermissionMapper permissionMapper) {
        this.projectMapper = projectMapper;
        this.securityService = securityService;
        this.submissionService = submissionService;
        this.permissionMapper = permissionMapper;
        this.cookieName = COOKIE_NAME + "-" + config.get(ENV_VAR);
    }

    @GetMapping("/atthost")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody ProjectsDto getProjects(@AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {
        return projectMapper.getProjectsDto(submissionService.getAllowedProjects(user.getId(), ATTACH));
    }

    /**
     * Deprecated in favor of {@link SecurityController#checkAccess(LoginRequest)}
     */
    @PostMapping(value = "/checkAccess")
    @Deprecated
    public ResponseEntity<String> getPermissions(@ModelAttribute LoginRequest loginInfo) {
        Map<String, String> permissions = permissionMapper.getPermissionMap(securityService.getUser(loginInfo));
        return ResponseEntity.ok()
                .header("produces", MediaType.TEXT_PLAIN_VALUE)
                .body(PlainFileFormat.asPlainFile(permissions));
    }

    @PostMapping(value = "/auth/check-access")
    public PermissionDto checkAccess(@ModelAttribute LoginRequest loginInfo) {
        return permissionMapper.getPermissionDto(securityService.getUser(loginInfo));
    }

    @PostMapping(value = "/auth/retryact")
    public ResponseEntity<Void> retryActivation(@RequestBody RetryActivationRequest loginInfo) {
        securityService.retryActivation(loginInfo.getEmail(), loginInfo.getActivationURL());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/auth/check")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody LoginResponseDto getPermissions(Authentication authentication) {
        uk.ac.ebi.biostd.authz.User user = (uk.ac.ebi.biostd.authz.User) authentication.getPrincipal();
        String token = (String) authentication.getCredentials();
        return permissionMapper.getLoginResponse(new UserData(token, securityService.getUser(user.getId())));
    }

    /**
     * Used only by terminal Submit application, deprecated in favor of
     * {@link SecurityController#signIn(SignInRequestDto, HttpServletResponse)}
     */
    @Deprecated
    @GetMapping(value = "/auth/signin")
    public ResponseEntity<String> signIn(@ModelAttribute SignInRequestDto signInRequest) {
        UserData userData = securityService.signIn(signInRequest.getLogin(), signInRequest.getPassword());

        String response = "OK" + "\n"
                + "sessid" + ": " + userData.getToken() + "\n"
                + "username" + ": " + userData.getUser().getFullName() + "\n"
                + "email" + ": " + userData.getUser().getEmail();
        return ResponseEntity.ok()
                .header("produces", MediaType.TEXT_PLAIN_VALUE)
                .body(response);
    }

    @PostMapping(value = "/auth/signin")
    public @ResponseBody LoginResponseDto signIn(
            @RequestBody SignInRequestDto signInRequest, HttpServletResponse response) {
        UserData userData = securityService.signIn(signInRequest.getLogin(), signInRequest.getPassword());
        response.addCookie(new Cookie(cookieName, userData.getToken()));
        return permissionMapper.getLoginResponse(userData);
    }

    @PostMapping(value = "/auth/signout")
    public @ResponseBody SignoutResponseDto signOut(@RequestBody SignoutRequestDto signoutRequest,
            HttpServletResponse response) {
        securityService.signOut(signoutRequest.getSessid());
        response.addCookie(WebUtils.newExpiredCookie(cookieName));

        return new SignoutResponseDto("User logged out", "OK");
    }

    @PostMapping(value = "/auth/signup")
    public @ResponseBody SignUpResponseDto signUp(@RequestBody SignUpRequest signUpRequest) {
        securityService.addUser(signUpRequest);
        return new SignUpResponseDto(signUpRequest.getUsername(), "OK");
    }

    @PostMapping(value = "/auth/activate/{activationKey}")
    public @ResponseBody ActivationResponseDto activate(@PathVariable String activationKey) {
        securityService.activate(activationKey);
        return new ActivationResponseDto("User successfully activated. You can log in now", "OK");
    }

    @PostMapping(value = "/auth/passreset")
    public @ResponseBody ResetPassResponseDto resetPassword(@RequestBody ChangePasswordRequest request) {
        securityService.resetPassword(request.getKey(), request.getPassword());
        return new ResetPassResponseDto("OK");
    }

    @PostMapping(value = "/auth/passrstreq")
    public @ResponseBody PassRequestResponseDto resetPassword(@RequestBody ResetPasswordRequest request) {
        securityService.resetPasswordRequest(request.getEmail(), request.getResetURL());
        return new PassRequestResponseDto("OK");
    }
}
