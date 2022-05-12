package br.com.app.tela;

import br.com.component.AdvancedFileDownloader;
import br.com.component.ButtonConfirm;
import br.com.component.ButtonDowload;
import br.com.component.ButtonRefresh;
import br.com.component.HorizontalLayout;
import br.com.component.ShowMensagens;
import br.com.component.Table;
import br.com.component.ThemeResource;
import br.com.component.UploadDrop;
import br.com.utilitarios.AppVariables;
import br.com.utilitarios.UteisProjeto;
import com.google.common.io.ByteStreams;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import io.storj.Access;
import io.storj.BucketInfo;
import io.storj.BucketIterator;
import io.storj.BucketListOption;
import io.storj.ObjectInfo;
import io.storj.ObjectInputStream;
import io.storj.ObjectIterator;
import io.storj.ObjectListOption;
import io.storj.ObjectOutputStream;
import io.storj.Project;
import io.storj.Uplink;
import io.storj.UplinkOption;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Felipe L. Garcia
 */
public class LayoutUplink extends VerticalLayout{
    
    private static Access access;
    private static UplinkOption[] uplinkOptions;
    private static Uplink uplink;
    
    private Table<File> tableList;
    private ComboBox filter;
    private Button delete;
    
    private List<String> listDrop;
    
    private String pathUplink;
            
    public LayoutUplink() {
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
        AppContent.get().setCaption("Uplink");
                
        super.setSizeFull();
        super.setSpacing(true);
        super.setMargin(true);

        String accessGrant = getAccessGrant();
        this.access = Access.parse(accessGrant);
//        String filesDir = System.getProperty("java.io.tmpdir");

        Properties properties = UteisProjeto.getConfig();              
        AppVariables.properties = properties;       
    
        this.pathUplink = properties.getProperty("pathUplink");
        
        this.uplinkOptions = new UplinkOption[]{
                UplinkOption.tempDir(pathUplink),
        };
        this.uplink = new Uplink(uplinkOptions);
        
        super.addComponent(buildTop());
        
        this.tableList = buildTable();                                
        Component drop = buildDrop(tableList);
        super.addComponent(drop);
//        super.addComponent(table);
        super.setExpandRatio(drop,3f);
    
        loadTable("");
        
        filter.focus();
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
        select.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
//        select.addContainerProperty("filter", String.class, null);
        select.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
//                if(filter.getItemCaption(filter.getValue()).contains("REMOVED"))
//                    return;
                if(filter.getItemIds().isEmpty())
                    return;
                if(filter.getValue()!=null 
                && tableList.getItem(filter.getValue().toString())==null)
                {
                    loadTable(getObjects(filter.getValue().toString()),filter.getValue().toString());
                    return;
                }
                IndexedContainer list  = 
                        (IndexedContainer) tableList.getContainerDataSource();
                tableList.setCurrentPageFirstItemIndex(
                        list.getItemIds().indexOf(filter.getValue()));

                if(getSelecteds()!=null){
                    for (Item i : getSelecteds()) {
                        tableList.unselect(i.getItemProperty("Name").getValue());
                    }
                }
                tableList.select(filter.getValue());
                tableList.focus();
            }
        });
        filter = select;
        
        return select;
    }
    
    private Component buildTop() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");                
        layout.setSpacing(true);                

        layout.addComponent(buildUp());
        layout.addComponent(buildRefresh());
        layout.addComponent(buildFilter());
        layout.setExpandRatio(3f);
