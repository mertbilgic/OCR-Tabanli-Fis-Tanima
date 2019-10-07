
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFileChooser;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Transactions {
        
    private Connection con =null;
    
    HashMap<String,String> content = new HashMap<String,String>();
    
    ArrayList<PlugData> plug = new ArrayList<PlugData>();
    
    public static String path;
    
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
    
    public String imagePath(){
        JFileChooser f = new JFileChooser();
        int retunValue=f.showSaveDialog(f);
        if(retunValue == JFileChooser.APPROVE_OPTION){
            path =f.getSelectedFile().getAbsolutePath();
            return path;
        }
        return "User clicked CANCEL"; 
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
    
    public int lineCursor(int lineIndex,String text){
        int newlineIndex=text.indexOf("\n",lineIndex+1);
        
        while(newlineIndex-lineIndex==1){
            lineIndex=newlineIndex;
            newlineIndex=text.indexOf("\n",lineIndex+1);          
        }
        
        return newlineIndex;
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
        key="İsletmeAdı";
        value=text.substring(0,nameIndex);
        
        content.put(key, value);
        
        coIndex=text.indexOf(":");
        lineSIndex=text.lastIndexOf("\n",coIndex);
        lineEIndex=text.indexOf("\n",coIndex);
        
        System.out.println(coIndex+"---"+lineSIndex);
        
        while(coIndex!=-1){
         
            key=text.substring((lineSIndex+1),(coIndex)).trim() ;
            
            value=text.substring((coIndex+1),lineEIndex).trim();
            
            //System.out.println("Key: "+key+" Value:"+value);
            
            content.put(key, value);
            
            coIndex=text.indexOf(":",coIndex+1);
            lineSIndex=text.lastIndexOf("\n",coIndex);
            lineEIndex=text.indexOf("\n",coIndex);
            
        }
        content.forEach((k,v) -> System.out.print("key: "+k+" value:"+v));
        
        coIndex=text.indexOf("*");
        lineSIndex=text.lastIndexOf("\n",coIndex);
        coIndex=text.lastIndexOf("*");
        lineEIndex=text.indexOf("\n",coIndex);
        
        product = text.substring(lineSIndex+1,lineEIndex-1);
        
        System.out.println("\np:  "+product);
        
        plug.add(new PlugData(content.get("İsletmeAdı"),content.get("Tarih"),content.get("Fiş No"),product,Integer.parseInt(content.get("Toplam fiyat"))));
              
                                 
    }
}

