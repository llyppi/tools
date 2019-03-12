/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.app.tela;

import br.com.utilitarios.Connection;
import br.com.utilitarios.StyleVaadin;
import br.com.utilitarios.AppVariables;
import com.vaadin.server.Resource;
import br.com.component.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.server.FileResource;
import java.io.File;
import br.com.utilitarios.AppInterface;

/**
 *
 * @author Felipe L. Garcia
 */
public class Application extends VerticalLayout implements AppInterface{
    private String dataBase;
    private String userDb;
    private String pwDb;
    private int client;
    private int userLogin;
    private int sysLogin;        
    
    private HorizontalLayout menuFooter;
    
    private Label versao;
    private AppUpdate updateApp;
    
    private Image background;
    private AbsoluteLayout absoluteBackground;
    
    private VerticalLayout viewLayout;
    private VerticalLayout content;
   

    public Application() {
   
    }
    
    @Override
    public void setParent(HasComponents parent) {        
        super.setParent(parent);

        if (parent == null) {
            return;
        }
        init();
    }

    private void init(){           
        super.setSizeFull();
        super.setMargin(false);
        super.setSpacing(false);

//        setCaption(AppVariables.byDev);
        
        absoluteBackground = new AbsoluteLayout();
        super.addComponent(absoluteBackground);                                
        
        background = new Image() {
            @Override
            public void setSource(Resource source) {
                if (source == null) {
                    super.setSource(new ThemeResource("null"));
                    return;
                }
                super.setSource(source);
            }
        };
        absoluteBackground.addComponent(background, "top:0;left:0");
        //BACKGROUD
        String fbkgd = AppVariables.pathWeb
                + File.separator+"WEB-INF"+ File.separator
                + "background.jpg";
        File img = new File(fbkgd);
        if (img.exists()) {
            setBackground(new FileResource(img));
        }
        
        content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin(false);
        content.setSpacing(false);
        absoluteBackground.addComponent(content);
        
        //LAYOUT RECEBE TODOS OS LAYOUTs SECUNDARIOS
        viewLayout = buildView();
        content.addComponent(viewLayout);
        content.setExpandRatio(viewLayout,3f);
        
//        Component telaPrincipal = new LayoutPrincipal();
//        viewLayout.addComponent(telaPrincipal);
//        viewLayout.setComponentAlignment(telaPrincipal, Alignment.TOP_CENTER);
//        viewLayout.setExpandRatio(telaPrincipal, 3f);
        
        //LAYOUT BARRA INFERIOR
        menuFooter = buildMenuFooter();
        content.addComponent(menuFooter);
        
        setVersao(AppVariables.versionAPP);                 
    }

    public Image getBackground() {
        return background;
    }

    @Override
    public void removeComponent(Component c) {
        viewLayout.removeComponent(c); 
    }        

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        viewLayout.replaceComponent(oldComponent, newComponent);
//        super.setComponentAlignment(newComponent, Alignment.TOP_CENTER);
        viewLayout.setExpandRatio(newComponent, 3f);
    }
    
    private HorizontalLayout buildMenuFooter(){
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.setMargin(false);

        Component c = buildBy();
        layout.addComponent(c);
        layout.setExpandRatio(c,3f);
        
        updateApp = new AppUpdate();
        layout.addComponent(updateApp);
        layout.setComponentAlignment(updateApp, Alignment.BOTTOM_RIGHT);
        
        c = buildMenuVersao();  
        layout.addComponent(c);  
        layout.setComponentAlignment(c,Alignment.MIDDLE_RIGHT);  
        
        return layout;
    }
    
    private VerticalLayout buildView() {
        VerticalLayout layout = new VerticalLayout();

        layout.setSizeFull();
        layout.setMargin(false);
        layout.setSpacing(false);

        return layout;
    }
    
    private HorizontalLayout buildMenuVersao() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        versao = buildVersao();
        layout.addComponent(versao);
        layout.setComponentAlignment(versao, Alignment.BOTTOM_RIGHT);

        return layout;
    }
    
    private Label buildVersao(){        
        Label label = new Label(){

            @Override
            public void setValue(String newStringValue) {
                if(newStringValue==null){
                    super.setVisible(false);
                    return ;
                }
                super.setValue("<font color=#B6B6B6>"
                        +" "+newStringValue+"</font>");
//                super.setDescription(newStringValue);
                super.setVisible(true);
            }
        };
        label.setContentMode(ContentMode.HTML);
        label.setVisible(false);
//        label.setEnabled(false);
        label.setStyleName(StyleVaadin.TEXT_RIGHT);
//        label.setHtmlContentAllowed(true);
        
        return label;
    }    

    private Label buildBy(){        
        Label label = new Label();
        label.setValue("<font color=#B6B6B6>"
                +AppVariables.byDev+"</font>");
        label.setContentMode(ContentMode.HTML);
        label.setVisible(false);
//        label.setEnabled(false);
        label.setStyleName(StyleVaadin.TEXT_RIGHT);
//        label.setHtmlContentAllowed(true);
        
        return label;
    }    

    @Override
    public void setComponentAlignment(Component childComponent, Alignment alignment) {
        viewLayout.setComponentAlignment(childComponent, alignment);
    }

    
    @Override
    public void addComponent(Component c) {
        if(viewLayout.getComponentCount()==0){
            viewLayout.addComponent(c);
        }else{
            viewLayout.replaceComponent(viewLayout.getComponent(0),c);
        }
        
        viewLayout.setExpandRatio(c, 3f);
    }
    
    @Override
    public void setCaption(String caption) {
        AppContent.get().setCaption(caption);
    }
    
