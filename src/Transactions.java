import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import static javax.print.attribute.ResolutionSyntax.DPI;
import javax.swing.JFileChooser;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;
import org.opencv.imgproc.Imgproc;



public class Transactions {
        
    private Connection con =null;
    
    private Statement statement =null;
     
    private PreparedStatement preparedstatement=null;
    
    private Statement statement2=null;
    
    HashMap<String,String> content = new HashMap<String,String>();
    
    ArrayList<PlugData> plug = new ArrayList<PlugData>();
    
    Scanner scanner = new Scanner(System.in);
    
    private static String path;
    
    private String result;
    
    double INCH_2_CM=2.54;
    
    public static final String DENSITY_UNITS_NO_UNITS = "00";
    public static final String DENSITY_UNITS_PIXELS_PER_INCH = "01";
    public static final String DENSITY_UNITS_PIXELS_PER_CM = "02";
    
    static BufferedImage  gridImage=null;

    public static String getPath() {
        return path;
    }

    public static void setPath(String path) {
        Transactions.path = path;
    }
     
    public Transactions() {
 
        //?useUnicode=true&characterEncoding=utf8" türkçe karater ile ilgili problem yaşamamızı engelliyor
        //"jdbc:mysql://localhost:3306/calisan";
        String url="jdbc:mysql://"+Database.host+":"+Database.port+"/"+Database.d_name+"?useUnicode=true&characterEncoding=utf8";
            
        try {
            //driver ımızı özellikle başlatıyoruz bunu yapmazsak bazen sıkıntı çıkabiliyor
            //Bunu yaptığımızda jdbc driver ını ekstradan çağırmış oluyoruz
            Class.forName("com.mysql.jdbc.Driver");
            
        } catch (ClassNotFoundException ex) {
            System.out.println("jdbc driver not found");
        }
        try {
            con=DriverManager.getConnection(url,Database.user_name,Database.pass);
           //System.out.println("Veritabi baglantisi basarili");
            
        } catch (SQLException ex) {
            System.out.println("Database conneciton failed");
        }
    }  
    public String allTras(){
        
        path = imagePath();
        
        //System.out.println(path);
             
       /*File input = new File(path);
        
        try {
            gridImage = ImageIO.read(new File(path));
       } 
       catch (IOException e) {
       }
       
       try {
            saveGridImage(input);
        } catch (IOException ex) {
            System.out.println("dpi porblem");
            //Logger.getLogger(Transactions.class.getName()).log(Level.SEVERE, null, ex);
        }*/
       
        grayScale(path);
        gauss(path);
        System.out.println(path);
        result=imageRead(path);
        System.out.println(result);
        try{
                    ArrayList<PlugData> plug= parseText(result);
        //System.out.println("İşletmenin Adın giriniz:");
        
        
        addplug(plug);
        }
        catch(Exception e) {
            System.out.println("Fiş okunmadı");
        }
        //result = result.toUpperCase();
        finally{
            return result;
        }
        
        
    }
    //Kullanıcının okuycağı fişi seçmesini sağlıyor
    public String imagePath(){
        String path;
        JFileChooser f = new JFileChooser();
        int retunValue=f.showSaveDialog(f);
        if(retunValue == JFileChooser.APPROVE_OPTION){
            path =f.getSelectedFile().getAbsolutePath();
            return path;
        }
        return "User clicked CANCEL"; 
    }
    //OpenCV kullnarak resmin gri versiyonunu çıkartıyor
    public void grayScale(String path){
         try {
         System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
         File input = new File(path);
         BufferedImage image = ImageIO.read(input);	

         byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
         Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
         mat.put(0, 0, data);

         Mat mat1 = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
         Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);

         byte[] data1 = new byte[mat1.rows() * mat1.cols() * (int)(mat1.elemSize())];
         mat1.get(0, 0, data1);
         
         BufferedImage image1 = new BufferedImage(mat1.cols(),mat1.rows(), BufferedImage.TYPE_BYTE_GRAY);
         image1.getRaster().setDataElements(0, 0, mat1.cols(), mat1.rows(), data1);

         File ouptut = new File("grayscale.jpg");
         ImageIO.write(image1, "jpg", ouptut);
         
             setPath(ouptut.getPath());
      } catch (Exception e) {
         System.out.println("Error: " + e.getMessage());
      }
    }   
    //Dpı ayarlanmış resmi kayıt ediyor
    private void saveGridImage(File output) throws IOException {
    output.delete();

    final String formatName = "jpeg";

    for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
        ImageWriter writer = iw.next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
        if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
            continue;
        }

        setDPI(metadata);

        final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
        try {
            writer.setOutput(stream);
            writer.write(metadata, new IIOImage(gridImage, null, metadata), writeParam);
        } finally {
            stream.close();
        }
        break;
    }

 }
   //Dpı ayarlanıyor
    private void setDPI(IIOMetadata metadata) throws IIOInvalidTreeException {

  String metadataFormat = "javax_imageio_jpeg_image_1.0";
    IIOMetadataNode root = new IIOMetadataNode(metadataFormat);
    IIOMetadataNode jpegVariety = new IIOMetadataNode("JPEGvariety");
    IIOMetadataNode markerSequence = new IIOMetadataNode("markerSequence");

    IIOMetadataNode app0JFIF = new IIOMetadataNode("app0JFIF");
    app0JFIF.setAttribute("majorVersion", "1");
    app0JFIF.setAttribute("minorVersion", "2");
    app0JFIF.setAttribute("thumbWidth", "0");
    app0JFIF.setAttribute("thumbHeight", "0");
    app0JFIF.setAttribute("resUnits", DENSITY_UNITS_PIXELS_PER_INCH);
    app0JFIF.setAttribute("Xdensity", String.valueOf(300));
    app0JFIF.setAttribute("Ydensity", String.valueOf(300));

    root.appendChild(jpegVariety);
    root.appendChild(markerSequence);
    jpegVariety.appendChild(app0JFIF);

    metadata.mergeTree(metadataFormat, root);
 } 
    //Tessract kullarak işlenmiş resmi text haline getiriyor
    public void gauss(String path){
         try{ 
         // For proper execution of native libraries 
         // Core.NATIVE_LIBRARY_NAME must be loaded before 
            // calling any of the opencv methods 
         System.loadLibrary( Core.NATIVE_LIBRARY_NAME ); 
  
         // Input image 
         Mat source = 
         Imgcodecs.imread(path, IMREAD_COLOR); 
         Mat destination = new Mat(source.rows(), source.cols(), source.type()); 
  
         // filtering 
         Imgproc.GaussianBlur(source, destination, new Size(0, 0), 10); 
         Core.addWeighted(source, 1.5, destination, -0.5, 0, destination); 
  
         // writing output image 
         Imgcodecs.imwrite(path, destination); 
      }catch (Exception e) { 
      } 
    }
    
    public String imageRead(String path){
        Tesseract tesseract = new Tesseract();
        String text="";
        try { 
            
            tesseract.setDatapath("C:\\Tesseract-OCR\\tessdata");
            tesseract.setLanguage("tur");
  
            // the path of your tess data folder 
            // inside the extracted file 
            text = tesseract.doOCR(new File(path)); 
  
            // path of your image file 
        } 
        catch (TesseractException e) { 
            //e.printStackTrace(); 
            System.out.println("İmage parsing işlemi sırasında hata alındı");
        }
        return text;
    }
    //Değişkeni aldıktan sonra satır sonu kontrolü yapıyor
    public boolean cursorLine(String text,int coIndex){
        
        int control1=text.indexOf("\n",coIndex+2);
        int control2=text.indexOf(" ",coIndex+2);
        
        if(control1 > control2&&  control1-control2>6){
            //Satırın devamında bir değişken daha var
            return true;
        }
        else{
            //Satırda tek değişken var
            return false;
        }
        
        
    }
    //Fişi database eklenecek hale getiriyor
    public ArrayList<PlugData> parseText(String text){
        
        //coIndex colone index
        // lineSIndex start line index
        // lineMIndex mid line index
        // lineEIndex end line index
       
        int coIndex,lineSIndex,lineMIndex,lineEIndex;
        int nameIndex;
        String key,value,product;
        
        nameIndex=text.indexOf("\n");
        key="İsletme Adı";
        value=text.substring(0,nameIndex);
        
        content.put(key, value);
        
        coIndex=text.indexOf(":");
        lineSIndex=text.lastIndexOf("\n",coIndex);
        boolean result =cursorLine(text, coIndex+2);
        if(result)
            lineEIndex=text.indexOf(" ",coIndex+2);
        else
            lineEIndex=text.indexOf("\n",coIndex+2);
        System.out.println(coIndex+"---"+lineSIndex);
        
        while(coIndex!=-1){
         //System.out.println("key: "+key);
            key=text.substring((lineSIndex+1),(coIndex)).trim() ;
            
            value=text.substring((coIndex+1),lineEIndex).trim();
                       
            content.put(key, value);
            result =cursorLine(text, coIndex+2);
            if(result){
                //System.out.println("girdi");
                lineSIndex=text.indexOf(" ",coIndex+2);
                coIndex=text.indexOf(":",coIndex+1);
                lineEIndex=text.indexOf("\n",lineSIndex); 
            }
            else{
                coIndex=text.indexOf(":",coIndex+1);
                lineSIndex=text.lastIndexOf("\n",coIndex);
                lineEIndex=text.indexOf("\n",coIndex);                
            }

            
        }
        
        coIndex=text.indexOf("*");
        lineSIndex=text.lastIndexOf("\n",coIndex);
        coIndex=text.lastIndexOf("*");
        lineEIndex=text.indexOf("\n",coIndex);
        
        product = text.substring(lineSIndex+1,lineEIndex-1);
        /*System.out.println(content.get("İsletme Adı"));
        System.out.println(content.get("Tarih"));
        System.out.println(content.get("Fiş No"));
        System.out.println(content.get("Toplam fiyat"));
        System.out.println(product);*/
        //System.out.println("Liste\n"+content.get("İsletme Adı")+" "+content.get("TARİH")+" "+content.get("FİŞ NO"+" "+content.get("TOPLAM FİYAT")+" "+product));    
        plug.add(new PlugData(content.get("İsletme Adı"),content.get("Tarih"),Integer.parseInt(content.get("Fiş No")),product,Integer.parseInt(content.get("Toplam fiyat"))));
        
        return plug;
    }
    //Şirket kontolü yapıcak yoksa eklenicek
    public void isThereCompany(String name){
        
        String query="SELECT *FROM company WHERE name='"+name+"'";
        
        try {
           statement=con.createStatement();
           
           ResultSet rs=statement.executeQuery(query);
           
            if(!rs.next()){
               
                addCompany(name);
            } 
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Transactions.class.getName()).log(Level.SEVERE, null, ex);       
        }    
       
    }
    //Kontrol yapıldığında şirket bulunmuyorsa eklenicek
    public void addCompany(String name){
        
        String query="INSERT INTO company (Name) VALUES (?)";
        
        try {
            preparedstatement=con.prepareStatement(query);
            
            preparedstatement.setString(1,name);
            
            preparedstatement.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(Transactions.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    //İşlemler tamamlandığında arraylist alıcak
    //Eklenecek veriler arraylistten gelecek
    public void addplug(ArrayList<PlugData> plug){
        int id=0,no=0,total=0;
        String date="",pt="",name="";
        
        //System.out.println(plug.get(0).toString()+"  "+plug.get(1).toString()+plug.get(2).toString()+plug.get(3).toString()+plug.get(4).toString());
        for (PlugData p: plug){
               name=p.getMarketName();
               no=p.getPlugNo();
               date=p.getDate();
               pt=p.getProduct();
               total=p.getTotalPrice();
                
            }
        isThereCompany(name);
        String query="SELECT *FROM company WHERE name='"+name+"'";
        
        try {
            statement=con.createStatement();
                        
            ResultSet rs=statement.executeQuery(query);
        
            while(rs.next()){
                
                id=rs.getInt("ID");
                
            }
                                 
            query="INSERT INTO plug (ID,date,plugNo,product,total) VALUES (?,?,?,?,?)";
            
            preparedstatement=con.prepareStatement(query);
            
            preparedstatement.setInt(1, id);
            
            preparedstatement.setString(2,date);
            
            preparedstatement.setInt(3,no);
            
            preparedstatement.setString(4,pt);
            
            preparedstatement.setInt(5, total);
                    
            preparedstatement.executeUpdate();
            
      
        } catch (SQLException ex) {
            Logger.getLogger(Transactions.class.getName()).log(Level.SEVERE, null, ex);
        }
             
    }
    //Databasedeki verileri sıralamak için kullandığım fonksiyon
    public ArrayList<PlugData> sorting(String var){
  
        String query="SELECT * FROM plug p,company c Where p.ID=c.ID ORDER BY total "+var;
        
        ArrayList<PlugData> result = list(query);
        
        return result;
    }
    //Databasede arama yapmak için kulladığım fonsksiyon
    public ArrayList<PlugData> search(String nm ,String dt){
        String query="";
           
        if(nm.isEmpty()&&dt.isEmpty()){
            query="SELECT *FROM company c,plug p Where p.ID=c.ID AND date='"+dt+"'"+"AND name='"+nm+"'";
        }
        else if(!nm.isEmpty()&&dt.isEmpty()){
            query="SELECT *FROM company c,plug p Where p.ID=c.ID AND name='"+nm+"'";
            
        }
        else{
            query="SELECT *FROM company c,plug p Where p.ID=c.ID AND date='"+dt+"'";
        }
        
        ArrayList<PlugData> result = list(query);
              
        return result;
    }
    //Veritabanından verileri çekikten sonra menude tabloya basıcaz
    public ArrayList<PlugData> getAllDB(){
         
        String query="SELECT *FROM company c,plug p Where p.ID=c.ID";
        
        ArrayList<PlugData> result  = list(query);
     
        return result;
    }
    //Sorgu sonucu dönemem satırları listeye ekleliyor
    public ArrayList<PlugData> list(String query){
        
         ArrayList<PlugData> result = new ArrayList<PlugData>();
         
        try {
            statement=con.createStatement();
            
            ResultSet rs = statement.executeQuery(query);
            
            while(rs.next()){
                
                String name = rs.getString("Name");
                String date= rs.getString("date");
                int pNo=rs.getInt("plugNo");
                String product = rs.getString("product");
                int total = rs.getInt("total");
                            
                result.add(new PlugData(name, date, pNo, product, total));          
            }
        } catch (SQLException ex) {
            Logger.getLogger(Transactions.class.getName()).log(Level.SEVERE, null, ex);
        }                    
        return result;
    }
}

