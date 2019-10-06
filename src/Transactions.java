
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import javax.swing.JFileChooser;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Transactions {
        
    private Connection con =null;
    
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
    
    
    
    HashMap<String,String> content = new HashMap<String,String>();
    public static String path;
    
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
        System.out.println("Parsing: \n"+text);
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
        int lineIndex=text.indexOf("\n");
        int LastIndex=text.lastIndexOf("Toplam");
        int coloneIndex=text.indexOf(":");
        int startLine=0,temp;
        int lastcolon=text.lastIndexOf(":");
        String key,value,controlLine;

        while (text.length()!=lineIndex){ 
            key = text.substring(startLine,(coloneIndex));
            
            value = text.substring((coloneIndex+2),lineIndex);
            
            
            content.put(key, value);
            
            startLine=lineIndex+1;
            coloneIndex=text.indexOf(":",lineIndex+1);
            if(lastcolon==coloneIndex){
                
                key = text.substring(LastIndex,(coloneIndex));
            
                value = text.substring((coloneIndex+2),text.length());
                  
                return;
            }
            lineIndex=lineCursor(lineIndex,text);          
        }  
    }
}

