

package br.com.utilitarios;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Felipe L. Garcia
 */

public class UF  implements Serializable{
    private String uf;
    private String nome;

    public UF() {
    }

    public UF(String nome,String sigla) {
        this.uf = sigla;
        this.nome = nome;
    }

    public String getDescricao() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public static List<UF> getLista() {
        List<UF> lista = new ArrayList<UF>(27);

        lista.add(new UF("Acre", "AC"));
        lista.add(new UF("Alagoas", "AL"));
        lista.add(new UF("Amapá", "AP"));
        lista.add(new UF("Amazonas", "AM"));
        lista.add(new UF("Bahia", "BA"));
        lista.add(new UF("Ceará", "CE"));
        lista.add(new UF("Distrito Federal", "DF"));
        lista.add(new UF("Espírito Santo", "ES"));
        lista.add(new UF("Goiás", "GO"));
        lista.add(new UF("Maranhão", "MA"));
        lista.add(new UF("Mato Grosso", "MT"));
        lista.add(new UF("Mato Grosso do Sul", "MS"));
        lista.add(new UF("Minas Gerais", "MG"));
        lista.add(new UF("Pará", "PA"));
        lista.add(new UF("Paraíba", "PB"));
        lista.add(new UF("Paraná", "PR"));
        lista.add(new UF("Pernambuco", "PE"));
        lista.add(new UF("Piauí", "PI"));
        lista.add(new UF("Rio de Janeiro", "RJ"));
        lista.add(new UF("Rio Grande do Norte", "RN"));
        lista.add(new UF("Rio Grande do Sul", "RS"));
        lista.add(new UF("Rondônia", "RO"));
        lista.add(new UF("Roraima", "RR"));
        lista.add(new UF("Santa Catarina", "SC"));
        lista.add(new UF("São Paulo", "SP"));
        lista.add(new UF("Sergipe", "SE"));
        lista.add(new UF("Tocantins", "TO"));
        lista.add(new UF("Exterior", "EX"));

        return lista;
    }
}
