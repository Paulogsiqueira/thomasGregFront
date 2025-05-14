package br.com.clientefront.controller;

import br.com.clientefront.model.Cliente;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import javax.ws.rs.client.Client;

import br.com.clientefront.model.Logradouro;
import br.com.clientefront.service.ClienteService;
import br.com.clientefront.service.LogradouroService;
import br.com.clientefront.util.ImageUtils;
import lombok.Data;
import org.primefaces.model.UploadedFile;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "clienteController")
@ViewScoped
@Data
@Named
public class ClienteController{

    @Inject
    private ClienteService clienteService;

    @Inject
    private LogradouroService logradouroService;

    private Cliente cliente = new Cliente();
    private List<Cliente> clientes = new ArrayList<>();
    private String nome;
    private String email;
    private UploadedFile logotipo;
    private Cliente clienteSelecionado;
    private Logradouro logradouroSelecionado;
    private UploadedFile logotipoEditado;
    private List<Logradouro> logradouros;
    private Logradouro novoLogradouro = new Logradouro();


    @PostConstruct
    public void init() {
        try {
            clienteService = new ClienteService();
            logradouroService = new LogradouroService();
            clientes = clienteService.buscarTodosClientes();
        } catch (Exception e) {
            e.printStackTrace();
            clientes = new ArrayList<>();
        }
    }

    public void adicionarCliente() {
        System.out.println("Teste add");
        cliente.setNome(nome);
        cliente.setEmail(email);
        try {
            cliente.setLogotipo(ImageUtils.convertUploadedImageToBase64(logotipo));
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            Cliente novoCliente = clienteService.adicionarCliente(cliente);
            clientes.add(novoCliente);
        }catch (IllegalArgumentException e){
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Erro",
                            "Logotipo vazio ou inválido. Adicione imagem PNG,JPG ou JPEG até 1MB"
                    )
            );
        }catch (Exception e){
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Erro",
                            "Email já cadastrado"
                    )
            );
        }
    }

    public void prepararEdicao(Cliente cliente) {
        clienteSelecionado = cliente;
        try {
            logradouros = logradouroService.buscarTodosLogradouros(clienteSelecionado.getId());
        }catch (Exception e){
            e.printStackTrace();
        }
        logotipoEditado = null;
    }

    public void salvarClienteEditado() {
        try {
            try {
                if(logotipoEditado != null) {
                    clienteSelecionado.setLogotipo(ImageUtils.convertUploadedImageToBase64(logotipoEditado));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            clienteService.atualizarCliente(clienteSelecionado, clienteSelecionado.getId());
        }catch (Exception e){

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Erro",
                            "Erro ao atualizar cliente"
                    )
            );
        }finally {
            try {
                clientes = clienteService.buscarTodosClientes();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void removerCliente(Cliente cliente) {
        try {
            clienteService.removerCliente(cliente.getId());
            clientes = clienteService.buscarTodosClientes();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    // Logradouro
    public void adicionarNovoLogradouro() {
        try {
            novoLogradouro.setClienteId(clienteSelecionado.getId());
            Logradouro logradouroAdicionado = logradouroService.adicionarLogradouro(novoLogradouro);
            logradouros.add(logradouroAdicionado);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void prepararEdicaoLogradouro(Logradouro logradouro) {
        logradouroSelecionado = logradouro;
    }

    public void editarLogradouro() {
        try {
            System.out.println(clienteSelecionado);
            logradouroService.atualizarLogradouro(logradouroSelecionado, clienteSelecionado.getId());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void removerLogradouro(Logradouro logradouro) {
        try {
            logradouroService.removerLogradouro(logradouro.getId());
            logradouros.removeIf(l -> l.getId().equals(logradouro.getId()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
