/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.utilitarios;

import java.io.Serializable;

/**
 *
 * @author Felipe L. Garcia
 */
public class AppIPDeny implements Serializable{
     private int codigo;
     private String ip;

    public AppIPDeny() {
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
     
}
