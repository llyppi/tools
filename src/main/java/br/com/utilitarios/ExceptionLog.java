/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.utilitarios;

import br.com.utilitarios.UteisDate.FormatData;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Felipe L. Garcia
 */
public class ExceptionLog {
    public final static String nameFileLog = ExceptionLog.class.getSimpleName();
    //LISTA Exception 
    private static Set<String> listException = new TreeSet<String>();
    
    //FLAG ROTINA EXECUTANDO
    private static boolean reportRodando;
    
    private static PrintStream logStream;

    /**GRAVAR Exception NO LOG pastaweb/nameLog SOMENTE UMA VEZ*/
    public static void log(Exception e) {
        String value = e.getLocalizedMessage();
        
        if(listException.contains(value)){
            return ;
        }
        
        StringBuilder trace = new StringBuilder();
        
        for (StackTraceElement traceElement : e.getStackTrace()) {
            trace.append("<Trace>");
            trace.append("<ClassName>").append(traceElement.getClassName()).append("</ClassName>");
            trace.append("<Method>").append(traceElement.getMethodName()).append("</Method>");
            trace.append("<LineNumber>").append(traceElement.getLineNumber()).append("</LineNumber>");
            trace.append("<FileName> ").append(traceElement.getFileName()).append("</FileName>");
            trace.append("</Trace>");
        }
        log(trace.toString());
        
        listException.add(value);
    }
    
    /**GRAVAR LOG NO ARQUIVO web/nameLog*/
    public static void log(String erro) {        
        long time = System.currentTimeMillis();
        
        String dtHoje = UteisDate.convertDate(time
                                        ,FormatData.DATA_SIMPLES);
        dtHoje = dtHoje.replaceAll("/", "-")+"";
        
        File fLog = new File(AppVariables.pathWeb
                                        +File.separator
                                        +dtHoje+nameFileLog+".xml");                                
        try {
            //TESTAR SE CAMINHO É CORRETO
            //SE NAO EXISTIR E NAO CRIAR NOVO,O CAMINHO NAO ESTÁ CORRETO
            if (!fLog.canWrite() | !fLog.exists() && !fLog.createNewFile()) {
                System.out.println(ExceptionLog.class.getSimpleName()
                        + ": CAMINHO DO LOG ESTÁ INCORRETO OU NAO PODE SER CRIADO"
                        + " config-pathLog=  " + fLog.getPath());
                return;
            }
            
            if (!fLog.exists()) {//PRIMEIRO LOG DO DIA
                fLog.createNewFile();                
//                listException.clear();
                if (logStream != null) {//STREAM DE ARQUIVO ANTIGO
                    logStream.close();
                } 
            }
            if(logStream == null){
                logStream = new PrintStream(fLog);
                logStream.println("<xml>");
            }
            
        } catch (IOException e) {
            System.out.println("ERRO AO CRIAR LOG "+fLog);
            System.out.println(e);
            return ;
        }
        
        logStream.println();
        logStream.println("<Exception date=\""+
                UteisDate.convertDate(time,FormatData.DATA_HORA_SIMPLES)+"\">"); 
        logStream.println(erro);
        logStream.println("</Exception>");
        logStream.flush();
//        log.close();
    }
    
    /**ZIP LOG DO DIA ANTERIOR*/
    public static File getZipLogs(){
        System.out.println("ZIP "+ExceptionLog.class.getSimpleName());
        
        if(ExceptionLog.reportRodando){
            return null;
        }
        
        ExceptionLog.reportRodando = true;
                
        long time = System.currentTimeMillis();
        
        final String dtHoje = UteisDate.convertDate(time,FormatData.DATA_SIMPLES)
            .replaceAll("/", "-");
        
        final File fWeb = new File(AppVariables.pathWeb);
                                   
        //LISTA DOS LOGS ANTERIORES A DATA ATUAL
        File[] fListOld = fWeb.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {
                return string.toLowerCase().contains(nameFileLog.toLowerCase())
                    && !string.contains(dtHoje);//EXCETO DATA ATUAL
            }
        });                
        
        if(fListOld==null || fListOld.length==0){
            ExceptionLog.reportRodando = false;
            return null;
        }
        
//        ByteArrayOutputStream bytesFile = new ByteArrayOutputStream();
        
        File fileZip = new File(fWeb, dtHoje+nameFileLog+".zip");
        fileZip.delete();
        
        if(!UteisZip.zipFile(fileZip,null,null,false,3,fListOld)){
            System.out.println("ERRO ZIP LOGS "+fileZip);
            ExceptionLog.reportRodando = false;
            return null;
        }
        
        System.out.println("ZIP:");
        System.out.println(fileZip.getName());
        
        ExceptionLog.reportRodando = false;
        
        return fileZip;
    }
    
}
