# springboot-template

- Gradle build file is build.gradle.
- Source classes are Main, SpringController, RequestHandler.
- Test class is IntegrationTest.
- PCF config is manifest.yml.

### Main.java

Just the psvm.

```
public class Main {
    public static void main(String[] args) {
        SpringController.start(8080);
    }
}
```

### SpringController.java

Two public static methods: start and stop. Delegates request handling to the RequestHandler, which is independent of Spring.

```
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
```

### RequestHandler.java

Responds to endpoints.

```
public class RequestHandler {
    String home() {
        return "This is web maths";
    }
}
```

### IntegrationTest.java

Starts and stops the application at the top level. Tests endpoints by making HTTP requests using Unirest.

```
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class IntegrationTest {
    private final int portNumber = 9090;
    private final String origin = "http://localhost:" + portNumber;

    @Before
    public void setUp() {
        SpringController.start(portNumber);
    }

    @After
    public void tearDown() {
        SpringController.stop();
    }

    @Test
    public void GET_slash_returnsHelloMessage() throws UnirestException {
        String responseText = getRequestText("/");

        assertThat(responseText, equalTo("This is web maths"));
    }

    private String getRequestText(String requestPath) throws UnirestException {
        String url = origin + requestPath;
        HttpResponse<String> response = Unirest.get(url).asString();
        return response.getBody();
    }
}
```

### build.gradle

Needs the springboot plugin. Needs the springboot library for source and the unirest library for the integration tests.

```
plugins {
    id 'java'
    id "org.springframework.boot" version "2.1.4.RELEASE"
}

group 'pcf-practice'
version '1.0-SNAPSHOT'

sourceCompatibility = 1.8

repositories {
    mavenCentral()
}

dependencies {
    compile group: 'org.springframework.boot', name: 'spring-boot-starter-web', version: '2.1.4.RELEASE'
    testCompile group: 'junit', name: 'junit', version: '4.12'
    testCompile group: 'com.mashape.unirest', name: 'unirest-java', version: '1.4.9'
}
```

### manifest.yml

The manifest for pushing to PCF. The command, when in the top level directory of this repo, is `cf push --hostname springboot-template`.

```
---
applications:
- name: springboot-template
  memory: 1G
  random-route: true
  path: build/libs/springboot-template-1.0-SNAPSHOT.jar

```
