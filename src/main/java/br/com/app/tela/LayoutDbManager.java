package br.com.app.tela;


import br.com.component.AdvancedFileDownloader;
import br.com.component.ButtonDowload;
import br.com.component.ButtonPopUp;
import br.com.component.HorizontalLayout;
import br.com.component.Pagination;
import br.com.component.ShowMensagens;
import br.com.component.Table;
import br.com.component.TextField;
import br.com.component.ThemeResource;
import br.com.component.UploadDrop;
import br.com.component.UploadFile;
import br.com.utilitarios.Connection;
import br.com.utilitarios.ThreadUtil;
import br.com.utilitarios.UteisConnect;
import br.com.utilitarios.UteisDate;
import br.com.utilitarios.UteisFile;
import br.com.utilitarios.UteisMetodos;
import br.com.utilitarios.UteisProjeto;
import br.com.utilitarios.UteisSQL;
import br.com.utilitarios.UteisSQL.DBServer;
import br.com.utilitarios.UteisVaadin;
import br.com.utilitarios.UteisZip;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.BorderStyle;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.link.LinkConstants;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Felipe L. Garcia
 */
public class LayoutDbManager extends VerticalLayout{
    private Connection connection;
    private int idapp;
    private String dbURL;
    
    private DBServer dbServer;
    
    private Table<String> tableList;
    private ComboBox filter;
    private NativeSelect modeLoad;

    private List<String> listTable;
    private List<String> listErrMap;
    
    private List<String> listDrop;

    private String packEntity;

    private final String MSG_SELECT = "Selecione";
    
    public LayoutDbManager() {
       this(null);
    }
    
    public LayoutDbManager(List<String> list ) {
        this.listTable=list;
        this.listErrMap = new LinkedList<>();  
        connection = new Connection();
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
        AppContent.get().setCaption("DB Manager");
        
        super.setSizeFull();
        super.setSpacing(true);
        super.setMargin(true);
        
        String context = Page.getCurrent().getLocation().getPath();
        idapp = getID(context.split("/"));
        if (idapp <= 0) {
            showErro("ID app");
            return;
        }
        Properties properties = UteisProjeto.getConfig();
    
        this.packEntity = properties.getProperty("packEntity");
//        this.packDao = properties.getProperty("packDAO");
                
        if(!connectMaster()){
            return ;
        }                                        
        tableList = buildTable();
    
        super.addComponent(buildDbURL());
        super.addComponent(buildTop());
        super.addComponent(buildBar());
        
        Component drop = buildDrop(tableList);
        super.addComponent(drop);
//        super.addComponent(table);
        super.setExpandRatio(drop,3f);
    
        loadMode();
        modeLoad.setValue(ModeLoad.DB);
        
        filter.focus();
    }

