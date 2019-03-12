/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.app.tela;

import br.com.component.Button;
import br.com.component.ButtonConfirm;
import br.com.component.ConfigEdit;
import br.com.component.ButtonPopUp;
import br.com.component.ComboSelect;
import br.com.component.PopupList;
import br.com.component.PopupSelect;
import br.com.component.ExplorerFile;
import br.com.component.ExplorerFile.ModeSelect;
import br.com.component.MultLayout;
import br.com.component.WindowConfirm;
import br.com.utilitarios.Connection;
import br.com.utilitarios.ExceptionLog;
import br.com.utilitarios.Ftp;
import br.com.utilitarios.ProgressTread;
import br.com.component.ShowMensagens;
import br.com.utilitarios.StyleVaadin;
import br.com.utilitarios.ThreadVaadin;
import br.com.utilitarios.UteisDate;
import br.com.utilitarios.UteisDate.FormatData;
import br.com.utilitarios.UteisMetodos;
import br.com.utilitarios.UteisProjeto;
import br.com.utilitarios.UteisVaadin;
import br.com.utilitarios.AppVariables;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import br.com.component.ThemeResource;
import br.com.utilitarios.UteisSecurity;
import br.com.utilitarios.UteisFile;
import br.com.utilitarios.UteisRuntime;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;
import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Random;

/**
 *
 * @author Felipe L. Garcia
 */
public class AppUpdate extends ButtonPopUp{
    private VerticalLayout content;
    private ListSelect listLink;
    
    private String linkDownload;

    private final String msgAguarde  = "Aguarde a Atualização"
                            + "<br>Em aproximadamente 1min, recarregue a página";
    private final String msgErrArquivo = "Arquivo de atualização não existe";
    private final String msgErr = "Erro ao Atualizar."
            + "<br>Verifique arquivo baixado e tente Continuar Atualização";

    private final String msgErrDest = "Destino Download não definido"
                    + " no config,nao existe,sem permissão";
    
    private final String msgDownload = "Download concluido.";
    private final String msgDownloadErr = "Erro ao fazer o download.";
    private final String msgPackteErr = "Não existem pacotes para atualizar";
    private final String msgPackteSelect = "Se o arquivo de atualização for apenas complementar."
                        + "<br>Selecione o local dos arquivos(pack1.zip,2...)";
    
    public AppUpdate() {
//        init();
    }
    
    private enum UpdateMode implements Serializable{
        DOWNLOAD,CONTINUACAO,PACK
    }
    
    @Override
    public void setParent(HasComponents parent) {
        super.setParent(parent);    
        
        if(parent==null){
            return ;
        }
        init();
    }
    
    private void init(){
        super.setCaption("Atualização da Aplicação");
        super.setDescription("Somente usuário Administrador");
        super.setVisible(false);
        super.setTabIndex(-1);
        super.setStyleName(StyleVaadin.BUTTON_RED);
        super.addStyleName(Runo.BUTTON_LINK);        
        super.addPopupVisibilityListener(new PopupView.PopupVisibilityListener() {
            
            @Override
            public void popupVisibilityChange(PopupView.PopupVisibilityEvent event) {
                if(!event.isPopupVisible()){
                    return ;
                }
                
                if(listLink.size()>0){
                    return ;
                }
                
                checkUpdate(linkDownload,listLink);
            }
        });
        super.setWidthPop(UteisVaadin.getScreenWidth(50));
        super.setHeightPop(UteisVaadin.getScreenHeight(80));
        
        content = new  VerticalLayout();
        content.setSizeFull();
        content.setSpacing(true);
        
        super.addComponent(content);
                
        content.addComponent(buildListLink());
        
        content.addComponent(buildBarra());
    }

