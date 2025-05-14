package br.com.clientefront.model;

import lombok.Data;

import javax.servlet.http.Part;
import java.io.Serializable;
@Data
public class Cliente implements Serializable {

    private Long id;
    private String nome;
    private String email;
    private String logotipo;

    private Part arquivoLogotipo;
}