//        delete = buildDelete();
//        layout.addComponent(delete);
        
        return layout;
    }
    
    private Button buildDelete() {
        ButtonConfirm btn = new ButtonConfirm("Delete"){

            @Override
            public void confirm() {
                delete(getSelecteds());
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
                refresh();
            }
        
        });

        return btn;
    }
    
    private Button buildDownload(String bucket,String fileName) {
        ButtonDowload btn = new ButtonDowload("");
       
        btn.setTabIndex(-1);
        btn.addDetachListener(new AdvancedFileDownloader.DetachListener() {
            @Override
            public void detach(DetachEvent event) {
                
            }
        });
        btn.addListener(new AdvancedFileDownloader.DownloaderListener() {
                        
            public void beforeDownload(Resource resource) {                
                
                String fileDown = pathUplink+File.separator+fileName;
                Project project = uplink.openProject(access);
                InputStream input = download(bucket, fileDown,project);                
                File f = new File(fileDown);
                
                if (input == null || !f.exists()){
                    showErro("Erro download " + fileName);
                    btn.setDownload(null);
                    return;
                }
//                btn.setDownload(new FileResource(f));
                btn.setDownload(new StreamResource(new StreamResource.StreamSource() {
                    @Override
                    public InputStream getStream() {
//                        return ByteStreams.limit(input,Long.MAX_VALUE);
                        return input;
                    }
                }, f.getName()));
            }
        });
        
        return btn;
    }
    
    private Button buildUp() {
        Button btn = new Button();
        btn.setIcon(new ThemeResource("images/arrow-left_double24x24.png"));
        btn.setTabIndex(-1);
        btn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                loadTable("");
            }
        });

        return btn;
    }
    
    private Component buildDrop(Component c) {
        UploadDrop drop = new UploadDrop(c) {

            @Override
            public void uploadFinished(byte[][] files, File[] fileUp) {
                for (File f : fileUp) {
                    try {
                        String b = tableList.getDescription() == null 
                                ? (filter.getValue() == null
                                    ? "":  filter.getValue().toString())
                                : tableList.getDescription();
                        if(b==null){
                            showErro("Selecione o bucket");
                            return;
                        }
                        
                        upload(b, f.getName(), new FileInputStream(f));
                        
                        refresh();
                    } catch (FileNotFoundException ex) {
                        f.delete();
                        ShowMensagens.showErro("Erro no arquivo "+f.getName());
                    }
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
        drop.setPathOut(new File(pathUplink));

        return drop;
    }
    
    private Table buildTable() {
        Table table =  new Table();

        table.setNullSelectionAllowed(false);
        table.setSelectable(true);
        table.setImmediate(true);
        table.setMultiSelect(true);
        table.setWidth("100%");
        table.setHeight("100%");
        table.setFooterVisible(true);

        table.addContainerProperty("Name", String.class, null);
        table.setColumnExpandRatio("Name", 3f);
        table.addContainerProperty("Date", Date.class, null);
        table.addContainerProperty("Size", Long.class, null);
        table.addContainerProperty("Check", CheckBox.class, null);
        table.addContainerProperty("Type", String.class, null);
        table.addContainerProperty("Parent", String.class, null);
        table.addContainerProperty("Opc", Component.class, null);
        
        table.setColumnHeaders("Name", "Date","Size","Check","Type","Parent",".....");
    
        table.addShortcutListener(new ShortcutListener(
                null, KeyCode.DELETE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                if(!target.equals(table)){
                    return ;
                }
    
                delete.click();
            }
        });
        table.addShortcutListener(new ShortcutListener(
                null, KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                if(!target.equals(table)){
                    return ;
                }
                Item[] i = getSelecteds();
                if(i==null || i[0].getItemProperty("Type").getValue()!="bucket")
                    return;
                
                String b = (String) i[0].getItemProperty("Name").getValue();
                loadTable(getObjects(b),b);
            }
        });
        table.addListener(new ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (!event.isDoubleClick()) {
                    return;
                }
                if(event.getItem().getItemProperty("Type").getValue()!="bucket")
                    return;
                
                String b = (String) event.getItem().getItemProperty("Name").getValue();
                loadTable(getObjects(b),b);
            }

          
        });

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
                    CheckBox c = (CheckBox) i.getItemProperty("Check").getValue();
                    c.setValue(true);
                    
