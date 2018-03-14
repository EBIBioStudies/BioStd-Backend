package uk.ac.ebi.biostd.webapp.application.security.rest;

import static uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType.ATTACH;

import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import uk.ac.ebi.biostd.webapp.application.security.entities.ChangePasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.LoginRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.ResetPasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignInRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ActivationResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.LoginResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.PassRequestResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ProjectsDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ResetPassResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.SignUpResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.SignoutRequestDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.SignoutResponseDto;
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
    public @ResponseBody ProjectsDto getProjects(@AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {
        return projectMapper.getProjectsDto(submissionService.getAllowedProjects(user.getId(), ATTACH));
    }

    @PostMapping(value = "/checkAccess")
    public ResponseEntity<String> getPermissions(@ModelAttribute LoginRequest loginInfo) {
        Map<String, String> permissions = permissionMapper.getPermissionMap(securityService.getPermissions(loginInfo));
        return ResponseEntity.ok()
                .header("produces", MediaType.TEXT_PLAIN_VALUE)
                .body(PlainFileFormat.asPlainFile(permissions));
    }

    @GetMapping(value = "/auth/check")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody Map<String, String> getPermissions(@AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {
        Map<String, String> permissions = permissionMapper.getPermissionMap(securityService.getUser(user.getId()));
        return permissions;
    }

    @GetMapping(value = "/auth/signin")
    public ResponseEntity<String> signIn(@ModelAttribute SignInRequest signInRequest) {
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
    public @ResponseBody LoginResponseDto sign(@RequestBody SignInRequest signInRequest, HttpServletResponse response) {
        UserData userData = securityService.signIn(signInRequest.getLogin(), signInRequest.getPassword());
        response.addCookie(new Cookie(SECURITY_COOKIE_NAME, userData.getToken()));
        return permissionMapper.getLoginResponse(userData);
    }


    @PostMapping(value = "/auth/signout")
    public @ResponseBody SignoutResponseDto signOut(@RequestBody SignoutRequestDto signoutRequest,
            HttpServletResponse response) {
        securityService.signOut(signoutRequest.getSessid());
        response.addCookie(WebUtils.newExpiredCookie(SECURITY_COOKIE_NAME));

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
