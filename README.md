# springboot-template

- Main.java contains only the 'public static void main'
- SpringController.java is the only class refering directly to spring stuff
- RequestHandler.java is delegated to by the SpringController to respond to requests
- IntegrationTest.java starts and stops the server using the SpringController, and tests it by sending HTTP requests and asserting on the responses.
- build.gradle includes the dependencies required for this set up.
- manifest.yml is the PCF config file

This is shown below.

### Main.java

```
public class Main {
    public static void main(String[] args) {
        SpringController.start(8080);
    }
}
```

### SpringController.java

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

```
public class RequestHandler {
    String home() {
        return "This is web maths";
    }
}
```

### IntegrationTest.java

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

```
---
applications:
- name: web-maths
  memory: 1G
  random-route: true
  path: build/libs/web-maths-1.0-SNAPSHOT.jar

```
