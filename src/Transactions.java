
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Transactions {
        
    private Connection con =null;
    
    HashMap<String,String> content = new HashMap<String,String>();
    
    ArrayList<PlugData> plug = new ArrayList<PlugData>();
    
    private static String path;
    
    private String result;
    
    private int width=300;
    
    private int height=300;
    
    

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
        
        grayScale(path);
        System.out.println(path);
        
        setDpi();
        
        result=imageRead(path);
        
        return result;
        
        
    }
    
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
    
    public void setDpi(){
        BufferedImage image=null;
     try {
         File input = new File(path);
         image = ImageIO.read(input);
         width = image.getWidth();
         height = image.getHeight();
         
         int count = 0;
         
         for(int i=0; i<height; i++) {
         
            for(int j=0; j<width; j++) {
            
               count++;
               Color c = new Color(image.getRGB(j, i));
               //System.out.println("S.No: " + count + " Red: " + c.getRed() +"  Green: " + c.getGreen() + " Blue: " + c.getBlue());
            }
         }

      } catch (Exception e) {}
   }
    
    public String imageRead(String path){
        Tesseract tesseract = new Tesseract();
        String text="";
        try { 
            
            tesseract.setDatapath("C:/Tess4J/tessdata");
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
    
    public void parseText(String text){
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
        lineEIndex=text.indexOf("\n",coIndex);
        
        System.out.println(coIndex+"---"+lineSIndex);
        
        while(coIndex!=-1){
         
            key=text.substring((lineSIndex+1),(coIndex)).trim() ;
            
            value=text.substring((coIndex+1),lineEIndex).trim();
                       
            content.put(key, value);
            
            coIndex=text.indexOf(":",coIndex+1);
            lineSIndex=text.lastIndexOf("\n",coIndex);
            lineEIndex=text.indexOf("\n",coIndex);
            
        }
        
        coIndex=text.indexOf("*");
        lineSIndex=text.lastIndexOf("\n",coIndex);
        coIndex=text.lastIndexOf("*");
        lineEIndex=text.indexOf("\n",coIndex);
        
        product = text.substring(lineSIndex+1,lineEIndex-1);
        
        
        //plug.add(new PlugData(content.get("İsletme Adı"),content.get("Tarih"),content.get("Fiş No"),product,Integer.parseInt(content.get("Toplam fiyat"))));
        

        
        
         
      
                                 
    }
}

