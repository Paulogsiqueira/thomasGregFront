package br.com.clientefront.service;

import br.com.clientefront.model.Logradouro;
import br.com.clientefront.util.AuthUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import javax.faces.bean.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class LogradouroService {

    private static final String BASE_URL = "http://localhost:8081/logradouros";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Logradouro> buscarTodosLogradouros(Long id) throws Exception {
        HttpGet request = new HttpGet(BASE_URL + "/" + id);

        String token = AuthUtils.getToken();
        if (token != null && !token.isEmpty()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return objectMapper.readValue(json, new TypeReference<List<Logradouro>>() {});
        }
    }

    public Logradouro adicionarLogradouro(Logradouro logradouro) throws Exception {
        HttpPost request = new HttpPost(BASE_URL + "/" + logradouro.getClienteId());

        String token = AuthUtils.getToken();
        if (token != null && !token.isEmpty()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        Map<String, Object> dadosLogradouro = new HashMap<>();
        dadosLogradouro.put("clienteId",logradouro.getClienteId());
        dadosLogradouro.put("rua", logradouro.getRua());
        dadosLogradouro.put("numero", logradouro.getNumero());
        dadosLogradouro.put("bairro", logradouro.getBairro());
        dadosLogradouro.put("cidade", logradouro.getCidade());
        dadosLogradouro.put("estado", logradouro.getEstado());

        String logradouroJson = objectMapper.writeValueAsString(dadosLogradouro);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addTextBody("logradouro", logradouroJson, ContentType.APPLICATION_JSON);

        HttpEntity multipart = builder.build();
        request.setEntity(multipart);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_CREATED) {
                String responseBody = EntityUtils.toString(response.getEntity());
                return objectMapper.readValue(responseBody, Logradouro.class);
            } else {
                String errorResponse = EntityUtils.toString(response.getEntity());
                throw new RuntimeException("Erro ao criar logradouro. Status: " + statusCode
                        + " - Response: " + errorResponse);
            }
        }
    }

    public void removerLogradouro(Long id) throws Exception {
        HttpDelete request = new HttpDelete(BASE_URL + "/" + id);

        String token = AuthUtils.getToken();
        if (token != null && !token.isEmpty()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }


        try (CloseableHttpResponse response = httpClient.execute(request)) {
            EntityUtils.consume(response.getEntity());
        }
    }

    public Logradouro atualizarLogradouro(Logradouro logradouro, Long clienteId) throws Exception {
        HttpPut request = new HttpPut(BASE_URL + "/" + logradouro.getId());

        String token = AuthUtils.getToken();
        if (token != null && !token.isEmpty()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }


        Map<String, Object> dadosLogradouro = new HashMap<>();
        dadosLogradouro.put("clienteId", clienteId);
        dadosLogradouro.put("rua", logradouro.getRua());
        dadosLogradouro.put("numero", logradouro.getNumero());
        dadosLogradouro.put("bairro", logradouro.getBairro());
        dadosLogradouro.put("cidade", logradouro.getCidade());
        dadosLogradouro.put("estado", logradouro.getEstado());

        String logradouroJson = objectMapper.writeValueAsString(dadosLogradouro);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addTextBody("logradouro", logradouroJson, ContentType.APPLICATION_JSON);

        HttpEntity multipart = builder.build();
        request.setEntity(multipart);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) { // 200 OK
                String responseBody = EntityUtils.toString(response.getEntity());
                return objectMapper.readValue(responseBody, Logradouro.class);
            } else {
                throw new RuntimeException("Falha ao atualizar logradouro. Status: " + statusCode);
            }
        }
    }
}
