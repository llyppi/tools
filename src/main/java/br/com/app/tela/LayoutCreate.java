package br.com.app.tela;


import br.com.component.ButtonConfirm;
import br.com.component.ButtonPopUp;
import br.com.component.ButtonRefresh;
import br.com.component.HorizontalLayout;
import br.com.component.ShowMensagens;
import br.com.component.Table;
import br.com.component.TextField;
import br.com.component.UploadDrop;
import br.com.utilitarios.UteisDate;
import br.com.utilitarios.UteisFile;
import br.com.utilitarios.UteisJava;
import br.com.utilitarios.UteisMetodos;
import br.com.utilitarios.UteisProjeto;
import br.com.utilitarios.UteisVaadin;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 *
 * @author Felipe L. Garcia
 */
public class LayoutCreate extends VerticalLayout{
    
    private String AUTHOR;
    
    private Table<File> tableList;
    private TextField entity;
    private ComboBox filter;

    private List<String> listDrop;
    
    private List<PacktSufix> listPacktSufix;

    private String pathWrite;
    private String packEntity;
    private String packList;
    
    private final String MSG_SELECT = "Selecione";

    public LayoutCreate() {
       this(null);
    }
    
    public LayoutCreate(List<String> list ) {
        this.listPacktSufix = new LinkedList<>();  
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
        AppContent.get().setCaption("Java Create");
                
        super.setSizeFull();
        super.setSpacing(true);
        super.setMargin(true);
        
        String context = Page.getCurrent().getLocation().getPath();
        
        Properties properties = UteisProjeto.getConfig();              
    
        this.AUTHOR = properties.getProperty("authorClass");
        
        this.pathWrite = properties.getProperty("pathWrite");
        this.packEntity = properties.getProperty("packEntity");
        this.packList = properties.getProperty("packList");
                
        super.addComponent(buildTop());
        super.addComponent(buildBar1());
        super.addComponent(buildBar2());
        
        tableList = buildTable();                                
        Component drop = buildDrop(tableList);
        super.addComponent(drop);
//        super.addComponent(table);
        super.setExpandRatio(drop,3f);
    
        loadTable(packEntity);
        
        entity.focus();
        
        validate();
    }

    private Component buildTop() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.setSpacing(true);
    
        layout.addComponent(buildPathWrite());        
        layout.setExpandRatio(3f);   
        layout.addComponent(buildEntity());        
        layout.addComponent(buildAuthor());        
        
        return layout;
    }
        
    private Component buildPackList() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("100%");
        layout.setHeight("100px");
        layout.setSpacing(true);
        
        VerticalLayout content = new VerticalLayout();
        content.setWidth("100%");
        content.setMargin(false);
