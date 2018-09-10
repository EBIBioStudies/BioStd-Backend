package uk.ac.ebi.biostd.webapp.application.rest.types;

import static uk.ac.ebi.biostd.webapp.application.rest.util.FileUtil.ARCHIVE_EXTENSION;
import static uk.ac.ebi.biostd.webapp.application.rest.util.FileUtil.PATH_SEPARATOR;

import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.ApiErrorException;

public class PathDescriptorResolver implements HandlerMethodArgumentResolver {
    public static final String USER_RESOURCE_ID = "/user";
    public static final String GROUPS_RESOURCE_ID = "/groups";
    static final String MALFORMED_PATH_ERROR_MSG = "Request must be addressed for user or groups";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(PathDescriptor.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        HttpServletRequest nativeRequest = (HttpServletRequest)webRequest.getNativeRequest();
        String path = getPath(nativeRequest.getRequestURL().toString());
        String archivePathSeparator = ARCHIVE_EXTENSION + PATH_SEPARATOR;
        String archivePath = "";

        if (path.contains(archivePathSeparator)) {
            String[] paths = path.split(archivePathSeparator);
            archivePath = paths[1];
            path = paths[0] + archivePathSeparator;
        }

        return new PathDescriptor(path, archivePath);
    }

    private String getPath(String requestUrl) {
        StringBuilder pathSeparator = new StringBuilder();
        String decodedUrl = URLDecoder.decode(requestUrl);
        String lowerCaseDecodedUrl = decodedUrl.toLowerCase();
        String separator = getPathSeparator(lowerCaseDecodedUrl);

        pathSeparator.append(separator).append(PATH_SEPARATOR);
        int pathSeparatorIdx = StringUtils.indexOf(lowerCaseDecodedUrl, pathSeparator.toString().toLowerCase());

        return pathSeparatorIdx > -1 ? StringUtils.substring(decodedUrl, pathSeparatorIdx + pathSeparator.length()) : "";
    }

    private String getPathSeparator(String requestUrl) {
        if(StringUtils.contains(requestUrl, USER_RESOURCE_ID)) {
            return USER_RESOURCE_ID;
        } else if (StringUtils.contains(requestUrl, GROUPS_RESOURCE_ID)) {
            return StringUtils.substringBetween(requestUrl, GROUPS_RESOURCE_ID + PATH_SEPARATOR, PATH_SEPARATOR);
        } else {
            throw new ApiErrorException(MALFORMED_PATH_ERROR_MSG, HttpStatus.BAD_REQUEST);
        }
    }
}
