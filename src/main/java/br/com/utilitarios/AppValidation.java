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
public class AppValidation implements Serializable{
    private int codigo;
    private int appID;
    private String rsa;
    private Date dataCadastro;

    public AppValidation() {
    }

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getRsa() {
        return rsa;
    }

    public void setRsa(String rsa) {
        this.rsa = rsa;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
 
}