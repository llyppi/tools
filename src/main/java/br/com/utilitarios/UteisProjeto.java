/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.utilitarios;

import br.com.utilitarios.UteisModulos.Sistemas;
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Felipe L. Garcia
 */
public class UteisProjeto implements Serializable{
    public static enum EmailType implements Serializable{
        INFO,ALERTA,FALHA;
    }

    public static Properties getConfig() {
        if (AppVariables.pathWeb == null || AppVariables.pathWeb.isEmpty()) {
            System.out.println("getConfig falhou");
            System.out.println("pathWeb==null");
            return null;
        }
        File f = new File(AppVariables.pathWeb
                + File.separator 
                +"WEB-INF"
                + File.separator 
                +"config.properties");

        return UteisMetodos.getProperties(f);
    }    

    public static int getConfigInt(String value) {
        return UteisMetodos.nz(getConfig(value), 0);
    }
    public static String getConfig(String value) {
        return getConfig(value, false);
    }
    public static String getConfig(String value,boolean reload) {
        if (value == null) {
            return "";
        }
        Properties props = null;
        if(reload || AppVariables.properties==null){
            props = getConfig();
        }else{
            props = AppVariables.properties;
        }
        if (props == null) {
            return "";
        }
        value = props.getProperty(value);
        if (value == null) {
            return "";
        }

        return value;
    }
    
    public static Properties getConfig2() {
        if (AppVariables.pathWeb == null || AppVariables.pathWeb.isEmpty()) {
            System.out.println("getConfig falhou");
            System.out.println("pathWeb==null");
            return null;
        }

        File f = new File(AppVariables.pathWeb
                + File.separator 
                +"config.properties");

        return UteisMetodos.getProperties(f);
    }

    public static String getConfig2(String value) {
        if (value == null) {
            return "";
        }

        Properties props = getConfig2();
        if (props == null) {
            return "";
        }
        value = props.getProperty(value);
        if (value == null) {
            return "";
        }

        return value;
    }
    
    public static void configEmailLogoDefault(UteisEmail email) {
//        String path = AppVariables.pathWeb
//                            +File.separator+"vaadin"
//                            +File.separator+"themes"                            
//                            +File.separator+"vaadin"
//                            +File.separator+"images"
//                            +File.separator+"LogoMarca64x64.png";
//        String back = AppVariables.pathWeb
//                            +File.separator+"WEB-INF"
//                            +File.separator+"background.jpg";
        
//        String bs64="";
//        byte[] byt = UteisFile.fileToByte(path);
//        if(byt!=null){
//            bs64 = UteisMetodos.byteToBase64(byt);
//        }
//        String back64="";
//        byt = UteisFile.fileToByte(back);
//        if(byt!=null){
//            back64 = UteisMetodos.byteToBase64(byt);
//        }
//        System.out.println(bs64);
//        email.setImgByte(byt);
//        email.setImgCID("imglogo");
        
//<img width=128 height=128 src=data:image/png;base64,
//<img width=128 height=128 src=\"cid:"
    String url = UteisProjeto.getConfig("urlLogotipo");
    
        String content = 
                "<table backgroud-color=\"#D7DADB\""
                +" width=\"80%\" "
                +" align=\"center\""
                    +"<tr>"                       
                        +"<td bgcolor=\"#DCEAFA\">"
                
                            +"<img style=\"float:center\" src=\""+url+"\"/>"
                        +"</td>"
                    +"</tr>"                       
                +"</table>"
                +"<table width=\"80%\" align=\"center\">"
                    +"<tr>"                       
                        +"<td bgcolor=\"#C1DAF7\">"
                            +"<p style=\"font-family:Verdana;font-size:14px;color:#60646b;\">"
                                +"<strong>"
                                    +email.getMsg()
                                +"</strong>"
                            +"</p>"
                        +"</td>"
                    +"</tr>"
                +"</table>";
        
        email.setMsg(content);
    }

    public static boolean isDeveloper() {
        String macLocal = UteisMetodos.getMAC();
        String macdev = getConfig("MACDEV");
        
        macLocal = macLocal.replaceAll("[\\-\\.\\:]", "");
        macdev = macdev.replaceAll("[\\-\\.\\:]", "");
        
        if(!macdev.contains(";")){
            return macLocal.equalsIgnoreCase(macdev);//PC DEVELOPER
        }
        String[] mc = macdev.split(";");
        for (String m : mc) {
            if(m.equalsIgnoreCase(macLocal)){
                return true;
            }
        }
        return false;
    }
    /**ALTERA DESTINATARIO CASO ENVIO SEJA EXECUTADO EM AMBIENTE DE DESENVOLVIMENTO
     * EVITAR ENVIO POR ENGANO
     */
    private static void configDestTest(UteisEmail email) {
        if (isDeveloper()) {//PC DEVELOPER
            String to = getConfig("emailsuporte");
            
            email.setEmailTo(to);
            System.out.println("EMAIL DESTINATARIO ALTERADO "+to);
        } 
    }
    
