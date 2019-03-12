/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.com.utilitarios;

import br.com.utilitarios.ThreadUtil.ThreadLIVE;
import br.com.utilitarios.UteisDate.FormatData;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Felipe L. Garcia
 */
public class AppJavaMemory {
    private ThreadUtil monitorMemory;
    private float freeMemoryInit;
    
//    private Map<VaadinSession,String> sessions;
    private Collection<String> sessions;
    
    private PrintStream print;
//    private PrintStream printIP;
    
    private long timeSleep;//3600000
    private String nameFileLog;
    private File fileLog;
//    private final File fileIP;
    private File fileZip;
    
//    private String idSession;
            
    public AppJavaMemory() {
    }

    public void init() {
        timeSleep = UteisProjeto.getConfigInt("timeJavaMemory");
        nameFileLog = this.getClass().getSimpleName();
                
        monitorMemory = new ThreadUtil();
        monitorMemory.addMetodo(new Runnable() {
            @Override
            public void run() {
                checkMemory();
            }
        });
        monitorMemory.setRunFinally(new Runnable() {
            @Override
            public void run() {
                monitorMemory.setTimeSleep(0);
            }
        });
        monitorMemory.setTimeSleep(timeSleep);
        monitorMemory.setLIVE(ThreadLIVE.INFINITY);

//        sessions = new TreeMap<>();
        sessions = new HashSet<>();

        String dtHoje = UteisDate.convertDate(System.currentTimeMillis(),
                 FormatData.DATA_SIMPLES)
                .replaceAll("/", "-");

        fileLog = new File(AppVariables.pathWeb
                + File.separator + dtHoje + nameFileLog + ".log");
//        fileIP = new File(AppVariables.pathWeb,dtHoje+"IP.xml");
        try {
            print = new PrintStream(fileLog);
//            printIP = new PrintStream(fileIP);            

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }
    
    public void start() {
        limparLogs();                
        
        initMemory();
        monitorMemory.setTimeSleep(timeSleep);
        monitorMemory.start();
    }
        
    public void stop() {
        monitorMemory.setTimeSleep(0l);
        print.println("Stop Java Monitoring");
        print.close();
    }
    public boolean isMonitorStop() {
        return monitorMemory.getTimeSleep() <=0;
    }
    public Collection<String> getSessions() {
        return sessions;
    }
            
    private void initMemory() {
        String dt = UteisDate.convertDate(System.currentTimeMillis()
                                        , FormatData.DATA_HORA);
      
        Runtime runtime = Runtime.getRuntime();
        
        long fator = 1024*1024;
        
        long max = runtime.maxMemory();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;

        long freeNow = max - used;
        freeMemoryInit = freeNow;
        
        print.println();
        print.println("Start Java Monitoring");
        print.println(AppVariables.versionAPP);
        print.println(dt);
        print.println("maxMemory " + max/fator);
        print.println("totalAlocMemory " + total / fator);
        print.println("usedMemory " + used / fator);
        print.println("freeMemoryInit " + freeNow / fator);        
        print.close();
    }
    
    public void registerSession(String ip) {
        if(ip == null){
            return ;
        }
        if(!sessions.contains(ip)){
            sessions.add(ip);

//            printIP.println("<Sessoes n= \""+sessions.size()+"\">"
//                    + "<data>" 
//                    + UteisDate.convertDate(System.currentTimeMillis())
//                    +"</data>"
//                    + "<IP>" + ipmac+"</IP>"
//                            +"</Sessoes>");
        }
    }    
    
    public synchronized void checkMemory() {
        //SE MEMORIA CAIU +d 1/3
        Runtime runtime = Runtime.getRuntime();
        
        long fator = 1024 * 1024;

        long max = runtime.maxMemory();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;

        long freeBef = max - used;
        
        String app = new File(AppVariables.pathWeb).getName();

        print.println();
        print.println(UteisDate.convertDate(System.currentTimeMillis()));
        print.println("checkMemory");
        print.println(app+" "+AppVariables.versionAPP);
        print.println("Sessões "+sessions.size());
        print.println("usedMemory " + used / fator);
        print.println("freeMemory "+freeBef / fator + "mg");
        print.println(UteisVaadin.getURL());
        print.close();
        
        if(!UteisMetodos.checkMemory(freeMemoryInit/3,runtime)){//NAO PASSOU DO LIMITE
            return ;
        }     
        long freeNow = UteisMetodos.getFreeMemory(runtime);
        
        print.println();    
        print.println("exec GC");    
        print.println("freeMemory " + freeNow / (1024*1024)+"mg");
        print.close();
        
        fileZip = new File(fileLog.getParent()
                ,fileLog.getName().replaceAll("\\.log", "") + ".zip");

        if(!fileZip.exists()){//NAO ENVIO ALERT DIARIO
            sendEmailAlert(freeBef,used);
        }
    }

    private void sendEmailAlert(long freeBef,long used) {
        UteisEmail e = new UteisEmail();

        String app = new File(AppVariables.pathWeb).getName();
        
        long fator = 1024 * 1024;
        
        String msg = "Java memory is low"
                + "<br>"+app
                + "<br>"+AppVariables.versionAPP
                +"<br>"+sessions.size()+" Sessões"
                +"<br>usedMemory " + used / fator
                +"<br>free memory "+freeBef / fator+"mg "
                +"<br>"+UteisVaadin.getURL();
        
        e.setMsg(msg);
        
        e.setAssunto("Java Memory Low ");
        
//        fileZip = getZipLogs();

        e.setFileAnexoDest(fileZip.getPath());
        e.setDeleteAnexo(true);
        e.setFileAnexoName("javaMemory.zip");
        
        UteisProjeto.sendEmailSuporte(e);
    }
    
    private void limparLogs() {
        File[] fList = new File(AppVariables.pathWeb)
                                    .listFiles(new FilenameFilter() {

            String data = UteisDate.convertDate(
                    System.currentTimeMillis(), FormatData.DATA_SIMPLES);
            @Override
            public boolean accept(File file, String string) {
                data  = UteisMetodos.getNumber(data);
                
                return string.toLowerCase().contains(nameFileLog.toLowerCase())
                        && !string.contains(data);//EXCETO DATA DE HOJE
            }
        });
        if (fList == null || fList.length == 0) {
            return ;
        }

        for (File file : fList) {
            file.deleteOnExit();
            file.delete();
        }
    }
    
    public File getZipLogs() {
        System.out.println("ZIP "+AppJavaMemory.class.getSimpleName());
        
        File fcopy1 = new File(fileLog.getParent(), "copy" + fileLog.getName());
//        File fcopy2 = new File(fileIP.getParent(), "copy" + fileIP.getName());

        if(fileLog.exists()){
            UteisFile.copyFile(fileLog, fcopy1);
        }
//        if(fileIP.exists()){
//            UteisFile.copyFile(fileIP, fcopy2);
//        }
        
        fileZip = new File(fileLog.getParent()
                ,fileLog.getName().replaceAll("\\.log", "") + ".zip");
        fileZip.delete();
        
        if (!UteisZip.zipFile(fileZip,new File[]{fcopy1})) {
            fcopy1.delete();
//            fcopy2.delete();
            return null;
        }
        fcopy1.delete();
//        fcopy2.delete();
        
        System.out.println("ZIP:");
        System.out.println(fileZip.getName());
        
        return fileZip;
    }
//    public void listener(VaadinRequest request) {
//        final VaadinService service = request.getService();
//
//        service.addSessionInitListener(new SessionInitListener() {
//
//            @Override
//            public void sessionInit(SessionInitEvent event) throws ServiceException {
//                print.println("sessionInit");
//
//                sessions.put(event.getSession(), event.getSession().getSession().getId());
//            }
//        });
//        service.addSessionDestroyListener(new SessionDestroyListener() {
//
//            @Override
//            public void sessionDestroy(SessionDestroyEvent event) {
//                print.println("sessionDestroy");
//
//                service.closeSession(event.getSession());
//
//                sessions.remove(event.getSession());
//            }
//        });
//        service.addServiceDestroyListener(new ServiceDestroyListener() {
//
//            @Override
//            public void serviceDestroy(ServiceDestroyEvent event) {
//                print.println("serviceDestroy");
//
//                for (Map.Entry<VaadinSession, String> entry : sessions.entrySet()) {
//                    VaadinSession s = entry.getKeyMap();
//                    event.getSource().closeSession(s);
//                }
//
//            }
//        });
//    }

}
