import br.com.app.tela.Application;
import br.com.app.tela.AppContent;
import br.com.utilitarios.AppInterface;
import br.com.utilitarios.UteisDate;
import br.com.utilitarios.UteisDate.FormatData;
import br.com.utilitarios.UteisProjeto;
import br.com.utilitarios.AppVariables;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.server.*;
import com.vaadin.ui.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

/**
 *
 * @author Felipe L. Garcia
 */

@Theme("vaadin")
@Viewport("width=device-width, initial-scale=1,user-scalable=no,orientation=portrait")

public class Main extends UI  {
    private RequestHandler requestParam;
    private AppContent main;
    private Application application;
    
    @Override
    protected void init(VaadinRequest request) {
        initConfig(request);
        configLog(request);

        Panel panel = new Panel();
        super.setContent(panel);
        panel.setSizeFull();

        main = new AppContent();
        panel.setContent(main);         

        application = new Application();
        main.addComponent((AppInterface)application);
      
        //CONFIG PARAMETROS BROWSER
        requestParam = getParamRequest();
        getSession().addRequestHandler(requestParam);
        if (request != null) {
            try {
                requestParam.handleRequest(null, request, null);
            } catch (IOException ex) {
            }
        }
      
    }

    private void initConfig(VaadinRequest request) {
        if (request == null) {
            return;
        }
        if (AppVariables.pathWeb == null
                || AppVariables.pathWeb.isEmpty()) {
//            AppVerification.init();
//            Locale.setDefault(new Locale("pt","BR"));

            AppVariables.pathWeb
                    = request.getService().getBaseDirectory().getAbsolutePath();

//            AppVariables.ip = request.getRemoteAddr();
//            compararConfig();
        }
//        if (UteisProjeto.isDeveloper()
//                && UteisMetodos.isSystemWin()){//DEVELOPER
//            System.out.println("startService");
//            UteisMetodos.startService(
//                    UteisProjeto.getConfig("mysql"));
//        }
        
//        if (AppVariables.ipRemote == null
//                || AppVariables.ipRemote.isEmpty()) {
//            final HttpServletRequest http
//                    = ((VaadinServletRequest) VaadinService.getCurrentRequest())
//                    .getHttpServletRequest();
//
//            AppVariables.ipLocal = UteisMetodos.getIPLocal();
//            AppVariables.ipRemote = UteisMetodos.getIPRemote(http);
//        }
       
        if (AppVariables.urlAPP == null 
        || AppVariables.urlAPP.isEmpty()) {
            AppVariables.urlAPP = getPage().getLocation().toString();
        }
        
        Properties props = UteisProjeto.getConfig();
        
        if (AppVariables.pathAlbumEventos == null 
        || AppVariables.pathAlbumEventos.isEmpty()) {
            AppVariables.pathAlbumEventos = props.getProperty("pathAlbumEventos");
            AppVariables.pathAlbumLocalEvento = props.getProperty("pathAlbumLocalEvento");
            AppVariables.pathAlbumEstoque = props.getProperty("pathAlbumEstoque");
        }
        if (AppVariables.versionAPP == null 
        || AppVariables.versionAPP.isEmpty()) {
            AppVariables.versionAPP = "18.9.3";
//            AppVariables.versionAPP
//                    = UteisProjeto.getVersaoXML(props.getProperty("nameAPP"));
        }

        AppVariables.noThread = "true".equals(props.getProperty("noThread"));

//        System.setProperty("java.net.useSystemProxies", "true");
//        System.setProperty("http.proxyHost", "192.168.5.1");
//        System.setProperty("http.proxyPort", "3128");
//        System.setProperty("https.proxyHost", "192.168.5.1");
//        System.setProperty("https.proxyPort", "3128");
//        System.setProperty("socksProxyHost", "192.168.5.1");
//        System.setProperty("socksProxyPort", "3128");
    }
    
    private RequestHandler getParamRequest() {
        return new RequestHandler() {
            @Override
            public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
                InitContext context = new InitContext();
                context.setAplicacao(application);
                
                return context.init(request);
            }
        };
    }

    @Override
    public void detach() {
        super.detach();
        // Clean up
        getSession().removeRequestHandler(requestParam);
    }

    /*REDIRECT out TOMCAT TO FILE */
    private static void configLog(VaadinRequest request) {
        String data = UteisDate.convertDate(System.currentTimeMillis()
                , FormatData.DATA_SIMPLES);
        data = data.replaceAll("/", ".").replaceAll(":", ".");

        String root = request.getService().getBaseDirectory().getAbsolutePath();
        final File fLog = new File(root
                + File.separator + "log" + data + ".txt");

        System.out.println("LOG: " + fLog.getPath());

        try {
            if (!fLog.exists()) {
                fLog.createNewFile();
            }

            System.setOut(new PrintStream(fLog) {

                @Override
                public void println(String s) {
                    super.println();

                    String data = UteisDate.convertDate(
                            System.currentTimeMillis(), FormatData.DATA_HORA);
                    data = data.replaceAll("/", ".").replaceAll(":", ".");
                    super.println(data);
                    super.println(s);
//                    System.out.println(data);
//                    System.out.println(s);
                }

            });
           
        } catch (Exception e) {
        }
    }      
    
}