    private Component buildUpload(){        
        Button btn = new Button("Upload/Atualizar");
//        btn.setDisableOnClick(true);
        btn.setDescription("Fazer upload");
        btn.setIcon(new ThemeResource("images/upload16x16.png"));        
        btn.addListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(listLink.isEmpty()){
                    ShowMensagens.showAlerta("Lista vazia");
                    return ;
                }
                AppUpdate.this.getPop().setPopupVisible(false);

                String linkDownload = UteisMetodos.nz(listLink.getValue(), "");

                confirmUpdate(linkDownload, UpdateMode.DOWNLOAD);
            }
        });
        return btn;
    }
    
    private Component buildContinuar(){        
        Button btn = new Button("Continuar/Atualizar");
//        btn.setDisableOnClick(true);
        btn.setDescription("Se download do arquivo completo já foi concluido."
                    + "<br>Selecione o local do arquivo");
        btn.setIcon(new ThemeResource("images/continue16x16.png"));
        btn.addListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                AppUpdate.this.getPop().setPopupVisible(false);

                confirmUpdate(UpdateMode.CONTINUACAO);
            }
        });
        return btn;
    }
    private Component buildAtualizacao(){        
        Button  btn = new Button("Atualização/Packs");
//        btn.setDisableOnClick(true);
        btn.setDescription(msgPackteSelect);
        btn.setIcon(new ThemeResource("images/SelectFile/rar16x16.png"));
        btn.addListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                AppUpdate.this.getPop().setPopupVisible(false);

                confirmUpdate(UpdateMode.PACK);
            }
        });
        return btn;
    }
    
    private Component buildListLink(){        
        listLink = new ListSelect("Links"){

            @Override
            public void setContainerDataSource(Container newDataSource) {
                super.setContainerDataSource(newDataSource);
                
                Collection c = newDataSource.getItemIds();
                if(c.size() > 0){
                    super.select(c.iterator().next());
                }
            }
        };        
        listLink.setWidth("100%");
        listLink.setNullSelectionAllowed(false);
        listLink.setNewItemsAllowed(true);
        listLink.setImmediate(true);
        
        return listLink;
    }
    
    private Component buildMenu(){        
        ButtonPopUp  pop = new ButtonPopUp(){
            @Override
            public void addComponent(Component c) {
                super.addComponent(c);
                
                if(!Button.class.isInstance(c)){
                    return ;
                }
                ((Button)c).addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        UteisVaadin.closePopupAll();
                    }
                });
            }
            
        };
        pop.setIcon(new ThemeResource("images/config24x24.png"));
        pop.setTabIndex(-1);
        pop.setStyleName(Runo.BUTTON_LINK);
        pop.setMargin(false);
        pop.setSpacing(false);

        pop.addPopupVisibilityListener(new PopupVisibilityListener() {
            @Override
            public void popupVisibilityChange(PopupVisibilityEvent event) {
                if (!event.isPopupVisible()) {
                    pop.removeAllComponents();
                    return;
                }
                pop.addComponent(buildConfigPop());
                pop.addComponent(buildExplorer());
                pop.addComponent(buildScript());
                pop.addComponent(buildRenameApp());
                pop.addComponent(buildTomcat());
                pop.addComponent(buildRuntimePop());
            }
        });
        
        return pop;
    }
    
    private Component buildScript() {
        Button btn = new Button("Script");
        btn.setIcon(new ThemeResource("images/database16x16.png"));
        btn.setStyleName(Runo.BUTTON_LINK);
        btn.addListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                PopupView pop = UteisVaadin.getPopupView(event.getButton());
//                pop.setPopupVisible(false);
//                         
//                Application aplicacao = 
//                        (Application) AppContent.get().getApplication();
//                Connection conn = aplicacao.getConnection();
                
//                Window w = UteisVaadin.createWindow(new LayoutDataBase(AppVariables.USER_SUPPORT
//                                            , conn));
//                w.setModal(true);
//                w.setResizable(false);
//                w.setWidth("80%");
//                w.setHeight("100%");
//                getUI().addWindow(w);
            }
        });
        return btn;
    }
    
    private Component buildTomcat() {
        ButtonPopUp pop = new ButtonPopUp("Tomcat");
        pop.setIcon(new ThemeResource("images/tomcat16x16.png"));
        pop.setTabIndex(-1);
        pop.setStyleName(Runo.BUTTON_LINK);
        pop.setWidthPop(UteisVaadin.getScreenWidth(50)+"");
        
        ExplorerFile select1 = new ExplorerFile("jarTomcat");
        select1.setValue(AppVariables.pathWeb+ File.separator 
                                    +"update"+ File.separator+ "tomcatUtil.jar");
        select1.setWidth("100%");
        select1.setFiltroFile("jar");
        pop.addComponent(select1);
        
        ExplorerFile select2 = new ExplorerFile("Javahome");
        select2.setValue(UteisMetodos.getJavaDir());
        select2.setWidth("100%");
        select2.setModeSelect(ModeSelect.FILE_PATH);
        pop.addComponent(select2);
        
        ButtonConfirm btn = new ButtonConfirm("Restart") {

            @Override
            public void confirm() {
                ShowMensagens.showErro("Recarregue a página");
                
//                AppVerification.tomcatUtil(select1.getValue()
//                                            ,select2.getValue()
//                                            ,new String[]{"restart"});
            }
        };
        btn.setIcon(new ThemeResource("images/tomcat16x16.png"));
        
        pop.addComponent(btn);
        
        return pop;
    }    
    
    private Component buildConfig(ButtonPopUp pop){        
        HorizontalLayout layout = new HorizontalLayout();

        Button btn = new Button();
        btn.setIcon(new ThemeResource("images/refresh16x16.png"));
        btn.setStyleName(Runo.BUTTON_LINK);
        layout.addComponent(btn);

        ComboSelect cb = new ComboSelect();
        
        cb.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (pop.getComponentCount() > 1) {
                    pop.removeComponent(1);
                }

                String value = (String) event.getProperty().getValue();
                File file = new File(value);
                if (!file.exists()) {
                    return;
                }

                Component c = new ConfigEdit(UteisMetodos.getProperties(file));
                pop.addComponent(c);
                pop.setExpandRatio(c, 3f);
            }
        });
        layout.addComponent(cb);
        cb.setNewItemsAllowed(true);

        ExplorerFile selectFile = new ExplorerFile() {
            @Override
            public void setParent(HasComponents parent) {
                super.setParent(parent);

                super.setCaptionButton("Diretório");
                super.setStyleName(Runo.BUTTON_LINK);

                super.setValue(AppVariables.pathWeb
                        + File.separator
                        + "WEB-INF");
            }

        };
        selectFile.setFiltroFile(".properties");
        selectFile.setModeView(ModeSelect.VIEW_EXPLORER);
        selectFile.setModeSelect(ModeSelect.FILE_PATH);
        selectFile.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                cb.removeAllItems();

                String path = UteisMetodos.nz(event.getProperty().getValue(), "");

                if (path.isEmpty()) {
                    return;
                }
                File dir = new File(path);
                if (!dir.isDirectory()) {
                    return;
                }
                File[] list = dir.listFiles();
                if (list == null) {
                    return;
                }
                for (File f : list) {
                    cb.addItem(f.getPath());
                }
            }
        });
        layout.addComponent(selectFile);

        btn.addListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                selectFile.setValue(null);
                selectFile.setValue(AppVariables.pathWeb);
            }
        });
        return layout;
    }
    private Component buildConfigPop(){        
        ButtonPopUp pop = new ButtonPopUp("Config");

        pop.setWidthPop(UteisVaadin.getScreenWidth(50));
        pop.setHeightPop(UteisVaadin.getScreenHeight(50));
        pop.setIcon(new ThemeResource("images/config16x16.png"));
        pop.setTabIndex(-1);
        pop.setStyleName(Runo.BUTTON_LINK);
        
        pop.addPopupVisibilityListener(new PopupView.PopupVisibilityListener() {
            @Override
            public void popupVisibilityChange(PopupView.PopupVisibilityEvent event) {
                if (!event.isPopupVisible()) {
                    pop.removeAllComponents();
                    return;
                }
                Component c = buildConfig(pop);
                pop.addComponent(c);
                pop.setComponentAlignment(c, Alignment.MIDDLE_RIGHT);
            }
        });        
        return pop;
    }
    
    private Component buildRuntimePop(){        
        ButtonPopUp pop = new ButtonPopUp("Runtime");

        pop.setWidthPop(UteisVaadin.getScreenWidth(50));
        pop.setHeightPop(UteisVaadin.getScreenHeight(50));
        pop.setIcon(new ThemeResource("images/config16x16.png"));
        pop.setTabIndex(-1);
        pop.setStyleName(Runo.BUTTON_LINK);
        
        pop.addPopupVisibilityListener(new PopupView.PopupVisibilityListener() {
            @Override
            public void popupVisibilityChange(PopupView.PopupVisibilityEvent event) {
                if (!event.isPopupVisible()) {
                    pop.removeAllComponents();
                    return;
                }
                Method[] list = UteisRuntime.class.getMethods();
                
                for (Method m : list) {
                    if(!Modifier.isStatic(m.getModifiers())){
                        continue;
                    }
                    m.setAccessible(true);
                            
                    pop.addComponent(new com.vaadin.ui.Button(m.getName()
                            , new ClickListener() {
                        @Override
                        public void buttonClick(ClickEvent event) {
                            try {
                                m.invoke(null);
                                ShowMensagens.show("Executado");
                            } catch (Exception ex) {
                                ShowMensagens.showErro(ex.getMessage());
                            }
                        }
                    }));
                }
                
            }
        });        
        return pop;
    }
    
    private Component buildExplorer(){        
        ExplorerFile selectFile = new ExplorerFile(){
            @Override
            public void setParent(HasComponents parent) {
                super.setParent(parent); 
                
                super.setCaptionButton("Explorer");
                super.setStyleName(Runo.BUTTON_LINK);
            }
            
        };        
        selectFile.setModeView(ModeSelect.VIEW_EXPLORER);        
                
        return selectFile;
    }
    
    private Component buildRename(final TextField... fields) {
        Button btn = new Button("Rename");
        btn.addListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String oldName = UteisMetodos.nz(fields[0].getValue(), "");
                String newName = UteisMetodos.nz(fields[1].getValue(), "");
                 
                renameApp(oldName, newName);
            }
        });
        return btn;
    }
    
    private Component buildRenameApp(){        
        final ButtonPopUp  pop = new ButtonPopUp("RenameApp");
        pop.setIcon(new ThemeResource("images/SelectFile/java16x16.png"));
        pop.setTabIndex(-1);
        pop.setStyleName(Runo.BUTTON_LINK);
        pop.setSpacing(true);
        
        final TextField textField1 = new TextField("Nome atual");
        final TextField textField2 = new TextField("Nome novo");
        
        pop.addPopupVisibilityListener(new PopupView.PopupVisibilityListener() {
            @Override
            public void popupVisibilityChange(PopupView.PopupVisibilityEvent event) {
                if (!event.isPopupVisible()) {
                    pop.removeAllComponents();
                    return;
                }
                pop.addComponent(textField1);
                pop.addComponent(textField2);
                pop.addComponent(buildRename(textField1, textField2));
                
                File fWeb = new File(AppVariables.pathWeb);
                textField1.setValue(fWeb.getName());
            }
        });
        
        return pop;
    }
    
    private Component buildBarra(){        
        MultLayout layout = new MultLayout();
        
        layout.addComponent(buildUpload());
        layout.addComponent(buildContinuar());
        layout.addComponent(buildAtualizacao());
        layout.addComponent(buildMenu());
        
        return layout;
    }

    /**@param file SE EXISTIR LISTA DE ARQUIVOS,SERA O SELECIONADO*/
    private void confirmUpdate(UpdateMode tipo) {
        confirmUpdate(null, tipo);
    }
    /**@param file SE EXISTIR LISTA DE ARQUIVOS,SERA O SELECIONADO*/
    private void confirmUpdate(String linkDownload,UpdateMode tipo) {
        if(linkDownload!=null && linkDownload.contains("ftp:")){
            Ftp ftp = new Ftp(linkDownload);
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

            //SE NAO ACHOU FILE NA URL
            if(ftp.getFileFtp()==null || ftp.getFileFtp().trim().isEmpty()){
                ftp.setFileFtp(linkDownload);
            }

            linkDownload = ftp.toString();
        }

        if(UpdateMode.DOWNLOAD.equals(tipo)){
            final String download = linkDownload;
            
            getUI().addWindow(new WindowConfirm("Fazer download ?"
                    + "<br>"+linkDownload) {

                @Override
                public void confirm() {
                    uploadUpdateThread(download,null);
                }
            });

            return ;
        }
        if(UpdateMode.CONTINUACAO.equals(tipo)){
            getUI().addWindow(new WindowConfirm("Excluir ?") {

                @Override
                public void confirm() {
                    continueUpdateCheck(null);
                }
            });
            
            return ;
        }
        if(UpdateMode.PACK.equals(tipo)){
            pacoteUpdateCheck(new File(AppVariables.pathWeb));
        }
    }
    
    /**PROCURA ARQUIVO JA BAIXADO E CONTINUAR ATUALIZAÇÃO*/
    public void continueUpdateCheck(File destDownload) {
        //DESTINO DOWNLOAD                          
        if (destDownload == null) {
            System.out.println("Destino do Download não definido config,ou não existe");
            
            PopupSelect select = new PopupSelect() {
                @Override
                public void executar(File value) {
                    continueUpdateCheck(value);
                }
            };
            select.getSelect().setCaption("Destino do arquivo.WAR");
            select.getSelect().setModeSelect(ExplorerFile.ModeSelect.FILE_PATH);

            UteisVaadin.addPopUp(select);
            
            return ;
        }

        if(!destDownload.exists()){
            ShowMensagens.showErro(msgErrArquivo);
            continueUpdateCheck(null);
            return ;
        }

        String[] listFileDownload = destDownload.list(new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {
                 return string.toLowerCase().contains(".war");
            }
        });
        
        if (listFileDownload == null || listFileDownload.length==0) {
            ShowMensagens.showErro(msgErrArquivo);
            
            PopupSelect select = new PopupSelect("Destino do arquivo.WAR") {
                @Override
                public void executar(File value) {
                    continueUpdateCheck(value);
                }
            };
            select.getSelect().setModeSelect(ExplorerFile.ModeSelect.FILE_PATH);
            
            UteisVaadin.addPopUp(select);
            
            return;
        }
        
