package br.com.clientefront.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.faces.bean.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class LoginService {

    private static final String LOGIN_URL = "http://localhost:8081/api/auth/login";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String realizarLogin(String username, String password) throws Exception {
        HttpPost request = new HttpPost(LOGIN_URL);

        try {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", username);
            credentials.put("password", password);

            String json = objectMapper.writeValueAsString(credentials);
            request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();

                if (response.getStatusLine().getStatusCode() == 200) {
                    return EntityUtils.toString(entity);
                } else {
                    throw new RuntimeException("Falha no login: " + EntityUtils.toString(entity));
                }
            }
        } finally {
            request.releaseConnection();
        }
    }
}