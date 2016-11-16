/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.session.local.config.annotation.web.http;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSession;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;
import org.springframework.session.local.LocalMapOperationsSessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;

/**
 * Exposes the {@link SessionRepositoryFilter} as a bean named
 * "springSessionRepositoryFilter". In order to use this a single
 * {@link RedisConnectionFactory} must be exposed as a Bean.
 * 
 * @author Rob Winch
 * @since 1.0
 * 
 * @see EnableRedisHttpSession
 */
public class LocalMapHttpSessionConfiguration extends SpringHttpSessionConfiguration implements ServletContextListener {

    private Integer maxInactiveIntervalInSeconds = MapSession.DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;

    public LocalMapOperationsSessionRepository sessionRepository() {
        LocalMapOperationsSessionRepository sessionRepository = new LocalMapOperationsSessionRepository();
        sessionRepository.setDefaultMaxInactiveInterval(this.maxInactiveIntervalInSeconds);
        return sessionRepository;
    }

    public void setMaxInactiveIntervalInSeconds(int maxInactiveIntervalInSeconds) {
        this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        this.setServletContext(context);
        if (null != context.getInitParameter("maxInactiveIntervalInSeconds")) {
            maxInactiveIntervalInSeconds = Integer.valueOf(context.getInitParameter("maxInactiveIntervalInSeconds"));
        }

        SessionRepository<ExpiringSession> sessionRepository = sessionRepository();
        Dynamic filter = context.addFilter("springSessionRepositoryFilter",
                springSessionRepositoryFilter(sessionRepository));

        EnumSet<DispatcherType> dispatchers = EnumSet.allOf(DispatcherType.class);
        dispatchers.add(DispatcherType.REQUEST);
        dispatchers.add(DispatcherType.FORWARD);
        filter.addMappingForUrlPatterns(dispatchers, true, "/*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

    }
}