//                    filter.select(itm);
                }
                //VERIF ITM NAO SELECIONADOS,CHK FALSE
                Object[] listAll = tableList.getValues();
                List listSelects2 = Arrays.asList(listSelects);
                
                for (Object itm : listAll) {
                    if(listSelects2.contains(itm)){//SELECIONADO
                        continue;
                    }
                    Item i = tableList.getItem(itm);
                    CheckBox c = (CheckBox) i.getItemProperty("Check").getValue();
                    c.setValue(false);
                }
            }

        });                

        return table;
    }
    
    private void delete(Item... item) {
        for (Item i : item) {
            if(i.getItemProperty("Type").getValue()=="bucket")
                deleteBucket(i.getItemProperty("Name").getValue().toString());
            else
                deleteObject(i.getItemProperty("Parent").toString()
                        ,i.getItemProperty("Name").toString());
        }
        filter.focus();
    }
    private void deleteBucket(String bucket) {
        try (Project project = uplink.openProject(access)) {
            ObjectIterator objectIterator = project.listObjects(bucket);

            for (ObjectInfo objectInfo : objectIterator) {
                project.deleteObject(bucket,objectInfo.getKey());
                System.out.println(objectInfo.getKey());
            }
            project.deleteBucket(bucket);
            tableList.removeItem(bucket);
            filter.setItemCaption(bucket,"REMOVED "+bucket);
            
            project.close();
        }
    }
    private void deleteObject(String bucket,String obj) {
        try (Project project = uplink.openProject(access)) {
            project.deleteObject(bucket,obj);
            
            tableList.removeItem(obj);
            filter.setItemCaption(bucket,"REMOVED "+obj);
            
            project.close();
        }
    }
    private ObjectInfo[] getObjects(String bucket) {
        Project project = uplink.openProject(access);
        
        List<ObjectInfo> list = sortedIterator(
                    project.listObjects(bucket,
                            ObjectListOption.system(),
                            ObjectListOption.custom())
                , (Comparator<ObjectInfo>) (ObjectInfo f1, ObjectInfo f2) 
                        -> f1.getKey().compareTo(f2.getKey()));

        project.close();
        
        return list.toArray(new ObjectInfo[list.size()]);
    }
    private String[] getBuckets(String name) {
        Project project = uplink.openProject(access);
        
        BucketIterator list = project.listBuckets(BucketListOption.cursor(""));

        List<String> list2 = new ArrayList<String>();

        for (BucketInfo bucketInfo : list) {
//            if(name==null || name.isEmpty() || bucketInfo.getName().contains(name))
                list2.add(bucketInfo.getName());  
        }
        project.close();
        
        return list2.toArray(new String[list2.size()]);
    }
    private void upload(String bucket,String fileName,InputStream data){
        Project project = uplink.openProject(access);
        
        try (ObjectOutputStream upload = project.uploadObject(bucket, fileName)) {
//            upload.write(data, 0, data.length);
            ByteStreams.copy(data, upload);
            upload.commit();
        } catch (IOException ex) {            
        }
        project.close();
    }
    private InputStream download(String bucket,String file,Project project) {
//        Project project = uplink.openProject(access);
        
        OutputStream out = null;
//            File f = new File(file);
        try {
//            if(f.exists() && f.length() > 0)
//            {
//                FileInputStream input = new FileInputStream(file);
//                return input;
//            }
//            f.getParentFile().mkdirs();
//            f.delete();
//            f.createNewFile();
//            out = new FileOutputStream(file);
            
            ObjectInputStream download = project.downloadObject(bucket, new File(file).getName());
                    
//            ByteStreams.copy(download, out);
            
            return download;
        } catch (Exception ex) {            
//            f.delete();
        }finally{
            try {
//                project.close();
//                out.close();
            } catch (Exception ex1) {
            }
        }
        return null;
    }
    private void loadTable(String bucket) {
       loadTable(getBuckets(bucket));
    }
    
    private void loadTable(String[] list) {
        tableList.removeAllItems();
        tableList.setDescription(null);
        filter.removeAllItems();
        
        if (list == null) {
            return ;
        }

        for (String itm : list) {
            Item item = tableList.addItem(itm);
            if(item==null)
                continue;   
            filter.addItem(itm);
//            filter.getItem(itm).getItemProperty("filter").setValue(itm);
            filter.setItemCaption(itm, itm);
            
            item.getItemProperty("Name").setValue(itm);
            item.getItemProperty("Check").setValue(buildSelections(itm));
            item.getItemProperty("Type").setValue("bucket");
            
        }
        tableList.setColumnFooter("Name", list.length+"");
        filter.focus();
    }
    
    private void loadTable(ObjectInfo[] list,String bucket) {
        tableList.removeAllItems();
        tableList.setDescription(bucket);
        filter.removeAllItems();
//        filter.select(bucket);
        
        if (list == null) {
            return ;
        }

        for (ObjectInfo itm : list) {
            if(tableList.getItem(itm.getKey())!=null)
                continue;
            Item item = tableList.addItem(itm.getKey());
            
            if(filter.getItem(itm.getKey())==null){
                filter.addItem(itm.getKey());
    //            filter.getItem(itm.getKey()).getItemProperty("filter").setValue(itm.getKey());
                filter.setItemCaption(itm.getKey(), itm.getKey());
            }
            item.getItemProperty("Name").setValue(itm.getKey());
            item.getItemProperty("Date").setValue(itm.getSystemMetadata().getCreated());
            item.getItemProperty("Size").setValue(itm.getSystemMetadata().getContentLength()*(1024*1024));
            item.getItemProperty("Check").setValue(buildSelections(itm));
            item.getItemProperty("Type").setValue("object");
            item.getItemProperty("Parent").setValue(bucket);
            if(bucket!=null)
                item.getItemProperty("Opc").setValue(buildDownload(bucket,itm.getKey()));
            
        }
        tableList.setColumnFooter("Name", list.length+"");
        filter.focus();
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
    
    private Item[] getSelecteds(){
        if (!tableList.isSelected()) {
            return null;
        }
        Object[] list = tableList.getSelecteds();
        
        List<Item> list2 = new ArrayList<>();
        
        for (Object itm : list) {
            Item i = tableList.getItem(itm);
            CheckBox c = (CheckBox) i.getItemProperty("Check").getValue();
            
            if (c.getValue()) {
                list2.add(i);
            }
        }
        if(list2.isEmpty()){
            return null;
        }
        return list2.toArray(new Item[list2.size()]);
    }

    private static String getAccessGrant() {
        try {
            String accessGrant = System.getenv("GATEWAY_0_ACCESS");
//            String accessGrant = UteisFile.readByte(new File("d:/uplink/accessgrant.txt"));
            if (accessGrant == null || accessGrant.length() == 0) {
                accessGrant = System.getenv("UPLINK_ACCESS");
            }
            return accessGrant;
        } catch (Exception ex) {
            
        }
        return null;
    }
    
    public static List sortedIterator(Iterator it, Comparator comparator) {
          List list = new ArrayList();
          while (it.hasNext()) {
              list.add(it.next());
          }

          Collections.sort(list, comparator);
          return list;
    }
    private void refresh() {
        if(tableList.getDescription()==null) {
            loadTable(filter.getValue()==null?"":filter.getValue().toString());
        } else {
            String b = tableList.getDescription();
            loadTable(getObjects(b), b);
        }

    }
    
    private abstract class RunnableDone {
        public abstract void setInput(InputStream input);
    }
}
