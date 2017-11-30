package com.pri.session;

import javax.servlet.ServletRequest;


/**
 * @author mg
 *
 * To change the template for this generated type comment go to Window - Preferences - Java - Code Generation - Code and
 * Comments
 */
public class HTTPRequestData implements com.pri.session.RequestData {

    private final String addr;

    public HTTPRequestData(ServletRequest rq) {
        addr = rq.getRemoteAddr();
    }

    /**
     * @return Returns the addr.
     */
    @Override
    public String getAddr() {
        return addr;
    }

}
