package br.com.clientefront.controller;

import br.com.clientefront.service.LoginService;
import lombok.Data;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

@ManagedBean(name = "loginController")
@ViewScoped
@Data
public class LoginController implements Serializable {

    @Inject
    private LoginService loginService;

    @Inject
    private NavigationController navigationController;

    private String username;
    private String password;


    public void login() {
        try {
            loginService = new LoginService();
            String token = loginService.realizarLogin(username, password);

            HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSession(true);

            session.setAttribute("authToken", token);

            navigationController = new NavigationController();
            navigationController.redirectToClientes();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Erro",
                            "Credenciais Inválidas" // Mensagem hardcoded para teste
                    )
            );
        }
    }

}