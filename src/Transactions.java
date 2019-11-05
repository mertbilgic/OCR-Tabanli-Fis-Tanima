
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RescaleOp;
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
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;
import org.opencv.imgproc.Imgproc;

public class Transactions {

    private Connection con = null;

    private Statement statement = null;

    private PreparedStatement preparedstatement = null;

    private Statement statement2 = null;

    HashMap<String, String> content = new HashMap<String, String>();

    ArrayList<PlugData> plug = new ArrayList<PlugData>();

    Scanner scanner = new Scanner(System.in);

    private static String path;

    private String result;

    private String parseResult = "";

    static BufferedImage gridImage = null;

    public static String getPath() {
        return path;
    }

    public static void setPath(String path) {
        Transactions.path = path;
    }

    public Transactions() {

        //?useUnicode=true&characterEncoding=utf8" türkçe karater ile ilgili problem yaşamamızı engelliyor
        //"jdbc:mysql://localhost:3306/calisan";
        String url = "jdbc:mysql://" + Database.host + ":" + Database.port + "/" + Database.d_name + "?useUnicode=true&characterEncoding=utf8";

        try {
            //driver ımızı özellikle başlatıyoruz bunu yapmazsak bazen sıkıntı çıkabiliyor
            //Bunu yaptığımızda jdbc driver ını ekstradan çağırmış oluyoruz
            Class.forName("com.mysql.jdbc.Driver");

        } catch (ClassNotFoundException ex) {
            System.out.println("jdbc driver not found");
        }
        try {
            con = DriverManager.getConnection(url, Database.user_name, Database.pass);
            //System.out.println("Veritabi baglantisi basarili");

        } catch (SQLException ex) {
            System.out.println("Database conneciton failed");
        }
    }

    //Kullanıcı resimi seçtikten sonra yapılan işleri içeriyor
    public String allTras() {
        parseResult = "";
        boolean status;
        plug.removeAll(plug);
        path = imagePath();

        grayScale(path);

        gauss(path);

        result = imageRead(path);
        result = result.toUpperCase();

        ArrayList<PlugData> plug = demoParseText(result);
        status = addplug(plug);

        if (status == false) {
            parseResult = "";
            ArrayList<PlugData> plug2;
            scaling();
            result = imageRead(path);
            result = result.toUpperCase();
            plug2 = demoParseText(result);
            status = addplug(plug2);
        }
        if (status == false) {
            parseResult = result;
        }

        return parseResult;

    }

    //Kullanıcının okuycağı fişi seçmesini sağlıyor
    public String imagePath() {
        String path;
        JFileChooser f = new JFileChooser();
        int retunValue = f.showSaveDialog(f);
        if (retunValue == JFileChooser.APPROVE_OPTION) {
            path = f.getSelectedFile().getAbsolutePath();
            return path;
        }
        return "User clicked CANCEL";
    }