//        content.setSpacing(true);
        
        Panel panel = new Panel();
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.setContent(content);

        layout.addComponent(buildPackAdd(content));
        layout.addComponent(panel);
        layout.setExpandRatio(panel,3f);
        
        String[] listPack = packList.split(";");
        for (String pack : listPack) {
            String[] split = pack.split(":");
            
            PacktSufix packSuf = new PacktSufix(split[0], split.length > 1?split[1]:"");
            listPacktSufix.add(packSuf);
            
            content.addComponent(buildPack(packSuf));
        }
        
        return layout;
    }
    
    private Component buildAuthor() {
        TextField field = new TextField();
        field.setValue(AUTHOR);
        field.setInputPrompt("@author");
        field.setWidth("200");
        field.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String path = UteisMetodos.nz(event.getProperty().getValue(),"");
                
                if(!path.trim().isEmpty() && new File(path).exists()){
                    pathWrite=path;
                }
                loadTable(packEntity);
            }
            
        });       
        
        return field;
    }

    private Component buildEntity() {
        TextField field = new TextField();
        field.setValue(packEntity);
        field.setEnabled(false);
        field.setWidth("200");

        return field;
    }

    private Component buildPathWrite() {
        TextField field = new TextField();
        field.setValue(pathWrite);
        field.setInputPrompt("/project/src...");
        field.setWidth("100%");
        field.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String path = UteisMetodos.nz(event.getProperty().getValue(),"");
                
                if(!path.trim().isEmpty() && new File(path).exists()){
                    pathWrite=path;
                }
                loadTable(packEntity);
            }
            
        });       
        
        return field;
    }

    private Component buildPackAdd(VerticalLayout content) {
        HorizontalLayout layout = new HorizontalLayout();
//        layout.setWidth("100%");
        layout.setSpacing(true);
        
        entity = new TextField("Entidade");
        entity.setWidth("200px");
        layout.addComponent(entity);

        TextField field = new TextField("Pack");
        field.setInputPrompt("br.app");
        field.setWidth("200px");
        layout.addComponent(field);

        Button btn = new Button("+");
        btn.setTabIndex(-1);
        btn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                if(field.getValue()==null || field.getValue().trim().isEmpty()){
                    return ;
                }
                PacktSufix pack = new PacktSufix(field.getValue(), null);
                listPacktSufix.add(pack);
                
                Component c = buildPack(pack); 
                content.addComponent(c);
                UteisVaadin.getComponent(TextField.class, c).focus();
            }
        });
        layout.addComponent(btn);
        
        return layout;
    }
    
    private Component buildPack(PacktSufix pack) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.setSpacing(true);

        Button btn = new Button("Create");
        btn.setTabIndex(-1);
        btn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                File entitySelect = getSelectedNext();
                if(entitySelect==null){
                    ShowMensagens.showAlerta(MSG_SELECT);
                    return ;
                }
                createEntity(entity.getValue()
                        ,entitySelect.getName().replace(".java", "")
                        , pack);
            }
        });
        layout.addComponent(btn);
        
        TextField field = new TextField();
        field.setValue(pack.getPack());
        field.setInputPrompt("br.app.entity");
        field.setWidth("100%");
        layout.addComponent(field);
        field.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                pack.setPack(event.getProperty().getValue());
            }
        });
        layout.setExpandRatio(3f);
                
        field = new TextField();
        field.setValue(pack.getSufix());
        field.setInputPrompt("Class...Sufix");
        field.setWidth("100px");
        field.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                pack.setSufix(event.getProperty().getValue());
            }
        });
        layout.addComponent(field);
        
        btn = new Button("x");
        btn.setTabIndex(-1);
        btn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                ((AbstractLayout)layout.getParent()).removeComponent(layout);
                listPacktSufix.remove(pack);
            }
        });
        layout.addComponent(btn);
        
        return layout;
    }
    
    private CheckBox buildSelections(Object itm) {
        CheckBox chk = new CheckBox();
        chk.setTabIndex(-1);
        chk.setImmediate(true);
        chk.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (chk.getValue()) {
                    tableList.select(itm);
                }
            }
        });

        return chk;
    }

    private ComboBox buildFilter() {
        ComboBox select = new ComboBox();
        select.setWidth("300");
        select.setImmediate(true);
        select.setFilteringMode(FilteringMode.CONTAINS);
        select.setNullSelectionAllowed(false);
        select.setNullSelectionItemId("");
        select.setNewItemsAllowed(false);
        select.setMultiSelect(false);
        select.setItemCaptionMode(ItemCaptionMode.ITEM);
        select.addContainerProperty("filter", String.class, null);
        select.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                 IndexedContainer list  = 
                         (IndexedContainer) tableList.getContainerDataSource();
                 tableList.setCurrentPageFirstItemIndex(
                         list.getItemIds().indexOf(select.getValue()));
                 
                 tableList.select(filter.getValue());
            }
        });
        filter = select;
        
        return select;
    }
    
    private Component buildBar1() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.setSpacing(true);
    
        layout.addComponent(buildPackList());        
        
        return layout;
    }
    
    private Component buildBar2() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");                
        layout.setSpacing(true);                

        layout.addComponent(buildRefresh());
        layout.addComponent(buildFilter());
        layout.addComponent(buildJavaClassPop());
        layout.addComponent(buildCreate());
        layout.setExpandRatio(3f);
        
        layout.addComponent(buildDelete());
        
        return layout;
    }
    
    private Button buildJavaClassPop() {
        ButtonPopUp btn = new ButtonPopUp("JavaClass");
        btn.setTabIndex(-1);
        btn.setSpacing(true);
        btn.setMargin(true);

        btn.addComponent(buildJavaClass(FormatFile.JAVA_CLASS_LOADER));
        btn.addComponent(buildJavaClass(FormatFile.JAVA_COMPILER));
        btn.addComponent(buildJavaClass(FormatFile.JAVA_FILE));
        
        return btn;
    }
    
    private Button buildJavaClass(FormatFile format) {
        Button btn = new Button(format.name());

        btn.setTabIndex(-1);
        btn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                File entitySelect = getSelectedNext();
                if(entitySelect==null){
                    ShowMensagens.showAlerta(MSG_SELECT);
                    return ;
                }
                openClass(entitySelect,format);
                
                UteisVaadin.closePopupAll();
            }
        });

        return btn;
    }

    private Button buildCreate() {
        Button btn = new Button("Create Class");

        btn.setTabIndex(-1);
        btn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                File entitySelect = getSelectedNext();
                if(entitySelect==null){
                    ShowMensagens.showAlerta(MSG_SELECT);
                    return ;
                }
                createEntity(entity.getValue()
                        , entitySelect.getName().replace(".java", "")
                        ,getListPack());
            }
        });

        return btn;
    }
    
    private Button buildDelete() {
        ButtonConfirm btn = new ButtonConfirm("Delete"){
            @Override
            public String getMsg() {
                File[] entitys = getSelecteds();
                String msg="";
                for (File f : entitys) {
                    msg+="<br>"+f.getPath();
                }
                return msg;
            }
            
            @Override
            public void confirm() {
                File[] entitys = getSelecteds();
                
                deleteEntity(entitys,getListPack());
            }
            
        };
        btn.setTabIndex(-1);

        return btn;
    }
    
    private Button buildRefresh() {
        Button btn = new ButtonRefresh(null);

        btn.setTabIndex(-1);
        btn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                loadTable(packEntity);
            }
        });

        return btn;
    }
    
    private Component buildDrop(Component c) {
        UploadDrop drop = new UploadDrop(c) {

            @Override
            public void uploadFinished(byte[][] files, File[] fileUp) {
                for (byte[] bs : files) {
                    String txt;
                    try {
                        txt = UteisFile.read(bs);
                    } catch (Exception ex) {
                        continue;
                    }

                    String[] entitys = txt.split(String.valueOf((char) 13));

                    listDrop = new LinkedList(Arrays.asList(entitys));

                    openListDrop(listDrop);
                }
            }

            @Override
            public void uploadStart() {
             
            }          

            @Override
            public void uploadFailed(File... file) {
             
            }
        };
        drop.setFilesLimit(1);

        return drop;
    }
    
    private Table buildTable() {
        Table table =  new Table();

        table.setNullSelectionAllowed(false);
        table.setSelectable(true);
        table.setImmediate(true);
//        table.setMultiSelect(true);
        table.setWidth("100%");
        table.setHeight("100%");

        table.addContainerProperty("Tabela", String.class, null);
        table.setColumnExpandRatio("Tabela", 3f);
        
//        table.addContainerProperty("Path", String.class, null);
//        table.setColumnExpandRatio("Path", 3f);
//        
        table.addContainerProperty("chk", CheckBox.class, null);
        
        table.setColumnHeaders("Entidade", "");
        
        table.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Object[] listSelects = tableList.getSelecteds();
                if(listSelects==null){
                    return ;
                }
                //SELECIONADOS CHK TRUE
                for (Object itm : listSelects) {
                    Item i = tableList.getItem(itm);
                    CheckBox c = (CheckBox) i.getItemProperty("chk").getValue();
                    c.setValue(true);
                }
                //VERIF ITM NAO SELECIONADOS,CHK FALSE
                Object[] listAll = tableList.getValues();
                List listSelects2 = Arrays.asList(listSelects);
                
                for (Object itm : listAll) {
                    if(listSelects2.contains(itm)){//SELECIONADO
                        continue;
                    }
                    Item i = tableList.getItem(itm);
                    CheckBox c = (CheckBox) i.getItemProperty("chk").getValue();
                    c.setValue(false);
                }
            }

        });                
