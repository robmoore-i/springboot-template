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

        assertThat(responseText, equalTo("This is a springboot app"));
    }

    private String getRequestText(String requestPath) throws UnirestException {
        String url = origin + requestPath;
        HttpResponse<String> response = Unirest.get(url).asString();
        return response.getBody();
    }
}
