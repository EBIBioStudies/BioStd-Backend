package uk.ac.ebi.biostd.webapp.application.security.rest.mappers;

import static uk.ac.ebi.biostd.webapp.application.security.service.SecurityService.PUBLIC_ACCESS_TAG;

import com.google.common.base.MoreObjects;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.commons.files.MagicFolderUtil;
import uk.ac.ebi.biostd.webapp.application.persitence.common.AuxInfo;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessTag;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.AuxInfoDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.LoginResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.PermissionDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.model.UserData;

@Component
@AllArgsConstructor
public class PermissionMapper {

    private static final String STATUS_OK = "OK";
    private final MagicFolderUtil magicFolderUtil;

    public Map<String, String> getPermissionMap(User user) {
        Map<String, String> accessInfo = new LinkedHashMap<>(7);
        accessInfo.put("Status", STATUS_OK);
        accessInfo.put("Allow", getUserAllows(user));
        accessInfo.put("Deny", StringUtils.EMPTY);
        accessInfo.put("Superuser", String.valueOf(user.isSuperuser()));
        accessInfo.put("Name", user.getFullName());
        accessInfo.put("Login", MoreObjects.firstNonNull(user.getLogin(), StringUtils.EMPTY));
        accessInfo.put("EMail", user.getEmail());
        return accessInfo;
    }

    public PermissionDto getPermissionDto(User user) {
        return PermissionDto.builder()
                .status(STATUS_OK)
                .allow(getUserAllows(user))
                .deny(StringUtils.EMPTY)
                .superuser(user.isSuperuser())
                .name(user.getFullName())
                .login(user.getLogin())
                .email(user.getEmail())
                .build();
    }

    private String getUserAllows(User user) {
        List<String> accessTags = user.getAccessPermissions()
                .stream()
                .filter(permission -> permission.getAccessType().equals(AccessType.READ))
                .map(AccessPermission::getAccessTag)
                .map(AccessTag::getName)
                .collect(Collectors.toList());
        accessTags.add(PUBLIC_ACCESS_TAG);
        return "~" + user.getEmail() + ";#" + user.getId() + ";" + String.join(";", accessTags);
    }

    public LoginResponseDto getLoginResponse(UserData userData) {
        LoginResponseDto loginResponse = new LoginResponseDto();
        User user = userData.getUser();

        loginResponse.setEmail(user.getEmail());
        loginResponse.setSuperuser(String.valueOf(user.isSuperuser()));
        loginResponse.setStatus(STATUS_OK);
        loginResponse.setUsername(user.getFullName());
        loginResponse.setSessid(userData.getToken());
        loginResponse.setAux(getAuxInfo(user.getAuxProfileInfo()));
        loginResponse.setSecret(magicFolderUtil.getUserMagicFolderRelativePath(user.getId(),user.getSecret()).toString());

        return loginResponse;
    }

    private AuxInfoDto getAuxInfo(AuxInfo auxProfileInfo) {
        AuxInfoDto auxInfoDto = new AuxInfoDto();
        auxInfoDto.setOrcid(auxProfileInfo.getOrcid());
        return auxInfoDto;
    }
}