//        t.addShortcutListener(new ShortcutListener(
//                null, KeyCode.ENTER, null) {
//            @Override
//            public void handleAction(Object sender, Object target) {
//                if(!target.equals(t)){
//                    return ;
//                }
//                openRelacInt();
//            }
//        });
//        t.addListener(new ItemClickListener() {
//
//            public void itemClick(ItemClickEvent event) {
//                if (!event.isDoubleClick()) {
//                    return;
//                }
//                openRelacInt();
//            }
//
//          
//        });

        return table;
    }
    
    /**RETORNAR PATH DO PACKET*/
    private String getPathResource(String pack){
        pack = pack.replace(".",File.separator);
        
        File file = new File(pathWrite+File.separator+pack);
        if(file.exists()){
            return file.getPath();
        }
        System.out.println("NAO EXISTE "+file);
        return null;
    }
            
    private PacktSufix[] getListPack() {
        return listPacktSufix.toArray(new PacktSufix[listPacktSufix.size()]);
    }
    
    private void deleteEntity(File[] entitys,PacktSufix... listPack) {
        if(entitys == null || entitys.length==0) {
            ShowMensagens.showErro(MSG_SELECT);
            return ;
        }
        if(listPack == null || listPack.length==0) {
            ShowMensagens.showErro("Pack lista vazio");
            return ;
        }
        
        for (PacktSufix packtSufix : listPack) {
            if (packtSufix.getPack() == null || packtSufix.getPack().isEmpty()) {
                continue;
            }
            String path = getPathResource(packtSufix.getPack());
            if (path == null) {
                continue;
            }
            for (File ent : entitys) {
                File f = new File(path + File.separator
                        + ent.getName().replace(".java", "")
                        + packtSufix.getSufix() + ".java");
                f.delete();
            
                ShowMensagens.addMsg("Delete "+f);
            }
        }
        loadTable(packEntity);
    }
    //LISTA REGEX FIND NO FILE
    private Pattern[] getPatterns(PacktSufix[] listPack,String regex) {
//        String special = "[^A-Za-z0-9\\.\\(\\)\\;\\\"\\=\\<\\>\\,]";
        String special = "(\\s{1,}|\\(|\"|\\;|\\.)";
        
        Pattern[] list = new Pattern[]{
                    Pattern.compile("@author.*")                    
                    ,Pattern.compile("\\("+regex+"\\)")
                    ,Pattern.compile("\\<"+regex+"\\>")
                    ,Pattern.compile(regex+special)
                    ,Pattern.compile(UteisMetodos.uncapitalize(regex)+special)};                

        for (PacktSufix ps : listPack) {
            if(ps.getSufix().isEmpty()){
                continue;
            }
            list = UteisMetodos.addArray(list,
                     Pattern.compile(regex + ps.getSufix()));
            list = UteisMetodos.addArray(list,
                     Pattern.compile(UteisMetodos.uncapitalize(regex) + ps.getSufix()));
        }
        return list;
    }
    //LISTA REPLACE NO FILE
    private String[] getReplacements(PacktSufix[] listPack,String replac) {
        String[] list = new String[]{
            "@author "+AUTHOR
                        +" - " + UteisDate.convertDateISO()                    
            ,"("+replac+")"
            ,"<"+replac+">"
            ,replac+"$1"
            ,UteisMetodos.uncapitalize(replac)+"$1"};
       
        for (PacktSufix ps : listPack) {
            if(ps.getSufix().isEmpty()){
                continue;
            }
            list = UteisMetodos.addArray(list,replac + ps.getSufix());
            list = UteisMetodos.addArray(list,UteisMetodos.uncapitalize(replac) + ps.getSufix());
        }
        return list;
    }

    private void createEntity(String entity,String entityClone,PacktSufix... listPack) {
        if(entity == null || entity.trim().isEmpty()) {
            ShowMensagens.showErro("Entidade vazio");
            return ;
        }
        if(entityClone == null) {
            ShowMensagens.showAlerta(MSG_SELECT);
            return ;
        }
        if(listPack == null || listPack.length==0) {
            ShowMensagens.showErro("Pack lista vazio");
            return ;
        }        
        //LISTA REPLACEMENTS DO FILE
        Pattern[] regexp = getPatterns(listPack, entityClone);                

        String[] replacement = getReplacements(listPack, entity);

        for (PacktSufix packtSufix : listPack) {
            if (packtSufix.getPack() == null || packtSufix.getPack().isEmpty()) {
                continue;
            }
            //PATH SOURCE
            String path = getPathResource(packtSufix.getPack());
            if(path == null) {
                showErro("Não existe "+packtSufix.getPack());
                continue;
            }
            File fwrite = null;
            
            try {
                //FILE READER
                File fread = new File(path+File.separator
                        +entityClone+packtSufix.getSufix()+".java");
                //FILE WRITE
                fwrite = new File(path+File.separator
                        +entity+packtSufix.getSufix()+".java");

//                fwrite.delete();
                UteisFile.readWrite(fread, fwrite, replacement, regexp);
                
                //Gen serialVersionUID
                String pathEntity = UteisMetodos.getPathResource(packEntity); 
                String serial = UteisMetodos.getSerialVersionUID(
                        entity,packEntity,pathEntity)+"L";
                String regex;
                String replace;
                
                //SERIAL OK
                if(!"0L".equals(serial)){
                    //INSERIR SERIAL
                    if (!UteisFile.contains(fwrite, "serialVersionUID")) {
                        regex = "(public\\s*class.*\\{.*)";
                        replace = "$1\n"
                                +"\tprivate static final long serialVersionUID = "
                                +serial+";\n";
                    }else{//REPLACE SERIAL EXISTENTE
                        regex = "(serialVersionUID\\s*\\=\\s*).*\\;";
                        replace = "$1"+serial+";";
                    }
                    UteisFile.readWrite(fwrite, fwrite, replace, regex);
                }
                //CABECALHO AUTHOR
                if (!UteisFile.contains(fwrite, "@author")) {
                    regex = "(public\\s*class.*\\{)";
                    replace = "/**\n* @author "+AUTHOR
                            +" - " + UteisDate.convertDateISO()+"\n"                    
                            +"*/\n$1";
                    UteisFile.readWrite(fwrite, fwrite, replace, regex);
                }
//                ShowMensagens.showFixe(fclone.getPath());
                ShowMensagens.addMsg(fwrite.getPath());

            } catch (Exception ex) {
                showErro(ex.getMessage()
                        +"<br> Erro create "+fwrite.getName());
            }
        }
        loadTable(packEntity);
        filter.setValue(entity);
//        ShowMensagens.showFixe("Criado");
    }
    
    private File[] getEntitys(String pack) {
        String path = getPathResource(pack);
        File[] list = UteisFile.getFiles(path,".java",false);
       
        return list;
    }
    
    private void validate() {
        if (pathWrite == null || pathWrite.isEmpty()) {
           showErro("config pathWrite null");
        }
        if (!new File(pathWrite).exists()) {
            showErro("Not exist " + pathWrite);
        }
        if (packEntity == null || packEntity.isEmpty()) {
           showErro("config packEntity null");
        }
        String path = packEntity.replace(".", File.separator);
        File f = new File(pathWrite+File.separator+path);
        if (!f.exists()) {
            showErro("Not exist packEntity " + f);
        }
        if (packList == null || packList.isEmpty()) {
           showErro("config packList null");
        }
    }
    
    private void loadTable(String pack) {
        loadTable(getEntitys(pack));
    }
    
    private void loadTable(File[] list) {
        tableList.removeAllItems();
        filter.removeAllItems();
        
        if (list == null) {
            return ;
        }

        for (File itm : list) {
            Item item = tableList.addItem(itm);
            String name =itm.getName().replace(".java", "");
            
            item.getItemProperty("Tabela").setValue(name);
//            item.getItemProperty("Path").setValue(itm.getPath()
//                            .replace(itm.getName(), "")
//                            .replace(pathWrite, ""));
            item.getItemProperty("chk").setValue(buildSelections(itm));
            
            filter.addItem(itm);
            filter.getItem(itm).getItemProperty("filter").setValue(name);
        }
    }
        
    private void openClass(File entity,FormatFile format){               
        if (entity == null) {
            ShowMensagens.showAlerta(MSG_SELECT);
            return ;
        }
        String clsCode = null;
        
        if (FormatFile.JAVA_CLASS_LOADER.equals(format)) {
            clsCode = getJavaClassLoader(entity.getName().replace(".java", "")
                                ,packEntity);
        }
        if (FormatFile.JAVA_COMPILER.equals(format)) {
            clsCode = getJavaCompiler(entity);
        }
        if (FormatFile.JAVA_FILE.equals(format)) {
            clsCode = getJavaFile(entity);
        }

        if (clsCode == null) {
            showErro("Class erro " + entity);
            return ;
        }
        TextArea text = new TextArea();
        text.setSizeFull();
        text.setValue(clsCode);
        text.setWordwrap(false);
        text.setResponsive(false);
        
        Window w = new Window(format.name(),text);
        w.setWidth("30%");
        w.setHeight("80%");
        w.setResizable(false);
        w.setCloseShortcut(0, 0);
//        w.center();

        getUI().addWindow(w);
    }
        
    private void showErro(String err){        
        ShowMensagens.showErro(err);
        if(err==null){
            return ;
        }
        System.out.println(err.replace("<br>", ""+(char)13));
    }

    private void openListDrop(List<String> tables) {
        for (int i = 0; i < tables.size(); i++) {
            String tab = tables.get(i);

            if (tab.trim().isEmpty()) {
                tables.remove(i);
                i--;
            }
        }

        Window w = new Window("Lista ", new LayoutDbManager(listDrop));
        w.setWidth("80%");
        w.setHeight("80%");
        w.center();

        getUI().addWindow(w);
    }
    
    private String getJavaClassLoader(String entity, String pack) {
        Class cls = UteisMetodos.findClass(entity, pack,pathWrite);
        if (cls == null) {
            showErro("ClassLoader not found "+entity);
            return null;
        }
        return UteisJava.getJavaCode(cls);
    }
    
    private String getJavaFile(File entity) {
        try {
            return UteisFile.readByte(entity);
        } catch (Exception ex) {
            showErro(ex.getMessage());
        }
        return null;
    }
    
    private String getJavaCompiler(File entity) {
        try {
            Class cls = UteisJava.getJavaCompiler(entity);
           
            return UteisJava.getJavaCode(cls);
        } catch (Exception ex) {
            showErro(ex.getMessage());
        }
        return null;
    }
        
    private File getSelectedNext(){
        File[] list = getSelecteds();
        if (list == null) {
            return null;
        }
        return list[0];
    }
    
    private File[] getSelecteds(){
        if (!tableList.isSelected()) {
            return null;
        }
        Object[] list = tableList.getSelecteds();
        
        List<File> list2 = new ArrayList<>();
        
        for (Object itm : list) {
            Item i = tableList.getItem(itm);
            CheckBox c = (CheckBox) i.getItemProperty("chk").getValue();
            
            if (c.getValue()) {
                list2.add((File) itm);
            }
        }
        if(list2.isEmpty()){
            return null;
        }
        return list2.toArray(new File[list2.size()]);
    }

    private enum FormatFile implements Serializable{
        JAVA_CLASS_LOADER,JAVA_COMPILER,JAVA_FILE,XML;
    }
    private abstract class RunnableInput implements Serializable {

        public abstract void run(String value);

    }
    private class PacktSufix implements Serializable {
        private String pack;
        private String sufix;

        public PacktSufix(String pack, String sufix) {
            this.pack = pack;
            this.sufix = sufix;
        }

        public String getPack() {
            return pack;
        }

        public void setPack(String pack) {
            this.pack = pack;
        }
        public void setPack(Object sufix) {
            if (pack == null) {
                this.pack = null;
                return;
            }
            this.pack = String.valueOf(sufix);
        }
        public String getSufix() {
            return sufix;
        }

        public void setSufix(Object sufix) {
            if(sufix==null){
                this.sufix = null;
                return ;
            }
            this.sufix = String.valueOf(sufix);
        }
        public void setSufix(String sufix) {
            this.sufix = sufix;
        }
        
        
    }

}
