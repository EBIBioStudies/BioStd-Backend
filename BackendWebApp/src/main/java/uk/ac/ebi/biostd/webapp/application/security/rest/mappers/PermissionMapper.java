package uk.ac.ebi.biostd.webapp.application.security.rest.mappers;

import com.google.common.base.MoreObjects;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessTag;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;

@Component
public class PermissionMapper {

    public Map<String, String> getPermissionMap(User user) {
        Map<String, String> accessInfo = new LinkedHashMap<>(7);
        accessInfo.put("Status", "OK");
        accessInfo.put("Allow", getUserAllows(user));
        accessInfo.put("Deny", StringUtils.EMPTY);
        accessInfo.put("Superuser", String.valueOf(user.isSuperuser()));
        accessInfo.put("Name", user.getFullName());
        accessInfo.put("Login", MoreObjects.firstNonNull(user.getLogin(), StringUtils.EMPTY));
        accessInfo.put("EMail", user.getEmail());
        return accessInfo;
    }

    private String getUserAllows(User user) {
        List<String> accessTags = user.getAccessPermissions()
                .stream()
                .filter(permission -> permission.getAccessType().equals(AccessType.READ))
                .map(AccessPermission::getAccessTag)
                .map(AccessTag::getName)
                .collect(Collectors.toList());
        return "~" + user.getEmail() + ";#" + user.getId() + ";" + String.join(";", accessTags);
    }
}
