/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.utilitarios;

/**
 *
 * @author Felipe L. Garcia
 */
public class UteisMensagem {
    
    public static final String SISTEMA_OFF = "Aplicação offline";
    public static final String SISTEMA_INVALIDO = "Aplicação inválida";
    public static final String SISTEMA_VALIDADO = "Loading app...";
    public static final String SISTEMA_BLOCKED = SISTEMA_OFF
                                                + "<br>Entre em contato com o suporte"
                                            +"<br>"+AppVariables.properties.getProperty("emailsuporte");
    
    public static final String RPT_NULL = "Erro na impressão";
        
    public static final String ALERT_POPUP = "Atenção! Desative qualquer"
            + " bloqueador POPUP antes de abrir a impressão."
            + "<br> Caso ocorra erro tente outro navegador(Firefox,Chrome,iExplorer)";

    public static final String GMAIL_SMTP = "smtp.gmail.com";
    public static final String GMAIL_PORT = "port=465";
    public static final String GMAIL_SSL = "SSL=true";
    public static final String GMAIL_HELP = GMAIL_SMTP + "<br>" + GMAIL_PORT + "<br>" + GMAIL_SSL;

    public static final String LIVE_SMTP = "smtp.live.com";
    public static final String LIVE_PORT = "port=587";
    public static final String LIVE_SSL = "SSL=true";
    public static final String LIVE_HELP = LIVE_SMTP + "<br>" + LIVE_PORT + "<br>" + LIVE_SSL;

    public static final String SMTP_HELP = "Servidores comuns:<br>"
            + LIVE_HELP + "<br>" + GMAIL_HELP;
    

}
