package it;

import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Rule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

public class ZBikesITest {

    @Rule
    public DropwizardAppWithPostgresRule app = new DropwizardAppWithPostgresRule();

    @Test
    public void create_new_stations() throws Exception {

        String payload = "{\n" +
                "                                    \"name\": \"West Road\",\n" +
                "                                    \"location\": {\n" +
                "                                      \"lat\": 3.20,\n" +
                "                                      \"long\": 40.24\n" +
                "                                    },\n" +
                "                                    \"availableBikes\": [\n" +
                "                                      \"001\",\"002\",\"003\",\"004\"\n" +
                "                                    ]\n" +
                "                                  }";

        ValidatableResponse response = given().port(app.getLocalPort())
                .contentType(JSON)
                .body(payload)
                .put("/station/1")
                .then();

        response.statusCode(201);
    }

}
