/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.utilitarios;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Felipe L. Garcia
 */
public class AppMonitor implements Serializable{
    private int codigo;
    private String ip;
    private String url;
    private Date dataCadastro;

    public AppMonitor() {
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
}