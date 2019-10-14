
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    private Statement statement =null;
     
    private PreparedStatement preparedstatement=null;
    
    private Statement statement2=null;
    
    HashMap<String,String> content = new HashMap<String,String>();
    
    ArrayList<PlugData> plug = new ArrayList<PlugData>();
    
    Scanner scanner = new Scanner(System.in);
    
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
        
        
        
        System.out.println("İşletmenin Adın giriniz:");
        String name =scanner.nextLine();
        
        addplug(name);
        
        
        return result;
        
        
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
    //Resmin Dpi ayarını yapıyor
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
    //Tessract kullarak işlenmiş resmi text haline getiriyor
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
    //Fişi database eklenecek hale getiriyor
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
    public void addplug(String name){
        int id=0;
        isThereCompany(name);
        
        String query="SELECT *FROM company WHERE name='"+name+"'";
        
        try {
            statement=con.createStatement();
            
            
            ResultSet rs=statement.executeQuery(query);
        
            while(rs.next()){
                
                id=rs.getInt("ID");
                
            }
            
            System.out.println("Tarih");
            
            String date = scanner.nextLine();
            
            System.out.println("Fiş No");
            
            int no= scanner.nextInt();
            scanner.nextLine();
            System.out.println("Ürün");
            
            String pt = scanner.nextLine();
            
            System.out.println("Total");
            
            int total= scanner.nextInt();
            
            
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
        
    public ArrayList<PlugData> sorting(String var){
  
        String query="SELECT * FROM plug p,company c Where p.ID=c.ID ORDER BY total "+var;
        
        ArrayList<PlugData> result = list(query);
        
        return result;
    }
    
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

