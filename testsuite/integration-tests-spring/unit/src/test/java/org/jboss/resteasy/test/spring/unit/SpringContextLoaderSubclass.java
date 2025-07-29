package org.jboss.resteasy.test.spring.unit;

import jakarta.servlet.ServletContext;

import org.jboss.resteasy.plugins.spring.SpringContextLoader;
import org.springframework.web.context.ConfigurableWebApplicationContext;

public class SpringContextLoaderSubclass extends SpringContextLoader {

    @Override
    protected void customizeContext(ServletContext servletContext,
            ConfigurableWebApplicationContext configurableWebApplicationContext) {
        super.customizeContext(servletContext, configurableWebApplicationContext);
    }
}
