/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 *
 * $Id$
 */

package org.nuxeo.ecm.webengine.gwt.dev;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public interface Debug {

    final static String REDIRECT_HOST = "localhost";

    final static int REDIRECT_PORT = 8081;

    final static String REDIRECT_PREFIX = "/redirect";

    final static String REDIRECT_PATTERN = REDIRECT_PREFIX + "/(.*)";

    final static String REDIRECT_REPLACEMENT = "/$1";

    final static boolean REDIRECT_TRACE = false;

    final static boolean REDIRECT_TRACE_CONTENT = false;

    /*
     * Add this code in tomcat/webapps/ROOT/WEB-INF/web.xml
     */

    /*
     * <servlet> <servlet-name>redirect</servlet-name>
     * <servlet-class>org.nuxeo.ecm.webengine.gwt.debug.RedirectServlet</servlet-class> </servlet> <servlet-mapping>
     * <servlet-name>redirect</servlet-name> <url-pattern>/redirect/*</url-pattern> </servlet-mapping>
     */
}