    private Button buildDepend() {
        ButtonPopUp btn = new ButtonPopUp("Dependences");
        btn.setTabIndex(-1);
        btn.setSpacing(true);
        btn.setMargin(true);

        WebBrowser browser = Page.getCurrent().getWebBrowser();
        btn.setWidthPop(browser.getScreenWidth()*15/100);
//        pop.setHeightPop(browser.getScreenHeight()*15/100);
                
        btn.addComponent(buildDependInt());
        btn.addComponent(buildDependExt());

        return btn;
    }
    private Button buildDependInt() {
        Button b = new Button("Tabela Depend -> objs");

        b.setTabIndex(-1);
        b.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                openDependence(Dependence.INTERNO);
            }
        });

        return b;
    }
    
    private Button buildSelectAll() {
        Button b = new Button("Un/Select All");

        b.setTabIndex(-1);
        b.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                IndexedContainer container
                        = (IndexedContainer) tableList.getContainerDataSource();
                List<String> list = (List<String>) container.getItemIds();//LISTA TABLE

                for (String tab : list) {
                    Item i = container.getItem(tab);

                    CheckBox ck = (CheckBox) i.getItemProperty("chk").getValue();

                    ck.setValue(!ck.getValue());
                }
            }
        });

        return b;
    }
    private Button buildDependExt() {
        Button b = new Button("objs <- Depends Tabela");

        b.setTabIndex(-1);
        b.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                openDependence(Dependence.EXTERNO);
            }
        });

        return b;
    }
    private Button buildFields() {
        Button b = new Button("Fields");

        b.setTabIndex(-1);
        b.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                if (!tableList.isSelected()) {
                    ShowMensagens.showAlerta(MSG_SELECT);
                    return;
                }
                String table = getSelectedNext();
                
                openFields(table);
            }
        });

        return b;
    }
    
    private Button buildGravarMap(String table,List<String> fields) {
        Button b = new Button("Map");

        b.setTabIndex(-1);
        b.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
//                saveMap(table, getStr(fields));
            }
        });

        return b;
    }   
    
    private Button buildMap() {
        Button b = new Button("Map Entity");

        b.setTabIndex(-1);
        b.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                if (!tableList.isSelected()) {
                    ShowMensagens.showAlerta(MSG_SELECT);
                    return;
                }
                String table = getSelectedNext();

                createMap(table);
            }
        });

        return b;
    }   
    
    private Button buildJavaClass() {
        Button b = new Button("Java Class");

        b.setTabIndex(-1);
        b.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                if (!tableList.isSelected()) {
                    ShowMensagens.showAlerta(MSG_SELECT);
                    return;
                }
                String table = getSelectedNext();
                
                openClass(table,FormatFile.JAVA);
            }
        });

        return b;
    }
    private Button buildDelph() {
        Button b = new Button("Delph");

        b.setTabIndex(-1);
        b.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                if (!tableList.isSelected()) {
                    ShowMensagens.showAlerta(MSG_SELECT);
                    return;
                }
                String table = getSelectedNext();
                
                openClass(table,FormatFile.DELPH);
            }
        });

        return b;
    }
    private Button buildSort() {
        Button b = new Button("Sort Depends");

        b.setTabIndex(-1);
        b.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                ThreadUtil thread = new ThreadUtil();
                thread.addMetodo(new Runnable() {
                    @Override
                    public void run() {
                        sortDependsTable();
                    }
                });
                thread.start();
            }
        });

        return b;
    }
    private Button buildXML() {
        Button b = new Button("XML");

        b.setTabIndex(-1);
        b.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                if (!tableList.isSelected()) {
                    ShowMensagens.showAlerta(MSG_SELECT);
                    return;
                }
                String table = getSelectedNext();
                
                openClass(table, FormatFile.XML);
            }
        });

        return b;
    }
    
    private Component buildImportXML() {
        UploadFile up = new UploadFile("Import XML"){
            
            public void uploadFinished(File[] file, byte[][] bty) {
                
                String entidade = super.getFileName();
                entidade = entidade.replace(".xml", "");
                
                if(!importTab(bty[bty.length-1],entidade)){
                    showErro("Erro import");
                }
                super.setValueNull();
            }

            @Override
            public void uploadStart() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void uploadFailed(File... file) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        };
        up.setFileFiltro("xml");
//        up.setWidth("200");
        
        return up;
    }
    
    private Component buildTop() {
        HorizontalLayout layout = new HorizontalLayout();
//        layout.setWidth();
        layout.setSpacing(true);
            
        layout.addComponent(buildModeLoad());
        layout.addComponent(buildFilter());
        
//        layout.addComponent(new Label("<span/>",ContentMode.HTML));
//        layout.setExpandRatio(3f);
        
        return layout;
    }
    
    private Component buildBar() {
        HorizontalLayout layout = new HorizontalLayout();
//        layout.setWidth("100%");                

        layout.addComponent(buildDownDepeds());
        layout.addComponent(buildDepend());
        layout.addComponent(buildMap());
        layout.addComponent(buildFields());
        layout.addComponent(buildJavaClass());
        layout.addComponent(buildXML());
        layout.addComponent(buildSort());
//        layout.addComponent(buildMethod());        
        
        layout.addComponent(buildSQLSelect());
        layout.addComponent(buildSelectAll());
        layout.addComponent(buildDownExport());
        layout.addComponent(buildImportXML());
        
        return layout;
    }
    
    private ComboBox buildFilter() {
        ComboBox select = new ComboBox();
        select.setWidth("300");
        select.setImmediate(true);
        select.setFilteringMode(FilteringMode.STARTSWITH);
        select.setNullSelectionAllowed(false);
        select.setNullSelectionItemId("");
        select.setNewItemsAllowed(false);
        select.setMultiSelect(false);
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
    
    private NativeSelect buildModeLoad() {
        NativeSelect select = new NativeSelect();
        select.setWidth("100");
        select.setImmediate(true);
        select.setNullSelectionAllowed(false);
        select.setNewItemsAllowed(false);
        select.setMultiSelect(false);
        select.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (ModeLoad.DB.equals(modeLoad.getValue())) {
                    listTable = getTables();
                }
                if (ModeLoad.ENTITY.equals(modeLoad.getValue())) {
                    listTable = getEntitys(packEntity);
                }
                if (listTable == null) {
                    return ;
                }
                loadTable(listTable);
            }
        });
        modeLoad = select;
        
        return select;
    }
    
    private Component buildDbURL() {
        TextField field = new TextField();
        field.setValue(dbURL);
        field.setInputPrompt("url_db->mysql/firebird");
        field.setWidth("100%");
        field.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                dbURL = UteisMetodos.nz(event.getProperty().getValue(),"");
                
                if(connect(dbURL)){
                    loadTable(UteisSQL.getISQL(dbServer).getTables(connection));
                }
            }
            
        });       
        
        return field;
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
                    String[] tables = txt.split(String.valueOf((char) 13));

                    listDrop = new LinkedList(Arrays.asList(tables));

                    openListDrop(listDrop);
                }
            }

            @Override
            public void uploadStart() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void uploadFailed(File[] file) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        drop.setFilesLimit(1);
        
        return drop;
    }
    private Table buildTable() {
        Table t = new Table();

        t.setNullSelectionAllowed(false);
        t.setSelectable(true);
        t.setImmediate(true);
        t.setMultiSelect(true);
        //table_.setPageLength(100);
        t.setWidth("100%");
        t.setHeight("100%");

        t.addContainerProperty("Tabela", String.class, null);
        t.setColumnExpandRatio("Tabela", 3f);
        
        t.addContainerProperty("chk", CheckBox.class, null);
        
        t.setColumnHeaders("Tabela", "");
        t.addListener(new ValueChangeListener() {

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

        return t;
    }
    
    private int getID(String[] args) {
        for (String arg : args) {
            if (arg.toLowerCase().contains("idapp")) {
                return UteisMetodos.nz(
                        UteisMetodos.getNumber(arg),0);
            }
        }
        return 0;
    }
    private void openDependence(Dependence dependence) {
        if (!tableList.isSelected()) {
            ShowMensagens.showAlerta(MSG_SELECT);
            return;
        }        
        
//        Component c = buildDepends(dependence);
        String table = getSelectedNext();

        List<String> list2;
        if (Dependence.INTERNO.equals(dependence)) {
            list2 = UteisSQL.getISQL(dbServer).getDependsInt(table,connection);
        } else {
            list2 = UteisSQL.getISQL(dbServer).getDependsExt(table,connection);
        }
        Component c = new LayoutDbManager(list2);
        
        Window w = new Window("Depends "+dependence.name(),c);
        w.setWidth("80%");
        w.setHeight("80%");
        w.center();

        getUI().addWindow(w);      
        
//        tableDepends.focus();
    }
    
    private void openListDrop(List<String> tables) {       
        for (int i = 0; i < tables.size(); i++) {
            String tab = tables.get(i);
            
            if(tab.trim().isEmpty()){
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
    
    private void openSQLInsert(String table) {       
        String[] fields = UteisSQL.getISQL(dbServer).getColumns(table, connection);
        
        
      
    }
    
//    private void openMethod(FormatFile formatFile) {
//        IndexedContainer container
//                = (IndexedContainer) table.getContainerDataSource();
//        List<String> list = (List<String>) container.getItemIds();//LISTA TABLE
//
//        String txt = "";
//        String err = "";
//
//        for (String tab : list) {
//            Item i = container.getItem(tab);
//
//            CheckBox ck = (CheckBox) i.getItemProperty("chk").getValue();
//
//            if (!ck.getValue()) {
//                continue;
//            }
//
//            String txt2 = null;
//            if (FormatFile.IMPORT.equals(formatFile)) {
//                txt2 = getTxtImport(tab);
//            }
//            if (FormatFile.IMPORT2.equals(formatFile)) {
//                txt2 = getTxtImport2(tab);
//            }
//
//            if (txt2 != null) {
//                txt += (char) 13 + txt2;
//            } else {
//                err += (char) 10 + tab;
//            }
//        }
//        TextArea textArea = new TextArea();
//        textArea.setSizeFull();
//        textArea.setValue(txt);
//
//        Window w = new Window(null, textArea);
//        w.setWidth("50%");
//        w.setHeight("80%");
//        w.center();
//
//        getUI().addWindow(w);
//
//        if (!err.isEmpty()) {
//            showErro("Erro " + err);
//        }
//    }
    private void openFields(String table) {        
        
//        IndexedContainer list = new IndexedContainer(getFields(tab));
        String[] fields  = UteisSQL.getISQL(dbServer).getColumns(table,connection);
        
        Table tb =  new Table(null);
        tb.setNullSelectionAllowed(false);
        tb.setSelectable(true);
        tb.setImmediate(true);
        tb.setSizeFull();
        tb.addContainerProperty("Field", String.class, null);
        tb.setColumnExpandRatio("Field", 3f);

        for (String f : fields) {
            Item item = tb.addItem(f);//ADD
            item.getItemProperty("Field").setValue(f);
        }
      
        Window w = new Window(null,tb);
        w.setWidth("25%");
        w.setHeight("80%");
//        w.center();

        getUI().addWindow(w);      
    }
    
    private List<Object> getSelect(String table,int pag) {        
        Class cls = 
                UteisMetodos.findClass(table,packEntity);
        if (cls == null) {
            showErro("Classe não encontrada "+table);
            return null;
        }
        Map map = UteisSQL.getAttribColumn(table,packEntity,connection);
        if (map == null) {
            showErro("Tabela sem map/attrColumn " + table);
            return null;
        }
        List<Map> list
                = UteisSQL.getISQL(dbServer).getListMap(table, map                        
                        , connection
                        ,pag,100);
        
        List<Object> list2 = new ArrayList<>();
        for (Iterator<Map> it = list.iterator(); it.hasNext();) {
            map = it.next();
            
            Object obj = UteisMetodos.toObject(map, cls);
            
            list2.add(obj);
        }
        return list2;
    }
    
    private void openTable(List<Object> list) {        
        if(list.isEmpty()){
            showErro("Lista vazia");
            return ;
        }       
        Class clas = list.get(0).getClass();
                
        Method[] listGet = UteisMetodos.getMethods(clas);
        List<Method> listGET = new ArrayList<>();

        Table tb =  new Table();
        tb.setNullSelectionAllowed(false);
        tb.setSelectable(true);
        tb.setImmediate(true);
        tb.setSizeFull();
        
        for (Method mGet : listGet) {
            
            listGET.add(mGet); 
            //ATRIBUTO ENTIDADE
            String atributo = mGet.getName().replaceAll("get", "");
//            atributo = atributo.toLowerCase();

            //TIPO PARAM DO METODO
            Class typeParam = mGet.getReturnType();
            typeParam = UteisMetodos.getSuperType(typeParam);
            
            tb.addContainerProperty(atributo, typeParam, null);
        }
//        String colum = listMethod[0].getName().replaceAll("get", "");
//        tb.setSortContainerPropertyId(colum);
        
        for (Object obj : list) {
            try {
                Item item = tb.addItem(obj);//ADD
                
                for (Method m : listGET) {
                    String atributo = m.getName().replaceAll("get", "");
                    
                    item.getItemProperty(atributo).setValue(m.invoke(obj));
                }
            
            } catch (Exception ex) {
                
            }
        }
        Window w = new Window(clas.getSimpleName(),tb);
        w.setWidth("80%");
        w.setHeight("100%");
//        w.center();

        getUI().addWindow(w);      
    }
    
    private void openTable(String table) {   
        int size = UteisSQL.getCount(table, "CODIGO",connection);
        if (size < 1) {
            showErro("Lista vazia");
            return ;
        }
        Class cls = UteisMetodos.findClass(table, packEntity);
        if (cls == null) {
            showErro("Classe não encontrada "+table);
            return ;
        }
        Method[] listGet = UteisMetodos.getMethods(cls);
        List<Method> listGET = new ArrayList<>();

        Table tb =  new Table();
        tb.setNullSelectionAllowed(false);
        tb.setSelectable(true);
        tb.setImmediate(true);
        tb.setSizeFull();
        
        for (Method mGet : listGet) {
            
            listGET.add(mGet); 
            //ATRIBUTO ENTIDADE
            String atributo = mGet.getName().replaceAll("get", "");
//            atributo = atributo.toLowerCase();

            //TIPO PARAM DO METODO
            Class typeParam = mGet.getReturnType();
            typeParam = UteisMetodos.getSuperType(typeParam);
            
            tb.addContainerProperty(atributo, typeParam, null);
        }
//        String colum = listMethod[0].getName().replaceAll("get", "");
//        tb.setSortContainerPropertyId(colum);                
        
        //PAGINACAO
        Pagination pag = new Pagination(100, tb) {
            @Override
            public void loadTable(int pag) {
                super.getTable().removeAllItems();
                
                List lst = getSelect(table, pag);
                if(lst==null){
                    return ;
                }
                for (Object obj : lst) {
                    try {
                        Item item = super.getTable().addItem(obj);//ADD

                        for (Method m : listGET) {
                            String atributo = m.getName().replaceAll("get", "");

                            item.getItemProperty(atributo).setValue(m.invoke(obj));
                        }

                    } catch (Exception ex) {

                    }
                }
            }
        };
        pag.setCountItem(size);
        
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        layout.addComponent(tb);
        layout.setExpandRatio(tb,3F);

        pag.loadTable(1);
        layout.addComponent(pag);
        
        Window w = new Window(cls.getSimpleName(),layout);
        w.setWidth("80%");
        w.setHeight("100%");
//        w.center();
        
        getUI().addWindow(w);      
    }
    private void openNoMaps() {        
        
        Table t =  new Table(listErrMap.size()+"");
        t.setNullSelectionAllowed(false);
        t.setSelectable(true);
        t.setImmediate(true);
        t.setSizeFull();
        t.addContainerProperty("Tabela", String.class, null);
        t.setColumnExpandRatio("Tabela", 3f);
        t.addListener(new ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (!event.isDoubleClick()) {
                    return;
                }
                openEditMap((String) event.getItemId());
            }

          
        });
        for (String itm : listErrMap) {
            if (t.containsId(itm)) {
                continue;
            }
            Item item = t.addItem(itm);//ADD
            item.getItemProperty("Tabela").setValue(itm);
        }
      
        Window w = new Window("Sem Map",t);
        w.setWidth("25%");
        w.setHeight("80%");
//        w.center();

        getUI().addWindow(w);      
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
    
    private Component buildDownDepeds() {
        ButtonPopUp btn = new ButtonPopUp("Download Depends");
        btn.setIcon(new ThemeResource("images/download16x16.png"));      
        btn.setTabIndex(-1);
        btn.setSpacing(true);
        btn.setMargin(true);

        WebBrowser browser = Page.getCurrent().getWebBrowser();
        btn.setWidthPop(browser.getScreenWidth()*15/100);
//        pop.setHeightPop(browser.getScreenHeight()*15/100);

        btn.addComponent(buildDownDependsJava());
        btn.addComponent(buildDownDependsXML());
        btn.addComponent(buildDownDependsDelph());        
//        pop.addComponent(buildDownMethodImport(tabs));
//        pop.addComponent(buildDownMethodImport2(tabs));

        return btn;
    }
    
    private Component buildDownDependsJava() {
        final ButtonDowload btn = new ButtonDowload("Depends Java");

        btn.addListener(new AdvancedFileDownloader.DownloaderListener() {
            public void beforeDownload(Resource resource) {
                IndexedContainer list
                            = (IndexedContainer) tableList.getContainerDataSource();

                byte[] b = downDepends(list,FormatFile.JAVA);
                
                if (b != null && b.length > 0) {
                    StreamResource.StreamSource source = new StreamResource.StreamSource() {
                        public InputStream getStream() {
                            return new ByteArrayInputStream(b);
                        }
                    };

                    StreamResource resource2
                            = new StreamResource(source,  "doc.txt");
                    
                    btn.setDownload(resource2);
                }else{
                    showErro("Sem dependencia");
                    btn.setDownload(null);
                }

            }
            
        });

        return btn;
    }       
    
    private Component buildDownDependsDelph() {
        final ButtonDowload btn = new ButtonDowload("Depends Delph");

        btn.addListener(new AdvancedFileDownloader.DownloaderListener() {
            public void beforeDownload(Resource resource) {                
                IndexedContainer list
                            = (IndexedContainer) tableList.getContainerDataSource();
                
                byte[] b = downDepends(list,FormatFile.DELPH);
                
                if (b != null && b.length > 0) {
                    StreamResource.StreamSource source = new StreamResource.StreamSource() {
                        public InputStream getStream() {
                            return new ByteArrayInputStream(b);
                        }
                    };

                    StreamResource resource2
                            = new StreamResource(source, "doc.txt");

                    btn.setDownload(resource2);
                }else{
                    showErro("Sem dependencia");
                    btn.setDownload(null);
                }

            }
            
        });

        return btn;
    }       
    
    private Component buildDownDependsXML() {
        final ButtonDowload btn = new ButtonDowload("Depends XML");

        btn.addListener(new AdvancedFileDownloader.DownloaderListener() {
            public void beforeDownload(Resource resource) {
                IndexedContainer list
                            = (IndexedContainer) tableList.getContainerDataSource();
                
                byte[] b = downDepends(list,FormatFile.XML);
                
                if (b != null && b.length > 0) {
                    StreamResource.StreamSource source = new StreamResource.StreamSource() {
                        public InputStream getStream() {
                            return new ByteArrayInputStream(b);
                        }
                    };

                    StreamResource resource2
                            = new StreamResource(source,  "doc.xml");
                    
                    btn.setDownload(resource2);
                }else{
                    btn.setDownload(null);
                }

            }
            
        });

        return btn;
    }       
    
    private Component buildDownMethodImport(IndexedContainer list) {
        final ButtonDowload btn = new ButtonDowload("Method Import");

        btn.addListener(new AdvancedFileDownloader.DownloaderListener() {
            public void beforeDownload(Resource resource) {
                byte[] b = downDepends(list,FormatFile.IMPORT);
                
                if (b != null && b.length > 0) {
                    StreamResource.StreamSource source = new StreamResource.StreamSource() {
                        public InputStream getStream() {
                            return new ByteArrayInputStream(b);
                        }
                    };

                    StreamResource resource2
                            = new StreamResource(source,  "doc.txt");
                    
                    btn.setDownload(resource2);
                }else{
                    btn.setDownload(null);
                }

            }
            
        });

        return btn;
    }       
    private Component buildDownMethodImport2(IndexedContainer list) {
        final ButtonDowload btn = new ButtonDowload("Method Import2");

        btn.addListener(new AdvancedFileDownloader.DownloaderListener() {
            public void beforeDownload(Resource resource) {
                byte[] b = downDepends(list,FormatFile.IMPORT2);
                
                if (b != null && b.length > 0) {
                    StreamResource.StreamSource source = new StreamResource.StreamSource() {
                        public InputStream getStream() {
                            return new ByteArrayInputStream(b);
                        }
                    };

                    StreamResource resource2
                            = new StreamResource(source,  "doc.txt");
                    
                    btn.setDownload(resource2);
                }else{
                    btn.setDownload(null);
                }

            }
            
        });

        return btn;
    }       
    
    private Component buildDownExport() {
        Button btn = new Button("Export Table");
        btn.setIcon(new ThemeResource("images/download16x16.png"));      
        btn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                exportXML(getSelecteds());
            }
        });
        return btn;
    }           

    private Component buildSQLSelect() {
        Button btn = new Button("Select Table");
        btn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                if (!tableList.isSelected()) {
                    ShowMensagens.showAlerta(MSG_SELECT);
                    return;
                }
                String table = getSelectedNext();
                
                openTable(table);
            }
        });
        return btn;
    }           
   
    private void loadMode() {
        modeLoad.addItem(ModeLoad.DB);
        modeLoad.addItem(ModeLoad.ENTITY);
    }
    
    private void loadTable(String[] list) {
        loadTable(Arrays.asList(list));
    }
    private void loadTable(List<String> list) {
        if (list == null) {
            return ;
        }
        tableList.removeAllItems();
        filter.removeAllItems();

        for (String tab : list) {
            if (tableList.containsId(tab)) {
                continue;
            }
            Item item = tableList.addItem(tab);
            item.getItemProperty("Tabela").setValue(tab);
            item.getItemProperty("chk").setValue(buildSelections(tab));
            
            filter.addItem(tab);
        }
    }
        
    private void openClass(String table,FormatFile format){               
        String classe = null;
        
        if (FormatFile.JAVA.equals(format)) {
            classe = getJavaClass(table,packEntity);
        }
        if (FormatFile.XML.equals(format)) {
            Class cls = UteisMetodos.findClass(table,packEntity);
            classe = UteisMetodos.getXMLAttrib(cls);
        }
//        if (FormatFile.DELPH.equals(format)) {
//            classe = getClassDelph(table);
//        }
        
        TextArea text = new TextArea();
        text.setSizeFull();
        text.setValue(classe);
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
    
    private byte[] downDepends(IndexedContainer list,FormatFile format) {   
        List<String> list2;
        
        if (listDrop != null && !listDrop.isEmpty()) {
            list2 = new ArrayList<>(listDrop);//LISTA DROPED
        }else{
            list2 = (List<String>) list.getItemIds();//LISTA TABLE
        }
        
        //CACHE TXT GRAVADO NO ARQUIVO
        List<String> cacheOUT = new ArrayList<>();
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        listErrMap.clear();
                
        for (String tab : list2) {
            Item i = list.getItem(tab);

            CheckBox ck = (CheckBox) i.getItemProperty("chk").getValue();
            
            if (!ck.getValue()) {
                continue;
            }
              
            downDepends(tab, out, cacheOUT,format);
        }
        try {
            out.flush();
            out.close();
        } catch (IOException ex) {
        }
        
        if (!listErrMap.isEmpty()) {
            openNoMaps();
        }
            
        return out.toByteArray();
    }       
    
    private boolean importTab(byte[] bty,String entidade) {    
        if (bty == null) {
            showErro("Arquivo vazio");
            return false;
        }
//        if(event.getProperty().getValue()==null){
//            return ;
//        }
//        File file = new File((String) event.getProperty().getValue());
//        event.getProperty().setValue(null);
        ByteArrayInputStream input = new ByteArrayInputStream(bty);
        
        List list = UteisMetodos.getList(input,entidade,packEntity);
        if (list == null) {
            showErro("Lista vazia");
            return false;
        }
//        String cnpj = file.getName();
//        cnpj = cnpj.substring(18, cnpj.length());//CNPJ_=15
//        cnpj = cnpj.replaceAll("\\.xml", "");

//        Class clas = 
//                UteisMetodos.findClass(entidade,packEntity);

        openTable(list);

        return true;
    }
    
    private void exportXML(String... tables) {      
        if (tables == null) {
            ShowMensagens.showAlerta(MSG_SELECT);
            return ;
        }
        listErrMap.clear();                       

        try {
            File fExport = getFileExport();
            
            if(!UteisSQL.exportXML(connection, packEntity, fExport)){
                throw new Exception("Erro export");
            }
            
            downZip(fExport);
            
        } catch (Exception ex) {
            ShowMensagens.showErro(ex.getMessage());
        }
    }        

    
    private void downDepends(String table,OutputStream out
                            ,List<String> cacheOUT,FormatFile format) {
        
//        if (out == null) {
//            try {
//                out = new ByteArrayOutputStream();
//            } catch (Exception ex) {
//            }
//        }
        if (cacheOUT == null) {
            cacheOUT = new ArrayList<>();
        }
        if (cacheOUT.contains(table)) {
            return ;
        }
        
        List<String> list = 
                UteisSQL.getISQL(dbServer).getDependsInt(table,connection);

        for (String tab : list) {
            downDepends(tab, out,cacheOUT,format);//RECURCIVO
        }
        String txt = null;
        if (FormatFile.JAVA.equals(format)) {
            txt = getJavaClass(table,packEntity);
        }
        if (FormatFile.XML.equals(format)) {
            Class cls = UteisMetodos.findClass(table,packEntity);
            txt = UteisMetodos.getXMLAttrib(cls);
        }
//        if (FormatFile.DELPH.equals(format)) {
//            txt = getClassDelph(table);
//        }
//        if (FormatFile.IMPORT.equals(format)) {
//            txt = getTxtImport(table);
//        }
//        if (FormatFile.IMPORT2.equals(format)) {
//            txt = getTxtImport2(table);
//        }
        
        if (txt == null) {
            listErrMap.add(table);
            return ;
        }

        try {
            out.write(txt.getBytes(), 0, txt.getBytes().length);
        } catch (IOException ex) {
            return;
        }

        //CACHE out
        cacheOUT.add(table);
    }        
    
    private void openEditEntidade(RunnableInput exec){
        TextField text = new TextField();
        text.setSizeFull();        

        Button btn = new Button("Edit");
        
        VerticalLayout layout = new VerticalLayout(text,btn);
        layout.setSizeFull();
        
        Window w = new Window("Entidade",layout);
        w.setWidth("25%");
        w.setHeight("10%");
        w.setResizable(false);
        w.setCloseShortcut(0, 0);
        w.center();
        
        btn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                w.close();
                exec.run(text.getValue());
            }
        });
        
        getUI().addWindow(w);
    }
    
    private void openEditMap(String table){        
        showErro("Campo não encontrado " + table);
        
        String[] fields = UteisSQL.getISQL(dbServer).getColumns(table,connection);
        List<String> list = new LinkedList<String>(Arrays.asList(fields));
        
        VerticalLayout layout = new VerticalLayout();
        
        layout.addComponent(new Label("Campo VAZIO remove"));
                
        String value;
        
        for (int i = 0; i < list.size(); i++) {
            String field = list.get(i);

            TextField text = new TextField();            
            
            String atributo = UteisMetodos.findAttribLike(table, field,packEntity);
            if (atributo!=null) {
//                text.setEnabled(false);
                text.setWidth("80%");
                
                value=atributo+"="+field;
            }else{
                text.setWidth("100%");
                value="="+field;
            }
            text.setValue(value);
            
            list.remove(i);
            list.add(i, value);
                    
            text.addListener(new ValueChangeListener() {

                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    list.clear();
                    for (int i = 0; i < layout.getComponentCount(); i++) {
                        if (!TextField.class.isInstance(layout.getComponent(i))) {
                            continue;
                        }
                        TextField c = (TextField) layout.getComponent(i);
                        
                        String val = UteisMetodos.nz(c.getValue(), "");
                        if (!val.trim().isEmpty()) {//VAZIO REMOVE
                            list.add(val);
                        }
                    }
                }
            });
            
           
            layout.addComponent(text);
        }
        
        layout.addComponent(buildGravarMap(table, list));
        
        Window w = new Window("Mapear Entidade "+table, layout);
        w.setWidth("30%");
        w.setHeight("80%");
        w.setResizable(false);
        w.setCloseShortcut(0, 0);
        w.center();
      
        getUI().addWindow(w);
        
        openClass(table,FormatFile.JAVA);
    }
    
    private File getFileMap(String table){        
        Class clsEntidade = UteisMetodos.findClass(
                table, packEntity);

        if (clsEntidade == null) {
            return null;
        }
        String root = UteisProjeto.getConfig("pathMapEntity");

        if (root.isEmpty()) {
            root = getUI().getSession().getService()
                    .getBaseDirectory().getAbsolutePath();
        }
        File fmap = new File(new File(root), clsEntidade.getSimpleName() + ".map");

        return fmap;
    }
    
    private File getFileExport(){        
        String root = UteisProjeto.getConfig("pathExport");
        
        if(root.isEmpty()){
            root = getUI().getSession().getService()
                    .getBaseDirectory().getAbsolutePath();
        }        
        return new File(root);
    }
    
    private String getStr(Map list){        
        String map="";
                
        for (Object object : list.entrySet()) {
            map += (char) 13 + object.toString();
        }
        
        return map;
    }
    
    private void createMap(String table){
        String java = getJavaClass(table,packEntity);
        
        if (java == null) {
            showErro("Entidade não encontrada "+table);
            
            openEditEntidade(new RunnableInput() {
                @Override
                public void run(String value) {
                    createMap(value);
                }
            });
            return;
        }
        
        Map map = UteisSQL.getAttribColumn(table,packEntity,connection); 
        if (map == null) {
            openEditMap(table);
            return;
        }
        
        saveMap(table, map);
    }
    
    private void saveMap(String table,Map map){
        File fmap = getFileMap(table);   
        fmap.delete();
       
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        out.write(map.getBytes());
        
//        if(!UteisFile.writeFile(fmap, map)){
//            showErro("Erro ao criar arquivo"
//                    + fmap.getName());
//        }        
        UteisVaadin.openFile(map.toString().getBytes(),fmap.getName());
        
        ShowMensagens.show("Map criado " + fmap.getName());
    }    
   
    private boolean connect(String banco) {
        Connection conn = new Connection();
        conn.setDataBase(banco);
        conn.setUser(connection.getUser());
        conn.setPassword(connection.getPassword());

        if (!UteisConnect.getConn(conn).isConnected()) {
            showErro(conn.getLog());
            return false;
        }
        conn.close();
    
        connection.setDataBase(banco);

        dbServer = UteisSQL.getServer(connection);
        
        if(UteisSQL.getISQL(dbServer)==null){
            showErro("Interface not found "+dbServer.name());
            return false;
        }
        ShowMensagens.show("Connect");
        
        return true;
    }
    private boolean connectMaster() {
        Connection conDB = UteisConnect.getConn(UteisProjeto.getConfig("dbmaster"));
        try {
            
            conDB.setFixConn(true);

            if(!conDB.isConnected()){
                throw new Exception(conDB.getLog());
            }
            String ip = UteisSQL.get("select ip from sisclientes"
                    + " where codigo=" + idapp, "ip", conDB);
            String porta = UteisSQL.get("select porta from sisclientes"
                    + " where codigo=" + idapp, "porta", conDB);
            this.dbURL = UteisSQL.get("select banco from sisclientes"
                    + " where codigo=" + idapp, "banco", conDB);
            String usuarioDB = UteisSQL.get("select user from sisclientes"
                    + " where codigo=" + idapp, "user", conDB);
            String senhaDB = UteisSQL.get("select PW from sisclientes"
                    + " where codigo=" + idapp, "PW", conDB);
    //        this.driver = properties.getProperty("driverDB");
    //        this.urlPrefix = properties.getProperty("urlPrefixDB");

            dbURL = ip+":"+porta+"/"+dbURL;
                        
            connection.setDataBase(dbURL);
            connection.setUser(usuarioDB);
            connection.setPassword(senhaDB);
            
            connect(dbURL);
            
            return true;
        } catch (Exception e) {
            showErro(e.getMessage());
        }finally{
            conDB.close();
            connection.close();
        }
        return false;     
    }
    
    
    private void openLog() {
        final String data = UteisDate.convertDate(
                System.currentTimeMillis()
                , UteisDate.FormatData.DATA_SIMPLES)
                .replaceAll("/", ".").replaceAll(":", ".");

        String root = getUI().getSession().getService()
                        .getBaseDirectory().getAbsolutePath();
        
        final File froot = new File(root);
        File[] list = froot.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("log")
                        && name.contains(data)
                        && name.endsWith(".txt");
            }
        });
        if(list.length == 0){
            showErro("Log vazio");
            return ;
        }
        Resource resource = new FileResource(froot);
                
        Link link = new Link(null, resource);
        link.setImmediate(true);
        link.setVisible(false);
        link.beforeClientResponse(false);
        link.setTargetName("_blank");//_blank ABRIR EM NOVA JANELA
        super.addComponent(link);
        
        ResourceReference reference
                = new ResourceReference(resource, link, LinkConstants.HREF_RESOURCE);
        
        UI.getCurrent().getPage().open(
                reference.getURL(), "_blank", -1,-1, BorderStyle.DEFAULT);
    }

    private String findField(String table,String atributo) {
        Map<String, Object> map = UteisSQL.getAttribColumn(table,packEntity,connection); 
        if (map == null) {
            return null;
        }
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            
            if (key.equalsIgnoreCase(atributo)) {
                return UteisMetodos.nz(entry.getValue(),"");
            }
        }
        return null;
    }

    private void downZip(File path) {
        File[] list = path.getParentFile()
                .listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        File fzip = new File(path.getParentFile()
                                    , "export.zip");

//        ByteArrayOutputStream outZIP = new ByteArrayOutputStream();
//        String nameZip = new Random().nextLong()
//                +"export.zip";
        
        if(!UteisZip.zipFile(fzip,list)){
            showErro("Erro ao zipar");
            return ;
        }
//        Resource resource = new StreamResource(
//                ()->new ByteArrayInputStream(outZIP.toByteArray())
//                , nameZip);
        Resource resource = new FileResource(fzip);
        
        super.addComponent(new Link(fzip.getName(),resource));
    }

    private void sortDependsTable() {
        String[] tables = UteisSQL.getISQL(dbServer).getTables(connection);
        
        List<String> listSort = new LinkedList<>();
        
        for (String tab : tables) {
            sortDependsTable(tab, listSort);
        }
        
        String[] arr = listSort.toArray(new String[listSort.size()]);
        String arrStr = UteisMetodos.join(arr, String.valueOf((char)13));
        
        TextArea txt = new TextArea();
        txt.setValue(arrStr);
        txt.setSizeFull();
                
        Window w = new Window("Depends ", txt);
        w.setWidth("80%");
        w.setHeight("80%");
        w.center();

        getUI().addWindow(w);

    }
    
    private void sortDependsTable(String table,List<String> list) {
        if (list == null) {
            return ;
        }
        
        if (list.contains(table)) {
            return;
        }
        
        List<String> depeds = 
                UteisSQL.getISQL(dbServer).getDependsInt(table,connection);
        
        for (String tab : depeds) {
            sortDependsTable(tab, list);//RECURCIVO
        }
        
        for (String tab : list) {
            List<String> dp = 
                    UteisSQL.getISQL(dbServer).getDependsExt(tab,connection);
            
            if(dp.contains(table)){//POSSUI DEPENDENCIA
               int idx = list.indexOf(tab);
               list.add(idx, table);
               
               return ;
            }
        }
        
        list.add(table);
    }
    
    private void showErro(String err){
        ShowMensagens.showErro(err);
        System.out.println(err);
    }    

    private List<String> getTables() {
        String[] list = UteisSQL.getISQL(dbServer).getTables(connection);
        if (list == null) {
            return null;
        }
        
        return Arrays.asList(list);
    }
    private List<String> getEntitys(String pack) {
        List<Class> list = UteisMetodos.getClass(pack);
        if(list==null){
            return null;
        }
        List<String> list2 = new ArrayList<>();
        
        for (Class cls : list) {
            list2.add(cls.getSimpleName());
        }
        return list2;
    }
    public static String getJavaClass(String table, String pack) {
        Class cls = UteisMetodos.findClass(
                table, pack);
        if (cls == null) {
//            ShowMensagens.showErro("Entidade não encontrada "+table);
            System.out.println("Entidade não encontrada " + table);
            return null;
        }
        //LISTA METODOS
        Method[] listSET = UteisMetodos.getMethods(cls);

        String declare = "";
        for (Method set : listSET) {
            //ATRIBUTO ENTIDADE
            String atributo = set.getName().replaceAll("get", "");
            atributo = UteisMetodos.capitalize(atributo.toLowerCase());

            //TIPO PARAM DO METODO
            Class typeParam = set.getReturnType();
            String type = typeParam.getSimpleName();

            //DECLARACAO DO ATRIBUTO
            declare += (char) 13 + ""
                    + (char) 9
                    + "private " + type + " " + atributo + ";";
        }
        String classe = (char) 13 + "public class "
                + cls.getSimpleName() + " {" + (char) 13;
        classe += declare + (char) 13 + "}";

        return classe;
    }
    
    private String getSelectedNext() {
        String[] list = getSelecteds();
        if (list == null) {
            return null;
        }
        return list[0];
    }
    private String[] getSelecteds(){
        if (!tableList.isSelected()) {
            ShowMensagens.showAlerta(MSG_SELECT);
            return null;
        }
        Object[] tables = tableList.getSelecteds();
        
        List<Object> list2 = new ArrayList<>();
        
        for (Object table : tables) {
            Item i = tableList.getItem(table);
            CheckBox c = (CheckBox) i.getItemProperty("chk").getValue();
            
            if (c.getValue()) {
                list2.add(table);
            }
        }
        return list2.toArray(new String[list2.size()]);
    }
    
    private enum Dependence implements Serializable{
        INTERNO,EXTERNO;
    }
    private enum FormatFile implements Serializable{
        JAVA,XML,DELPH,IMPORT,IMPORT2;
    }

    public static enum ModeLoad implements Serializable{
        DB, ENTITY;
        
        @Override        
        public String toString() {
            return super.name();
        }
    }    
    private abstract class RunnableInput implements Serializable{
        
        public abstract void run(String value);
    
    }
}
