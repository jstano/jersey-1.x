/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.ws.rs.ext;

import com.sun.jersey.api.uri.UriBuilderImpl;
import com.sun.jersey.spi.HeaderDelegateProvider;
import com.sun.jersey.spi.service.ServiceFinder;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * A default client-based and hardcoded implementation of
 * {@link RuntimeDelegate} that will be instantiated if all look up mechanisms
 * to to find an instance fail.
 * <p>
 * A hardcoded reference to this class name occurs in the {@link RuntimeDelegate}
 * class present in the JAX-RS api jar.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class RuntimeDelegateImpl extends RuntimeDelegate {

    final Set<HeaderDelegateProvider> hps =
            new HashSet<HeaderDelegateProvider>();

    final private Map<Class<?>, HeaderDelegate> map =
            new WeakHashMap<Class<?>, HeaderDelegate>();

    public RuntimeDelegateImpl() {
        for (HeaderDelegateProvider p : ServiceFinder.find(HeaderDelegateProvider.class, true))
            hps.add(p);

        /**
         * Construct a map for quick look up of known header classes
         */
        map.put(EntityTag.class, _createHeaderDelegate(EntityTag.class));
        map.put(MediaType.class, _createHeaderDelegate(MediaType.class));
        map.put(CacheControl.class, _createHeaderDelegate(CacheControl.class));
        map.put(NewCookie.class, _createHeaderDelegate(NewCookie.class));
        map.put(Cookie.class, _createHeaderDelegate(Cookie.class));
        map.put(URI.class, _createHeaderDelegate(URI.class));
        map.put(Date.class, _createHeaderDelegate(Date.class));
        map.put(String.class, _createHeaderDelegate(String.class));
    }

    @Override
    public UriBuilder createUriBuilder() {
        return new UriBuilderImpl();
    }

    @Override
    public ResponseBuilder createResponseBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VariantListBuilder createVariantListBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T createEndpoint(Application application, Class<T> endpointType)
            throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
        HeaderDelegate h = map.get(type);
        if (h != null) return h;

        return _createHeaderDelegate(type);
    }

    @SuppressWarnings("unchecked")
    private <T> HeaderDelegate<T> _createHeaderDelegate(Class<T> type) {
        for (HeaderDelegateProvider hp: hps)
            if (hp.supports(type))
                return hp;

        throw new IllegalArgumentException("A header delegate provider for type, " + type +
                ", is not supported");
    }
}