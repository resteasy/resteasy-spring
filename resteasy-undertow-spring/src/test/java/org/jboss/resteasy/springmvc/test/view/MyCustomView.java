package org.jboss.resteasy.springmvc.test.view;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

@Component
public class MyCustomView implements View {

    public String getContentType() {
        return "application/custom";
    }

    @SuppressWarnings("rawtypes")
    public void render(Map model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        response.getOutputStream().print("Hi, I'm custom!");
    }

}
