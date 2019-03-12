
package br.com.utilitarios;

import java.util.Properties;

/**
 *
 * @author Felipe L. Garcia
 */
public class AppVariables {

    public static final String[] entity={"br.com.app.entidade"
                                        ,"br.com.app.entidade.auto"
                                        ,"br.com.app.entidade.commerce"
                                        ,"br.com.app.entidade.evento"};
    public static final String[] dao={"br.com.app.dao.my"
                                    ,"br.com.app.dao.my.auto"
                                    ,"br.com.app.dao.my.commerce"
                                    ,"br.com.app.dao.my.evento"};
    
    public static final String companyName="multware.com";
    public static final String byDev="by "+companyName;
    public static final String Copyright=" Copyright © 2016-2019 "
                            +AppVariables.companyName
                            +" All rights reserved";

    public static Properties properties;
    
    public static String pathWeb;
    public static String pathAlbumEventos;
    public static String pathAlbumLocalEvento;
    public static String pathAlbumEstoque;
    public static String pathAlbumCommerce;
    
    //CADA SESSION POSSUI UM IP
//    public static String ipLocal;   
//    public static String ipRemote;   
    
    public static String urlAPP;   
    
    public static boolean noThread;
    
    public static String versionAPP;
    
    public final static int USER_SUPPORT = Integer.MAX_VALUE;

    public final static long MIN_BYTE_BACKUP = 10 * 1024;// 1K = 1024B
    
    public static String charset;

    public static AppJavaMemory javaMemory;
}