//    public void setFavicon(String ico) {    
//        File f1 = new File(banco)
//        UteisFile.copyFile(file1, file2)
//    }
    public void setBackground(Resource resource) {    
//        Image background = AppContent.get().getAplicacao().getBackground();
        background.setSizeFull();
//        background.setWidth(UteisVaadin.getScreenWidth(99.9f) + "");
//        background.setHeight(UteisVaadin.getScreenHeight(88) + "");
        background.setStyleName(StyleVaadin.OPACITY);
        
        background.setSource(resource);
    }

    public void limparFooter() {
        Component c = buildMenuFooter();
        
        ((AbstractLayout)menuFooter.getParent()).replaceComponent(menuFooter,c);
        menuFooter = (HorizontalLayout) c;
        
        setVersao(AppVariables.versionAPP);
    }
    
    public void setMenuFooter(boolean visible) {
        menuFooter.setVisible(visible);
    }
    
    public HorizontalLayout getMenuFooter() {
        return menuFooter;
    }

    @Override
    public void setEnabled(boolean enabled) {
        viewLayout.setEnabled(enabled); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setVersao(String versao) {
        this.versao.setValue(versao);
    }

    public String getUserDb() {
        return userDb;
    }

    public void setUserDb(String userDb) {
        this.userDb = userDb;
    }

    public String getPwDb() {
        return pwDb;
    }

    public void setPwDb(String pwDb) {
        this.pwDb = pwDb;
    }
    
    public String getDataBase() {
        return dataBase;
    }

    public void setDataBase(String dataBase) {
        this.dataBase = dataBase;
    }

    public int getSysLogin() {
        return sysLogin;
    }

    public void setSysLogin(int sysLogin) {
        this.sysLogin = sysLogin;
    }

    public int getClient() {
        return client;
    }

    public void setClient(int client) {
        this.client = client;
    }

    public int getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(int userLogin) {
        this.userLogin = userLogin;
    }

    public AppUpdate getUpdateApp() {
        return updateApp;
    }

    public void setUpdate(String url) {
        updateApp.setLinkDownload(url);
    }

    public boolean isEnabled() {
        return viewLayout.isEnabled();
    }
        
    public Connection getConnection(){
        Connection conn = new Connection();
        
        conn.setDataBase(getDataBase());
        conn.setUser(getUserDb());
        conn.setPassword(getPwDb());
        
        return conn;
    }

//    public void notificarUso(int sistema,String banco) {
//        if (!UteisMetodos.isOnline()) {
//            return;
//        }
//        
//        String ip = UteisVaadin.getIP() +"/"+ UteisMetodos.getIP();
//
//        String nomeSistema = UteisModulos.Sistemas.getDescricao(sistema);
//
//        String assunto = nomeSistema.toUpperCase()
//                + "-WEB - " + banco;
//
//        String msg = "NOTIFICACAO DE USO <br>IP:" + ip
//                + "<br>VERSAO:" + AppVariables.versionAPP;
//
//        UteisProjeto.sendEmailSuporte(msg, assunto);
//    }

    }