    //OpenCV kullnarak resmin gri versiyonunu çıkartıyor
    public void grayScale(String path) {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            File input = new File(path);
            BufferedImage image = ImageIO.read(input);

            Mat mat = imageConverMat(image);

            byte[] data = new byte[mat.rows() * mat.cols() * (int) (mat.elemSize())];
            mat.get(0, 0, data);

            BufferedImage image2 = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_BYTE_GRAY);
            image2.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);

            File ouptut = new File("grayscale.jpg");
            ImageIO.write(image2, "jpg", ouptut);

            setPath(ouptut.getPath());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //Fişteki ürünleri parse ediyor
    public String selectProduct(String text) {
        String product = "";
        int percent = 0, star = 0, lineSIndex, lineEIndex, lineEIndex2;

        while (percent != -1 && star != -1) {
            percent = text.indexOf("%", star);
            star = text.indexOf("*", percent);

            lineSIndex = text.lastIndexOf("\n", percent);

            lineEIndex = text.indexOf("\n", percent);
            lineEIndex2 = text.indexOf("\n", star);

            if (lineEIndex == lineEIndex2) {
                product += "\n" + text.substring(lineSIndex + 1, lineEIndex);
            }
        }
        return product;

    }

    //Gauss filitresi ile resmi keskinleştiriyor
    public void gauss(String path) {
        try {

            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            Mat source
                    = Imgcodecs.imread(path, IMREAD_COLOR);
            Mat destination = new Mat(source.rows(), source.cols(), source.type());

            Imgproc.GaussianBlur(source, destination, new Size(0, 0), 10);
            Core.addWeighted(source, 1.5, destination, -0.5, 0, destination);

            Imgcodecs.imwrite(path, destination);
        } catch (Exception e) {
        }
    }

    //Tessract kullarak işlenmiş resmi text haline getiriyor
    public String imageRead(String path) {
        Tesseract tesseract = new Tesseract();
        String text = "";
        try {

            tesseract.setDatapath("C:\\Tesseract-OCR\\tessdata");
            tesseract.setLanguage("tur");

            text = tesseract.doOCR(new File(path));
        } catch (TesseractException e) {
            System.out.println("İmage parsing işlemi sırasında hata alındı");
        }
        return text;
    }

    //Image üzerinde işlem yapabilmek için mat'a convert edip yapıyoruz
    public Mat imageConverMat(BufferedImage image) {
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);

        Mat mat2 = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
        Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_RGB2GRAY);

        return mat2;
    }

    //Değişkeni aldıktan sonra satır sonu kontrolü yapıyor
    public boolean cursorLine(String text, int coIndex) {

        int control1 = text.indexOf("\n", coIndex + 2);
        int control2 = text.indexOf(" ", coIndex + 2);

        if (control1 > control2 && control2 != -1) {
            //Satırın devamında bir değişken daha var
            return true;
        } else {
            //Satırda tek değişken var
            return false;
        }

    }

    //Diğer parse fonskiyonlarını ve kendi içindeki parse işlemlerini gerçekleştiriyor
    public ArrayList<PlugData> demoParseText(String text) {
        String key, value, product;
        int startLine, colone, endline, counter = 0;
        //System.out.println(text);
        String[] var = {"TARİH", "TARIH", "FİŞ NO", "FIŞ NO", "FIS NO", "FİS NO", "FİİŞ NO", "EİZ N0", "FİS ND", "FİS N0"};
        String[] var2 = new String[2];
        for (String v : var) {

            startLine = text.indexOf(v);
            if (startLine == -1) {
                continue;
            }
            var2[counter] = v;
            counter++;

        }
        //Dinamik bir yapı olmamasının sebebi tesseractın bazen görüntüleri iyi
        //okumamasından dolayı database işlemleri sırasında problem çıkıyor
        try {

            startLine = text.indexOf("\n");

            key = "İsletme Adı";
            value = text.substring(0, startLine);

            parseResult += value + "\n";
            content.put(key, value);

            for (String v : var2) {
                startLine = text.indexOf(v);
                colone = text.indexOf(":", startLine);

                boolean result = cursorLine(text, colone + 2);
                if (result) {
                    endline = text.indexOf(" ", colone + 2);
                } else {
                    endline = text.indexOf("\n", colone + 2);
                }
                key = text.substring((startLine), (colone)).trim();
                value = text.substring((colone + 1), endline).trim();
                value = controlNumber(value);

                parseResult += key + " : " + value + "\n";

                content.put(key, value);
            }

            startLine = text.indexOf("TOPLAM");
            colone = text.indexOf(" ", startLine);
            endline = text.indexOf("\n", startLine);
            key = "TOPLAM";
            value = text.substring((colone + 1), endline).trim();
            value = controlNumber(value);
            parseResult += key + " : " + value + "\n";
            content.put(key, value);

            product = selectProduct(text);

            parseResult += product + "\n";

            plug.add(new PlugData(content.get("İsletme Adı"), content.get(var2[0]), content.get(var2[1]), product, Float.valueOf(content.get("TOPLAM"))));
            return plug;
        } catch (Exception e) {
            return null;
        }
    }

    //İşlenmiş resmi dosyanın üzerine yazıyor
    public void writeİmage(BufferedImage image) {
        try {
            ImageIO.write(image, "jpg", new File(path));
        } catch (IOException ex) {
            System.out.println("İmage yazılırken problem çıktı");
        }
    }

    //Seçilen resmi bufferedImage yazıyor
    public BufferedImage getImage() {
        File fPath = new File(path);

        BufferedImage image = null;

        try {
            image = ImageIO.read(fPath);
        } catch (IOException ex) {
            Logger.getLogger(Transactions.class.getName()).log(Level.SEVERE, null, ex);
        }

        return image;
    }

    //Resmin RGB sini dönüyor
    public double getRGB() {
        double value = 0;
        BufferedImage image = getImage();

        try {
            value = image.getRGB(image.getTileWidth() / 2, image.getTileHeight() / 2);
        } catch (Exception ex) {
            System.out.println("getRGBde porblem var");
        }

        return value;
    }

    //Gönderilen değerlere göre resmi yeniden boyutlandırıyor
    public void Rescale(float scale, float offset) {
        BufferedImage image = getImage();

        BufferedImage newImage = new BufferedImage(1050, 1024, image.getType());

        Graphics2D graphic = newImage.createGraphics();

        graphic.drawImage(image, 0, 0, 1050, 1024, null);

        graphic.dispose();

        RescaleOp rescale = new RescaleOp(scale, offset, null);

        BufferedImage pImage = rescale.filter(newImage, null);

        writeİmage(pImage);

    }

    //Yeniden boyutlandırmaya gönderilecek resmin slace ve offset değerlerini berliyor
    public void scaling() {
        double value = getRGB();
        try {
            if (value >= -1.4211511E7 && value < -7254228) {
                Rescale(3f, -10f);
            } else if (value >= -7254228 && value < -2171170) {
                Rescale(1.455f, -47f);
            } else if (value >= -2171170 && value < -1907998) {
                Rescale(1.35f, -10f);
            } else if (value >= -1907998 && value < -257) {
                Rescale(1.19f, 0.5f);
            } else if (value >= -257 && value < -1) {
                Rescale(1f, 0.5f);
            } else if (value >= -1 && value < 2) {
                Rescale(1f, 0.35f);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    //Şirket kontolü yapıcak yoksa eklenicek
    public void isThereCompany(String name) {

        String query = "SELECT *FROM company WHERE name='" + name + "'";

        try {
            statement = con.createStatement();

            ResultSet rs = statement.executeQuery(query);

            if (!rs.next()) {

                addCompany(name);
            }

        } catch (SQLException ex) {

            Logger.getLogger(Transactions.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Kontrol yapıldığında şirket bulunmuyorsa eklenicek
    public void addCompany(String name) {

        String query = "INSERT INTO company (Name) VALUES (?)";

        try {
            preparedstatement = con.prepareStatement(query);

            preparedstatement.setString(1, name);

            preparedstatement.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(Transactions.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Okunan değeri parse ederek database yazılacak hale getiriyor
    public String controlNumber(String text) {
        String newText = "";

        for (int i = 0; i < text.length(); i++) {

            if (text.charAt(i) == ',' && newText.indexOf(".") == -1) {
                newText += ".";
                continue;
            }

            if (text.charAt(i) > 43 && text.charAt(i) < 58 || text.charAt(i) == '\\' && newText.charAt(i) != ',') {
                //System.out.println(text.charAt(i));
                newText += text.charAt(i);
            } else if (text.indexOf(",") == -1 && !newText.isEmpty() && newText.indexOf(",") == -1 && newText.indexOf(".") == -1) {
                newText += ".";
            }

        }
        newText = newText.trim();

        return newText;
    }

    //Parse işlemi gerçekleşmiş resmi database yazıyor
    public boolean addplug(ArrayList<PlugData> plug) {
        if (plug == null) {
            return false;
        }
        int id = 0;
        String date = "", pt = "", name = "", no = "";
        float total = 0;

        for (PlugData p : plug) {
            name = p.getMarketName();
            no = p.getPlugNo();
            date = p.getDate();
            pt = p.getProduct();
            total = p.getTotalPrice();
        }
        if (name == null || no == null || date == null || pt == null) {
            return false;
        }

        isThereCompany(name);
        String query = "SELECT *FROM company WHERE name='" + name + "'";

        try {
            statement = con.createStatement();

            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {

                id = rs.getInt("ID");

            }

            query = "INSERT INTO plug (ID,date,plugNo,product,total) VALUES (?,?,?,?,?)";

            preparedstatement = con.prepareStatement(query);

            preparedstatement.setInt(1, id);

            preparedstatement.setString(2, date);

            preparedstatement.setString(3, no);

            preparedstatement.setString(4, pt);

            preparedstatement.setFloat(5, total);

            preparedstatement.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(Transactions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    //Databasedeki verileri sıralamak için kullandığım fonksiyon
    public ArrayList<PlugData> sorting(String var) {

        String query = "SELECT * FROM plug p,company c Where p.ID=c.ID ORDER BY total " + var;

        ArrayList<PlugData> result = list(query);

        return result;
    }

    //Databasede arama yapmak için kulladığım fonsksiyon
    public ArrayList<PlugData> search(String nm, String dt) {
        String query = "";

        if (!nm.isEmpty() && !dt.isEmpty()) {
            query = "SELECT *FROM company c,plug p Where p.ID=c.ID AND date='" + dt + "'" + "AND name='" + nm + "'";
        } else if (!nm.isEmpty() && dt.isEmpty()) {
            query = "SELECT *FROM company c,plug p Where p.ID=c.ID AND name='" + nm + "'";
        } else {
            query = "SELECT *FROM company c,plug p Where p.ID=c.ID AND date='" + dt + "'";
        }

        ArrayList<PlugData> result = list(query);

        return result;
    }

    //Veritabanından verileri çekikten sonra menude tabloya basıcaz
    public ArrayList<PlugData> getAllDB() {

        String query = "SELECT *FROM company c,plug p Where p.ID=c.ID";

        ArrayList<PlugData> result = list(query);

        return result;
    }

    //Sorgu sonucu dönemem satırları listeye ekleliyor
    public ArrayList<PlugData> list(String query) {

        ArrayList<PlugData> result = new ArrayList<PlugData>();

        try {
            statement = con.createStatement();

            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {

                String name = rs.getString("Name");
                String date = rs.getString("date");
                String pNo = rs.getString("plugNo");
                String product = rs.getString("product");
                float total = rs.getFloat("total");

                result.add(new PlugData(name, date, pNo, product, total));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Transactions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
