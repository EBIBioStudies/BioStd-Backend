package uk.ac.ebi.biostd.webapp.application.common.utils;

import javax.servlet.http.Cookie;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WebUtils {

    public static Cookie newExpiredCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        return cookie;
    }
}
