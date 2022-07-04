/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.com.utilitarios;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Felipe L. Garcia
 */
public class UteisConnect {
    private static ManagerConn managerConn;
    
    public static final String erroConexao = "Servidor offline";            

    public static Connection getConn(String banco){
        Connection c = new Connection();
        
        Properties properties = UteisProjeto.getConfig();

        if (properties == null) {
            c.setLog("getConfigProperties == null");
            return c;
        }
        String user = properties.getProperty("usuarioDB");
        String pw = properties.getProperty("senhaDB");
        String upperCaseTable = properties.getProperty("upperCaseTable");

        c.setUpperCaseTable("true".equalsIgnoreCase(upperCaseTable));
        
        c.setDataBase(banco);
        c.setUser(user);
        c.setPassword(pw);
        
        return getConn(c);
    }
    
    public static Connection getConnRemote(String banco){
        String ip = UteisProjeto.getConfig("ipServer");
        banco = banco.replaceAll("localhost",ip);
        
        Connection c = getConn(banco);
        
        return c;
    }
    
    public static Connection getConn(Connection conUtil){
        Properties properties = UteisProjeto.getConfig();
        
        if (properties == null) {
            conUtil.setLog("getConfigProperties == null");
            return conUtil;
        }
        String upperCaseTable = properties.getProperty("upperCaseTable");
        
        String driver = properties.getProperty("driverDB");
        String urlPrefix = properties.getProperty("urlPrefixDB");        
        
        UteisSecurity u = new UteisSecurity();
        try {
            if (driver == null) {
                throw new Exception("Drive do banco não foi definido");
            }
            
            Class.forName(driver);
                       
            if(urlPrefix==null || urlPrefix.trim().isEmpty()){
                throw new Exception("UrlPrefix não foi definido");
            }
            if(conUtil.getDataBase()==null || conUtil.getDataBase().trim().isEmpty()){
                throw new Exception("Caminho do banco não foi definido");
            }
            if(conUtil.getUser()==null || conUtil.getUser().trim().isEmpty()){
                throw new Exception("User do banco não foi definido");
            }
            if(conUtil.getPassword()==null || conUtil.getPassword().trim().isEmpty()){
                throw new Exception("Senha do banco não foi definido");
            }
            
            String urlFull = urlPrefix+conUtil.getDataBase();
            
            conUtil.setDriver(driver);
            conUtil.setUrl(urlFull);
//            conUtil.setBanco(currentBanco);
//            conUtil.setUser(user);
            
            if(conUtil.getUser().length() > 25){
                conUtil.setUser(u.decrypt(conUtil.getUser().trim()));
            }
            
            if(conUtil.getPassword().length() > 25){
                conUtil.setPassword(u.decrypt(conUtil.getPassword().trim()));
            }
//            conUtil.setSenha(pwdecrypt);
            
            java.sql.Connection conSQL = 
                    DriverManager.getConnection(urlFull
                    +"?autoReconnect=true&useSSL=false"
                                , conUtil.getUser().trim()
                                , conUtil.getPassword().trim());            

            if(conSQL==null){
                throw new SQLException("DriverManager não criou conexão:"
                        +urlFull);
            }
            
            conUtil.setConnection(conSQL);
            conUtil.setUpperCaseTable("true".equalsIgnoreCase(upperCaseTable));
            
            //SE config killConnThread=false,NAO START
            String closeConn = properties.getProperty("closeConn");
            if("true".equalsIgnoreCase(closeConn)){
                if (managerConn == null) {
                    //GERENCIADOR DE CON ABERTAS
                    managerConn = new ManagerConn();
                }
                managerConn.addConn(conUtil);
            }

        } catch (Exception ex) {            
            conUtil.setLog(erroConexao+(char)13+ex.getMessage());
            
            System.out.println(conUtil.getLog()
                    +(char)13+" user='"+conUtil.getUser()+"'"
                    +(char)13+" pw='"+u.rsa(conUtil.getPassword()+"'")
                    +(char)13+" url="+urlPrefix+conUtil.getDataBase()                        
                    +(char)13+new Date());                        
        } 
        
        return conUtil;
    }       
   
    public static String getConnTeste() {
        return getConnTeste(null);
    }
    public static String getConnTeste(String banco) {
        if(banco==null){
            banco = UteisProjeto.getConfig("dbmaster");
        }

        if(banco==null){
            return "dbmaster==null";
        }

        Connection conn = getConn(banco);

        try {
            if (conn.isConnected()) {
                return null;
            }

            return conn.getLog();

        } finally {
            conn.close();
        }
    }
    
    public static java.sql.Connection getConnAccess() {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");

            String banco = UteisProjeto.getConfig("dbCEP");

            return DriverManager.getConnection(
                    "jdbc:odbc:driver={Microsoft Access Driver (*.mdb)};"
                    + "DBQ=" + banco);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }
    
    //CRIA THREAD P/ VERIF TIME USO DA CONNECTION E FECHAR INATIVAS
    private static class ManagerConn {
        private List<Connection> connListManager;
        private ThreadUtil wkt;
        
        private final int limitInativeConn;//seg
        private final long sleep;//10s
        private final int limitLive;//LIMITE PARA THREAD FINALIZAR

        private final Calendar calendar;
        
        public ManagerConn() {            
            this.connListManager = new LinkedList<Connection>();
            
            this.limitInativeConn = 30;//seg
            this.sleep = 10000;//10s
            this.limitLive=60000;//1min
            
            this.calendar = Calendar.getInstance();
        }
        
        public void addConn(Connection con){
            connListManager.add(con);
            start();
        }
        
        private void start(){
            try {
                if (wkt != null) {
                    return ;
                }
                
                this.wkt = new ThreadUtil();
                wkt.addMetodo(new Runnable() {
                    @Override
                    public void run() {
                        closeListCon();
                    }
                });
                this.wkt.start();
            } catch (Exception e) {
            }
        }
       
        public synchronized void closeListCon(){
            Connection conUtil;
            
            int timeLive = 0;
            
            try {
                while(this.connListManager.size() > 0){
//                    System.out.println("ManagerConn: "+this.connListManager.size());
                    
                    for (int i = 0;i>=0 && i < this.connListManager.size(); i++) {
                        conUtil = this.connListManager.get(i);
                        
                        if(conUtil==null) {
                            this.connListManager.remove(i);
                            i--;
                            continue;
                        }
                        
                        if(!limitTime(conUtil.getTimeLast()) 
                        && !conUtil.isClosed()) {
                            continue;
                        }
                        
                        this.connListManager.remove(conUtil);
                        i--;
                        
                        conUtil.close();
                    }
                    
                    //LIMITE DE VIDA DA THREAD
                    if(timeLive >= limitLive){//CONVERT MIN/MILIS
                        break;
                    }
                    
                    Thread.sleep(this.sleep);
                    
                    timeLive+=this.sleep;
                }
                
//                System.out.println("ManagerConn Exit");
                
            } catch (Exception e) {
            }
            this.wkt = null;
        }
        
        private boolean limitTime(long lastTimeAcess) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, -this.limitInativeConn);

            if (calendar.getTimeInMillis() > lastTimeAcess) {
                return true;
            }

            return false;
        }

    }
    
}
