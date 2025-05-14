package br.com.clientefront.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class AuthUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getToken() throws IOException {
        HttpSession session = getSession();
        String jsonToken = (String) session.getAttribute("authToken");
        return extractTokenFromJson(jsonToken);
    }

    private static HttpSession getSession() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            throw new IllegalStateException("FacesContext não disponível");
        }
        return (HttpSession) context.getExternalContext().getSession(false);
    }

    private static String extractTokenFromJson(String jsonToken) throws IOException {
        Map<String, String> tokenMap = mapper.readValue(
                jsonToken,
                new TypeReference<Map<String, String>>(){}
        );
        return tokenMap.get("token");
    }
}