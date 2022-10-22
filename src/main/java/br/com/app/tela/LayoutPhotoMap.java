package br.com.app.tela;


import br.com.app.tela.LayoutMaps.Location;
import br.com.utilitarios.AppVariables;
import br.com.utilitarios.UteisFile;
import br.com.utilitarios.UteisImage;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;

/**
 *
 * @author Felipe L. Garcia
 */

public class LayoutPhotoMap extends VerticalLayout{
    private LayoutMaps map;
    
    public LayoutPhotoMap() {
       
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
        AppContent.get().setCaption("Photo Maps");
        
        super.setSizeFull();        
        super.setSpacing(true);
        super.setMargin(true);
        
        String path = AppVariables.pathWeb+ File.separator +"photos";
        
        if(!new File(path).exists())
        {
            return;
        }
        map = new LayoutMaps();
        super.addComponent(map);
        super.setExpandRatio(map,3f);
        
        Button btn = new Button();
        btn.setStyleName(Runo.BUTTON_LINK);
        btn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                File[] list = UteisFile.getFiles(path,"",false);
                load(list);
            }
        });
        btn.setIcon(new ThemeResource("images/refresh16x16.png"));
        super.addComponent(btn);
        
        File[] list = UteisFile.getFiles(path,"",false);
        load(list);
    }
    
    private void load(File... list){
        List<Location> locations = new ArrayList<>();
        
        for (File file : list) {
            try {
                File min = new File(AppVariables.pathWeb,"VAADIN/photos/"+file.getName());
                //Image create mini
                if(!min.exists()){
                    try {
                        float[] sz = UteisImage.getImageSize(new FileInputStream(file));
                        float w = sz[0];
                        float h = sz[1];
                        min.getParentFile().mkdirs();
                        if (w > h) {
                            UteisImage.imageResize(file, min, 80, 40);
                        } else {
                            UteisImage.imageResize(file, min, 40, 80);
                        }
                    } catch (Exception ex) {
                    }
                }
            
                String gps = getGPS(file);
                if(gps.isEmpty())
                {
                    locations.add(new Location(0,0,file));
                    continue;
                }
                String lat =  gps.split("/")[0];
                String log = gps.split("/")[1];
                
                locations.add(new Location(Double.parseDouble(lat)
                                , Double.parseDouble(log)
                                ,file));
                
            } catch (Exception ex) {                
            } 
        }
        map.addMap(locations.toArray(new Location[locations.size()]));
    }
    
    private static String getGPS(final File image) {

        try{

            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = Imaging.getMetadata(image);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                  
                    return exif.getGPS().getLatitudeAsDegreesNorth()
                            +"/"+exif.getGPS().getLongitudeAsDegreesEast();
                }
            }

        } catch (Exception ex) {
            
        }
        return "";
    }
    
}
