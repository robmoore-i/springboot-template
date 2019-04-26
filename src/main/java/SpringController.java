import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@EnableAutoConfiguration
public class SpringController {
    private static ConfigurableApplicationContext context;

    private final RequestHandler requestHandler = new RequestHandler();

    @RequestMapping("/")
    String home() {
        return requestHandler.home();
    }

    public static void start(int portNumber) {
        HashMap<String, Object> props = new HashMap<>();
        props.put("server.port", portNumber);
        context = new SpringApplicationBuilder(SpringController.class)
                .properties(props)
                .run();
    }

    public static void stop() {
        int exitCode = 0;
        SpringApplication.exit(context, (ExitCodeGenerator) () -> exitCode);
    }
}