//        if(listFileDownload.length==1){
//            continueUpdateThread(new File(listFileDownload[0]));
//            return ;
//        }
        
//        if(destDownload.getName().endsWith(File.separator)){
//            destDownload=destDownload.getName().substring(
//                            0, destDownload.getName().length()-1);
//        }
        //CRIAR ENDERECO COMPLETO DO ARQUIVO
        for (int i = 0; i < listFileDownload.length; i++) {
            listFileDownload[i] = destDownload+File.separator
                                    +listFileDownload[i];
        }
        //MOSTRAR LISTA DE ARQUIVOS ENCONTRADOS
        UteisVaadin.addPopUp(new PopupList(listFileDownload, PopupList.ModeList.OPTION) {
            @Override
            public void executar(String[] list) {
                continueUpdateThread(new File(list[0]));
            }
        });
    }
    
    private void continueUpdateThread(File fileDownload) {        
        final ThreadVaadin thread = new ThreadVaadin();
        thread.addMetodo(new Runnable() {
            @Override
            public void run() {
                update(fileDownload.getPath());
            }
        });
        thread.setForcedThread(true);
        thread.setRunFinally(new RunThread() {

            @Override
            public void run() {                
                if(Boolean.FALSE.equals(thread.getValueReturn())){
                    ShowMensagens.showErro(msgErr);
//                    thread.closeWin();
                    return;
                }
                
                ShowMensagens.showFixe(msgAguarde);
                ShowMensagens.showAlertJavaScript(msgAguarde );
            }
        });
        
        thread.start();        
    }
    
    private void pacoteUpdateCheck(File path) {
        if(path==null){
            return ;
        }
       
        String[] packs = path.list(new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {
                return string.toLowerCase().contains("_pack");
            }

        });
        if (packs==null || packs.length == 0) {
            ShowMensagens.showErro(msgPackteErr);
            UteisVaadin.addPopUp(buildPopupSelectPacks(path));
            return;
        }
        
        Arrays.sort(packs,new Comparator<String>() {
            public int compare(String a, String b) {
                return a.compareToIgnoreCase(b);
            }
        });
        
        for (int i = 0; i < packs.length; i++) {
            packs[i] = path.getPath()
                    +File.separator+packs[i].toLowerCase();
        }
        
        UteisVaadin.addPopUp(buildPopupListPacks(packs));
    }
    
    public void pacoteUpdateThread(String[] packs) {        
        final String files="\""+UteisMetodos.join(packs, ";")+"\"";
        
        final ThreadVaadin thread = new ThreadVaadin();
        thread.addMetodo(new Runnable() {
            @Override
            public void run() {
                update(files);
            }
        });
        thread.setRunFinally(new RunThread() {

            @Override
            public void run() {
                if(Boolean.FALSE.equals(thread.getValueReturn())){
                    ShowMensagens.showErro(msgPackteErr);
//                    thread.closeWin();
                    return;
                }
                
                ShowMensagens.showFixe(msgAguarde );
                ShowMensagens.showAlertJavaScript(msgAguarde );
            }
        });
        thread.setForcedThread(true);
        thread.start();        
    }
    
    private void uploadUpdateThread(String linkDownload,File fdest) {
        if (fdest == null) {
            System.out.println(msgErrDest);

            final String link2 = linkDownload;

            PopupSelect select = new PopupSelect() {
                @Override
                public void executar(File file) {
                    uploadUpdateThread(link2, file);
                }
            };
            select.getSelect().setModeSelect(ExplorerFile.ModeSelect.FILE_PATH);

            UteisVaadin.addPopUp(select);

            return ;
        }
        
        ProgressTread progress = new ProgressTread();
//        progress.msg = msgWin;
        progress.sufix = "%";
        
        final ThreadVaadin thread = new ThreadVaadin();
        thread.addMetodo(new RunThread() {
            @Override
            public void run() {
                uploadUpdate(linkDownload, fdest, progress);
            }
        });
        thread.setProgressTread(progress);
        thread.setForcedThread(true);
        thread.setRunFinally(new RunThread() {

            @Override
            public void run() {
                if(thread.getValueReturn()==null){
                    ShowMensagens.showErro(msgDownloadErr);
//                    thread.closeWin();
                    return;
                }
                                               
                File fileDownload = (File) thread.getValueReturn();
                
                getUI().addWindow(
                        new WindowConfirm("Executar atualização?") {

                    @Override
                    public void confirm() {
                        update(fileDownload.getPath());
                    }
                });
            }
        });
        
        thread.start();
    }

    /**CARREGA A LISTA DE ATUALIZACOES REFERENTE AO LINK
     * @param linkDownload HTTP,FTP : ACESSA DIR E RETORNA FILES
     * @param lista LISTA SERA CARREGADA
     */
    private void checkUpdate(String linkDownload,ListSelect lista){
        lista.removeAllItems();
        
        if(linkDownload==null || linkDownload.isEmpty()){
            return ;
        }
        //http
        if(!linkDownload.contains("ftp:") ){
            Collection c = Arrays.asList(linkDownload);
            lista.setContainerDataSource(new IndexedContainer(c));
            lista.setRows(1);
            return ;
        }       
        //ftp
        String[] path = UteisFile.getFilesFTP(linkDownload);
        if(path!=null){
            Collection c = Arrays.asList(path);
            lista.setContainerDataSource(new IndexedContainer(c));
            lista.setRows(path.length);
        }
        
    }

    private PopupView buildPopupSelectPacks(File fpath){ 
        PopupSelect select = new PopupSelect(fpath.getPath()) {
            @Override
            public void executar(File value) {
                pacoteUpdateCheck(value);
            }
        };
        select.getSelect().setModeSelect(ExplorerFile.ModeSelect.FILE_PATH);
                
        return select;
    }
    
    private PopupView buildPopupListPacks(final String[] packs){ 
        return new PopupList(packs, PopupList.ModeList.CHECK) {
            @Override
            public void executar(String[] list) {
                pacoteUpdateThread(list);
            }
        };
    }

    public void setLinkDownload(String linkDownload) {
        this.linkDownload = linkDownload;
        
        super.setDescription(linkDownload);
        super.setVisible(linkDownload != null);

        listLink.removeAllItems();
    }
   
    public boolean update(String fileDownload) {
        System.out.println("Atualização..."
                + UteisDate.convertDate(System.currentTimeMillis()
                        , FormatData.DATA_HORA));
                
        ShowMensagens.showAlertJavaScript(msgAguarde);

        File fWebapps = new File(AppVariables.pathWeb).getParentFile();
        
        File folderNewJar = new File(fWebapps.getPath() 
                                    + File.separator + "update");
        
        File fileNewJar = new File(folderNewJar
                                + File.separator + "update.jar");                
        
        String pathJar = fileNewJar.getPath();        
        
        File folderJar = new File(AppVariables.pathWeb
                + File.separator 
                +"update");
         
        System.out.println(folderJar.getPath());
        
        if (!folderJar.exists()) {
            ExceptionLog.log("JAR NAO ENCONTRATO: "+folderJar);
            return false;
        }
        
        UteisFile.copyFile(folderJar, folderNewJar);

        System.out.println("JAR COPIADO: ");
        System.out.println("================" + folderNewJar.getPath());

        String fJava;

        if (!UteisMetodos.isSystemWin()) {
            String varJavahome = UteisProjeto.getConfig("pathJavaHome");
            
            if(varJavahome==null || varJavahome.trim().isEmpty()){
                 varJavahome = System.getenv("JAVA_HOME");
                 
                 if(varJavahome==null){
                     ExceptionLog.log("JAVA_HOME não definido");
                     return false;
                 }
            }
            
            fJava = varJavahome
                    + File.separator + "bin"
                    + File.separator + "java";
        } else {
            pathJar = ("\"" + pathJar + "\"");
            
            fJava = "\"" + System.getProperty("java.home")
                    + File.separator + "bin"
                    + File.separator + "java\"";
        }
        //1-JVM OU JRE.....java.exe
        //-jar
        //2-*.JAR 
        //void main(String[] args)
        //3-(WAR,_PACK.ZIP)
        String[] cmd =  {fJava
                ,"-jar"
                ,pathJar
                ,fileDownload.trim()};

        System.out.println("EXECUTANDO JAR");
        System.out.println("cmd : " + String.join(" ",cmd));

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();

            return true;
        } catch (Exception ex) {
            ExceptionLog.log(ex);
        } finally {
            try {
                p.waitFor();
            } catch (InterruptedException ex) {

            }
        }
        return false;
    }

    public boolean renameApp(String oldName, String newName) {
        System.out.println("renameApp..."
                + UteisDate.convertDate(System.currentTimeMillis()
                        , FormatData.DATA_HORA));                        

        File fWebapps = new File(AppVariables.pathWeb).getParentFile();
        
        File fJar = new File(AppVariables.pathWeb
                + File.separator 
                +"update"
                + File.separator
                +"rename.jar");
        
        File fNewJar = new File(fWebapps.getPath()
                                    + File.separator + "rename.jar");
         
        if (!fJar.exists()) {
            ExceptionLog.log("JAR NAO ENCONTRATO: "+fJar);
            return false;
        }
        
        UteisFile.copyFile(fJar, fWebapps);

        System.out.println("JAR COPIADO: ");
        
        String fJava;

        if (!UteisMetodos.isSystemWin()) {
            String pathJavaHome = UteisProjeto.getConfig("pathJavaHome");
            
            if(pathJavaHome==null || pathJavaHome.trim().isEmpty()){
                 pathJavaHome = System.getenv("JAVA_HOME");
                 
                 if(pathJavaHome==null){
                     ExceptionLog.log("JAVA_HOME não definido");
                     return false;
                 }
            }
            
            fJava = pathJavaHome
                    + File.separator + "bin"
                    + File.separator + "java";
        } else {
            fJava = "\"" + System.getProperty("java.home")
                    + File.separator + "bin"
                    + File.separator + "java\"";
        }
        //1-JVM OU JRE.....java.exe
        //-jar
        //2-*.JAR 
        //void main(String[] args)
        //String oldName, String newName
        String[] cmd =  {fJava
                ,"-jar"
                ,fNewJar.getPath()
                ,oldName,newName};

        System.out.println("EXECUTANDO JAR");
        System.out.println("cmd : " + String.join(" ",cmd));
        
        ShowMensagens.showAlertJavaScript(msgAguarde);
        ShowMensagens.showAlertJavaScript(AppVariables.urlAPP.replaceAll(oldName, newName));
        
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();

            return true;
        } catch (Exception ex) {
            ExceptionLog.log(ex);
        } finally {
            try {
                p.waitFor();
            } catch (InterruptedException ex) {

            }
        }
        return false;
    }

    private File uploadUpdate(String link,File fdest,ProgressTread progress) {
        
        if (fdest == null) {
            ShowMensagens.showErro("Destino não definido");
            return null;
        }
        
        if (link == null || link.isEmpty()) {
            ShowMensagens.showErro("Link não definido");
            return null;
        }

//        if(!linkDownload.contains("ftp")){
//            if (!UteisMetodos.isOnline(UteisMetodos.getDomain(linkDownload))) {
//                return null;
//            }
//        }

        System.out.println("link upload " + link);

        final String msgErr ="Upload Falhou";

        String[] split = link.split("/");
        String fileName = split[split.length - 1];

        File fileDownload = new File(fdest, fileName);

        File fileTemp = new File(fileDownload.getParent()
                , new Random().nextLong() + "");
        
        if (link.contains("ftp:")) {
            Ftp ftp = new Ftp(link);
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

            //SE NAO ACHOU FILE NA URL
            if (ftp.getFileFtp() == null || ftp.getFileFtp().trim().isEmpty()) {
                ftp.setFileFtp(link);
            }

            link = ftp.toString();
        }
        boolean dowloadOk = false;

        try {
            if (link.contains("http:")) {
                dowloadOk = UteisFile.downloadFile(
                        new URL(link), fileTemp, false, progress);
                
            } else if (link.contains("ftp:")) {
                Ftp ftp = new Ftp(link);
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
//                System.out.println("FTP Download: " + ftp);

                dowloadOk = UteisFile.downloadFile(ftp, fileTemp
                        , false, false, progress);
            }
            
            if (!dowloadOk) {
                ShowMensagens.showErro(msgErr);
                throw new Exception();
            }
            //FILE NAO FOI CRIADO
            if (!fileTemp.exists()) {                
                ShowMensagens.showErro(msgErr);
                throw new Exception();
            }
            fileDownload.delete();//SE EXISTIR
            fileTemp.renameTo(fileDownload);
    
            System.out.println("upload " + fileDownload);
            
        } catch (Exception ex) {
            ExceptionLog.log(msgErr);
            ExceptionLog.log(ex);
            
            fileTemp.delete();
        }
        
        System.out.println(msgDownload + fileDownload.length() / 1024 + " kb");
        ShowMensagens.show(msgDownload);

        return fileDownload;
    }
    
}
