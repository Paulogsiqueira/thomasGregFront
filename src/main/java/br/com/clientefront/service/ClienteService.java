package br.com.clientefront.service;

import br.com.clientefront.model.Cliente;
import br.com.clientefront.util.AuthUtils;
import br.com.clientefront.util.ImageUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import javax.faces.bean.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ClienteService {

    private static final String BASE_URL = "http://localhost:8081/clientes";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Cliente> buscarTodosClientes() throws Exception {
        HttpGet request = new HttpGet(BASE_URL);

        String token = AuthUtils.getToken();
        if (token != null && !token.isEmpty()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return objectMapper.readValue(json, new TypeReference<List<Cliente>>() {});
        }
    }

    public Cliente adicionarCliente(Cliente cliente) throws IOException {
        HttpPost request = new HttpPost(BASE_URL);

        String token = AuthUtils.getToken();
        if (token == null || token.isEmpty()) {
            throw new IOException("Token de autenticação não disponível");
        }
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        Map<String, String> dadosCliente = new HashMap<>();
        dadosCliente.put("nome", cliente.getNome());
        dadosCliente.put("email", cliente.getEmail());

        String clienteJson = objectMapper.writeValueAsString(dadosCliente);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addTextBody(
                "cliente",
                clienteJson,
                ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8));

        String base64 = cliente.getLogotipo();
        if (base64 == null || base64.isEmpty()) {
            throw new IllegalArgumentException("Logotipo não fornecido");
        }

        String[] parts = base64.split(",");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Formato Base64 inválido (cabeçalho faltando)");
        }

        String base64Data = parts[1];
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new IOException("Base64 corrompido", e);
        }

        String mimeType = ImageUtils.detectImageType(imageBytes);
        String extension;
        switch (mimeType) {
            case "image/png":
                extension = "png";
                break;
            case "image/jpeg":
                extension = "jpg";
                break;
            default:
                throw new IOException("Formato de imagem não suportado: " + mimeType);
        }

        ContentType contentType = ContentType.create(mimeType);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        InputStreamBody inputStreamBody = new InputStreamBody(
                inputStream,
                contentType,
                "logotipo." + extension
        );
        builder.addPart("logotipo", inputStreamBody);

        HttpEntity multipart = builder.build();
        request.setEntity(multipart);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            if (statusCode < 200 || statusCode >= 300) {
                String errorResponse = EntityUtils.toString(response.getEntity());
                throw new IOException("Erro no servidor: " + statusCode + " - " + errorResponse);
            }
            return objectMapper.readValue(responseBody, Cliente.class);
           // EntityUtils.consume(response.getEntity());
        }
    }

    public Cliente atualizarCliente(Cliente cliente, Long id) throws Exception {
        HttpPut request = new HttpPut(BASE_URL + "/" + id);

        String token = AuthUtils.getToken();
        if (token != null && !token.isEmpty()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }


        Map<String, String> dadosCliente = new HashMap<>();
        dadosCliente.put("nome", cliente.getNome());
        dadosCliente.put("email", cliente.getEmail());

        String clienteJson = objectMapper.writeValueAsString(dadosCliente);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addTextBody("cliente", clienteJson, ContentType.APPLICATION_JSON);

        String base64 = cliente.getLogotipo();
        String base64Data = base64;
        if(base64.contains(",")) {
            base64Data = base64.split(",")[1];
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        InputStreamBody inputStreamBody = new InputStreamBody(inputStream, ContentType.IMAGE_PNG, "logotipo.png");

        builder.addPart("logotipo", inputStreamBody);

        request.setEntity(builder.build());

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseBody = EntityUtils.toString(response.getEntity());
                return objectMapper.readValue(responseBody, Cliente.class);
            } else {
                throw new RuntimeException("Erro ao atualizar cliente: " + response.getStatusLine());
            }
        }
    }

    public void removerCliente(Long id) throws Exception {
        HttpDelete request = new HttpDelete(BASE_URL + "/" + id);

        String token = AuthUtils.getToken();
        if (token != null && !token.isEmpty()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            EntityUtils.consume(response.getEntity());
        }
    }
}
