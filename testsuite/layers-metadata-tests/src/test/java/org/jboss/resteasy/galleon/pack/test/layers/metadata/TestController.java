package org.jboss.resteasy.galleon.pack.test.layers.metadata;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/" + TestController.CONTROLLER_PATH)
public class TestController {

    public static final String CONTROLLER_PATH = "spring";

    @GetMapping("/hello")
    public String string(@RequestParam String name) {
        return "hello " + name;
    }

}