    /**ENVIAR EMAIL USANDO LOGO DEFAULT*/
    public static void sendEmailDefault(UteisEmail... emails){
        
        for (UteisEmail e : emails) {
            configEmailLogoDefault(e);            
        }
        sendEmailThead(true, emails);
    }
    
    public static ThreadUtil sendEmailThead(UteisEmail... emails) {
        return sendEmailThead(false, emails);
    }
    
    public static ThreadUtil sendEmailThead(boolean smtpDefault,UteisEmail... emails) {
        ThreadUtil thread = new ThreadUtil();
        thread.addMetodo(new Runnable() {
            @Override
            public void run() {
                sendEmail(smtpDefault, emails);
            }
        });
        
        thread.start();
        return thread;
    }
    
    public static boolean sendEmail(UteisEmail... emails) {
        return sendEmail(true, emails);
    }

    public static boolean sendEmail(boolean smtpDefault,UteisEmail... emails){
        if (emails == null || emails.length ==0) {
            return false;
        }
        
        try {
            //CONFIGURAR EMAILS P ENVIAR
            for (UteisEmail e : emails) {
                if (smtpDefault) {
                    //SERVIDOR SMTP PADRAO
                    configEmailSmtpDefault(e);
                }
                
            }

            UteisEmail.sendEmailJavax(emails);
            
        } catch (Exception ex) {
            
            System.out.println(ex);
            emails[0].setLog(ex.getMessage());
            
            if (ex instanceof AuthenticationFailedException) {
                System.out.println("Erro ao enviar email."
                        +" Autenticação inválida");
            } 
            if (ex instanceof MessagingException) {
                if(ex.getMessage()!=null){
                    if (ex.getMessage().contains("Server busy")) {
                        System.out.println("Servidor SMTP ocupado");
                    }
                }
            }
            
            return false;
        }
        return true;
    }
    
    public static boolean isMicrosoft(String to){
        to = to.toLowerCase();
        if (to.contains("@outlook")
                || to.contains("@hotmail")
                || to.contains("@live")
                || to.contains("@msn")) {
            return true;
        }
        return false;
    }
//    /**PARAMETRO SEGURANCA CRIPTOGRAFADA NO SMTP*/
//    public static void configEmailSSL(UteisEmail email){
//        if (email == null) {
//            return;
//        }
//
//        if ("smtp.gmail.com".equals(email.getSmtp())
//        || "smtp.live.com".equals(email.getSmtp())) {
//            email.setSSL(true);
//        }
//    }
    /**CONFIG SMTP DEFAULT*/
    public static void configEmailSmtpDefault(UteisEmail email){
        if (email == null) {
            return;
        }
        Properties properties = getConfig();
        
        String pwd = properties.getProperty("smtpsenha");
        String user = properties.getProperty("smtpuser");
        if (user.length() > 25) {
            user = new UteisSecurity().decrypt(user);
        }
        if (pwd.length() > 25) {
            pwd = new UteisSecurity().decrypt(pwd);
        }
        email.setAutentication(user,pwd);
        email.setSmtp( properties.getProperty("smtphost"));
        email.setPort(UteisMetodos.nz(properties.getProperty("smtpport"),25));
        email.setSSL("true".equalsIgnoreCase(properties.getProperty("smtpSSL")));
        email.setTLS("true".equalsIgnoreCase(properties.getProperty("smtpTLS")));
        email.setEmailFrom(properties.getProperty("emailenvio"));
    }
    public static void sendEmailInfo(UteisEmail email, int sistema,int clienteID) {
        sendEmailSuporte(email, sistema, EmailType.INFO,clienteID);
    }
    public static void sendEmailAlerta(UteisEmail email, int sistema,int clienteID) {
        sendEmailSuporte(email, sistema, EmailType.ALERTA,clienteID);
    }
    public static void sendEmailFalha(UteisEmail email, int sistema,int clienteID) {
        sendEmailSuporte(email, sistema, EmailType.FALHA,clienteID);
    }
    /**
     * ENVIA EMAIL P/ SUPORTE
     */
    public static void sendEmailSuporte(UteisEmail email
            , int sistema,EmailType tipoEmail,int clienteID) {
        
        String cliente = "<br><br>Cliente: "+clienteID;
        
        String modulo = Sistemas.getName(sistema);
        String localCode = "<br><br>Projeto:"+getConfig("nameAPP")
                        +"<br><br>Módulo: "+modulo
                        +"<br><br>Versão: "+AppVariables.versionAPP;
        
        email.setMsg(email.getMsg()
                    +"<br><br>"+cliente+localCode);
        
        email.setAssunto(tipoEmail==null?"":tipoEmail.name()+" "
                + email.getAssunto()
                +" " + clienteID);
                
        sendEmailSuporte(email);   
    }
    
