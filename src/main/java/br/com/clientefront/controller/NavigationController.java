package br.com.clientefront.controller;

import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.IOException;

@Named
@RequestScoped
public class NavigationController {

    public void redirectToClientes() {
        try {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect("pages/cliente/clientes.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}