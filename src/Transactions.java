
import java.io.File;
import java.util.HashMap;
import javax.swing.JFileChooser;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Transactions {
    
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