    /**ENVIA EMAIL P/ COM DETALHES DO ERRO*/
    public static void sendEmailFalhaCode(String msg,String assunto
                                       ,Class cls,int sistema,int clienteID){
        
        String cliente = "<br><br>Cliente: "+clienteID;        
        
        String classe = "";
        if(cls!=null){
            classe = "<br><br>Classe: "+cls.getName();
        }
        
        String modulo = Sistemas.getName(sistema);
        String localCode = classe+"<br><br>Projeto:"+getConfig("nameAPP")
                            +"<br><br>Módulo: "+modulo
                            +"<br><br>Versão: "+AppVariables.versionAPP;
        
        msg = cliente+localCode;
        assunto = assunto+" "+clienteID;
                
        sendEmailSuporte(msg, assunto);        
    }

    /**ENVIA EMAIL P/ */
    public static void sendEmailSuporte(String msg, String assunto){
        UteisEmail email = new UteisEmail();
        email.setMsg(msg);
        email.setAssunto(assunto);
        
        sendEmailSuporte(email);
    }
        
    /**ENVIA EMAIL P/ */
    public static void sendEmailSuporte(UteisEmail email){
        configEmailSmtpDefault(email);
        
        email.setEmailTo(getConfig("emailsuporte"));
        
        sendEmailThead(true, email);
    }

    public static String getLocalJasper() {
        String path;

        path = AppVariables.pathWeb
                + File.separator 
                +"WEB-INF"
                + File.separator 
                +"rpt" ;

        return path;
    }
    
    public static long getVersaoRemote(String app) {
        //URL DOWNLOAD
        final String url
                = UteisProjeto.getConfig("urlVersoes");               

        System.out.println("URL: " + url);

        //LER XML URL
        NodeList sistemaList = null;
        try {
            if (url.contains("http")) {
//                if (!UteisMetodos.isOnline(UteisMetodos.getDomain(url))) {
//                    return 0;
//                }

                sistemaList = UteisFile.readFileXML(new URL(url), "sistema");
            }
            if (url.contains("ftp")) {
                Ftp ftp = new Ftp(url);
                String user = UteisProjeto.getConfig("ftpuser");
                String pw = UteisProjeto.getConfig("pwApp");
                
                if (user.length() > 25) {
                    user = new UteisSecurity().decrypt(user);
                }
                if (pw.length() > 25) {
                    pw = new UteisSecurity().decrypt(pw);
                }
                
                ftp.setUser(user);
                ftp.setPwd(pw);

                sistemaList = UteisFile.readFileXML(ftp, "sistema");
            }
            if (sistemaList == null) {
                System.out.println("VERSAO NAO ECONTRADA");
                return 0;
            }
            
            app = app.toUpperCase();

//            if (!sistema.endsWith("WEB")) {
//                sistema += "WEB";
//            }
            String value = getVersao(sistemaList, app);

            if (value != null) {
                System.out.println("VERSAO SITE " + app + ": " + value);
                return Long.parseLong(value.replaceAll("[^0-9]", ""));
            } else {
                System.out.println("VERSAO NAO ENCONTRADA NO SITE: " + app);
                return 0;
            }

        } catch (MalformedURLException ex) {
//            ex.printStackTrace();
        }

        return 0;
    }

    public static String getVersaoXML(String projeto) {
        String file = UteisMetodos.getTomcatDir()
                + File.separator 
                +"webapps"
                + File.separator 
                +"versoes"
                + File.separator 
                + "versoes.xml";

        NodeList sistemaList;
        try {
            sistemaList = UteisFile.readFileXML(file, "sistema");
        } catch (Exception ex) {
            return null;
        }

        String value = getVersao(sistemaList, projeto);

        System.out.println("VERSAO XML " + projeto + ": " + value);

        return value;
    }

    private static String getVersao(NodeList sistemaList, String sistema) {
        if (sistemaList == null) {
            return null;
        }
        if (sistema == null || sistema.isEmpty()) {
            return null;
        }

        Element findElement
                = UteisMetodos.getXMLElement(sistemaList, sistema, "nome");

        if (!(findElement instanceof Element)) {
            return null;
        }
        Element element = (Element) findElement;
        return element.getElementsByTagName("versao")
                .item(0).getFirstChild().getNodeValue();
    }    
    
    public static Map<String,String> getEndereco(String cep){
        String urlCorreios = getConfig("urlCorreios");
        urlCorreios = urlCorreios.replace("cep?", cep);
        
        return UteisMetodos.getAddress(cep,urlCorreios);
    }
    
    
}
