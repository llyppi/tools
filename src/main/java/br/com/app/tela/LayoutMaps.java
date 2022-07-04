package br.com.app.tela;


import br.com.component.ShowMensagens;
import br.com.utilitarios.UteisFile;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import elemental.json.JsonArray;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

/**
 *
 * @author Felipe L. Garcia
 */

@JavaScript({"https://maps.googleapis.com/maps/api/js?key=&callback=initMap"
,"vaadin://js/map.js"})


public class LayoutMaps extends VerticalLayout{
    private double lat=-16.68366784117075,lng=-49.264580863913366;
    
    public LayoutMaps() {
       
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
        AppContent.get().setCaption("Maps");
        
        super.setSizeFull();        
        super.setSpacing(true);
        super.setMargin(true);
        
        Component c = buildMap();
        super.addComponent(c);
        super.setExpandRatio(c,3f);        
  
        Link link = new Link();
        link.setId("link");        
        link.setCaption("javascript:br.com.app.tela.LayoutMaps.setPosition");
        link.setResource(new ExternalResource(link.getCaption()));
        
        super.addComponent(link);               
    }

    private Component buildMap() {
        BrowserFrame map = new BrowserFrame();
        map.setId("mapa");
        map.setSizeFull();
                
        return map;
    }       

    public void addMap(double lat,double log) {
        addMap(new Location(lat, log,null));
    }
    public void addMap(Location... locations) {
        
        UI ui = UI.getCurrent();
        ui.getPushConfiguration().setPushMode(PushMode.AUTOMATIC);      
        ui.setPollInterval(500);
        ui.access(new Runnable() {
            
            @Override
            public void run() {
                if(locations==null || locations.length <= 0){
                    ui.setPollInterval(-1);
                    return;
                }
                StringBuilder script = new StringBuilder();
                script.append("var loc = [];");
                script.append("var i = 0;");
                
                lat = locations[0].lat;
                lng = locations[0].log;
                
                script.append("var map = initMap(").append(lat)
                                    .append(",").append(lng).append(");");
//                                    .append("var link = document.getElementById('link');")
//                                    .append("link.style.display = 'none';");
                
//                ui.getPage().getJavaScript().execute(script.toString());                
                
                ui.getPage().getJavaScript().addFunction("br.com.app.tela.LayoutMaps.setPosition"
                        , new JavaScriptFunction() {
                    @Override
                    public void call(JsonArray arguments) {
                        for (Location l : locations) {
                            String name = arguments.get(0).toJson().replace("\"", "");
                            
                            if(l.file.getName().equalsIgnoreCase(name))
                            {
                                setGPS( l.file
                                        , Double.parseDouble(arguments.get(1).toJson())
                                        , Double.parseDouble(arguments.get(2).toJson()));
                            }
                        }
                    }
                });
                DecimalFormat df = new DecimalFormat();
                df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
                df.setMaximumFractionDigits(14);
                
                for (Location loc : locations) {
                    double lat = Double.parseDouble(df.format(loc.lat));
                    double lng = Double.parseDouble(df.format(loc.log));
                    
                    //script = new StringBuilder();
//                    script.append("loc[i++] = {")
//                    .append("lat:").append(lat)
//                    .append(",lng:").append(lng)
//                    .append("};");

                    script.append("addMarker({")
                    .append("lat:").append(lat==0 ? LayoutMaps.this.lat:lat)
                    .append(",lng:").append(lng==0 ? LayoutMaps.this.lng:lng)
                    .append("}")
                    .append(",map,'")
                    .append(loc.file.getName())
                    .append("');");
                    
                }
                ui.getPage().getJavaScript().execute(script.toString());                
                ui.setPollInterval(-1);
                
                ShowMensagens.showTray("loading...");
            }
        });
    }
    
    public static byte[] setGPS(final File image,double lat,double lng){
        try {
            TiffOutputSet outputSet = null;
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            
            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = Imaging.getMetadata(image);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();
                
                if (null != exif) {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by
                    // changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                }
            }
            
            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }
            
            final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
            // make sure to remove old value if present (this method will
            // not fail if the tag does not exist).
            exifDirectory.removeField(ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
            exifDirectory.add(ExifTagConstants.EXIF_TAG_APERTURE_VALUE,
                    new RationalNumber(3, 10));
            
            outputSet.setGPSInDegrees(lng, lat);
            
            new ExifRewriter().updateExifMetadataLossless(image, os,outputSet);
            
            if(os.toByteArray()!=null){
                image.delete();
                UteisFile.write(image, os.toByteArray());
                
                ShowMensagens.showTray("Nova Localização "+image.getName());
            }
            
        } catch (Exception ex) {
           ShowMensagens.showErro("Erro na Localização");
        }
        return null;
    }
    
    public static class Location
    {
        private double lat,log;
        private File file;

        public Location(double lat, double log,File file) {
            this.lat = lat;
            this.log = log;
            this.file = file;
        }
       
    }
}
