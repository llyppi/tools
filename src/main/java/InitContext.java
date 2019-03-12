
import br.com.app.tela.Application;
import br.com.app.tela.LayoutCreate;
import br.com.app.tela.LayoutDbManager;
import br.com.component.ConfigEdit;
import br.com.component.ExplorerFile;
import br.com.component.WindowPassword;
import br.com.utilitarios.ExceptionLog;
import br.com.component.ShowMensagens;
import br.com.utilitarios.UteisMetodos;
import br.com.utilitarios.UteisProjeto;
import br.com.utilitarios.UteisVaadin;
import br.com.utilitarios.AppVariables;
import br.com.utilitarios.UteisZip;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Felipe L. Garcia
 */
public class InitContext {
    private Application aplicacao;
    private String pwApp;
    
    boolean init(VaadinRequest request) {       
        if(request==null){
            return false;
        }
        if (AppVariables.pathWeb == null
                || AppVariables.pathWeb.isEmpty()) {
            AppVariables.pathWeb
                    = request.getService().getBaseDirectory().getAbsolutePath();
        }
        //SENHA APP                        
        pwApp = UteisProjeto.getConfig("pwApp");

        if(UI.getCurrent()==null){//RESQUEST SERVLET
            return false;
        }
        
        String parametro = request.getPathInfo();
        if (parametro == null || parametro.isEmpty()) {
            return false;
        }
        parametro = parametro.trim();

        String[] args = parametro.split("/");

        if (args.length==0) {
            ShowMensagens.showErro("URL incompleta");
            return false;
        }

//        initID(args);//INI ID PRIORIDADE
        
        for (String arg : args) {                        
            
            if (arg.equalsIgnoreCase("config")) {
                initConfig();
                continue;
            }
            if (arg.equalsIgnoreCase("db")) {
                initDBManager();
                continue;
            }
            if (arg.equalsIgnoreCase("create")) {
                initCreate();
                continue;
            }
            if (arg.equalsIgnoreCase("update")) {
                initUpdate();
                continue;
            }
            if (arg.equalsIgnoreCase("exp")) {
                initExplorer();
                continue;
            }
        
            if (arg.equalsIgnoreCase("logAll")) {
                initDownLogAll();
                continue;
            }
            
       
            if (arg.equalsIgnoreCase("logEx")) {
                initDownLogException();
                continue;
            }
            if (arg.equalsIgnoreCase("info")) {
                initInfoBrowser();
                continue;
            }
         
        }
        return true;
    }    
    
    
    
    private void initDBManager() {
        
        aplicacao.addComponent(new LayoutDbManager());
    }
    private void initConfig() {
        
        aplicacao.addComponent(new ConfigEdit(UteisProjeto.getConfig()));
    }
    private void initCreate() {
        
        aplicacao.addComponent(new LayoutCreate());
    }
   

    private void initPassword(Runnable runnable) {
        UI.getCurrent().addWindow(new WindowPassword(pwApp, true, true) {
            @Override
            public void run() {
                aplicacao.setUserLogin(AppVariables.USER_SUPPORT);
                runnable.run();
            }
        });
    }

    private void initExplorer() {
         initPassword(new Runnable() {
             @Override
             public void run() {
                 ExplorerFile selectFile = new ExplorerFile();
                 selectFile.setModeView(ExplorerFile.ModeSelect.VIEW_EXPLORER);

                 aplicacao.addComponent(selectFile);
                 selectFile.showExplorer();
             }
             
         });
      
    }
    
    private void initDownLogAll() {
        initPassword(new Runnable() {
            @Override
            public void run() {
                List<File> list = new ArrayList();
                
                list.add(ExceptionLog.getZipLogs());
                
                UteisMetodos.removeNull(list);
                
                File[] files = list.toArray(new File[list.size()]);
                File zip = new File(AppVariables.pathWeb
                                +File.separator+"all_logs.zip");
                
                UteisZip.zipFile(zip,files);
                
                UteisVaadin.openFile(zip);
            }
        });   
    }
  
    private void initDownLogException() {
         initPassword(new Runnable() {
             @Override
             public void run() {
                 File log = ExceptionLog.getZipLogs();

                 if (log == null || !log.exists()) {
                     ShowMensagens.showErro("Arquivo vazio");
                     return;
                 }

                 UteisVaadin.openFile(log);
             }
             
         });
      
    }

    private void initInfoBrowser() {
        VerticalLayout layout = new VerticalLayout();                

        // Create labels for the information and add them to the application
        String ipRemote = UteisVaadin.getIP();
        final Label ipAddresslabel = 
                new Label(ipRemote==null
                        ?UteisMetodos.getIPLocal()
                        :ipRemote);
        ipAddresslabel.setCaption("IP address");
        layout.addComponent(ipAddresslabel);
        
        Window win = UteisVaadin.createWindow(layout);
        UI.getCurrent().addWindow(win);
        
    }
    private void initUpdate() {
        initPassword(new Runnable() {
            @Override
            public void run() {
                aplicacao.getUpdateApp().setVisible(true);
                aplicacao.getUpdateApp().click();
            }
        });
    }

    public void setAplicacao(Application aplicacao) {
        this.aplicacao = aplicacao;
    }
    
}
