public class PlugData {
    
    private String marketName;
    private String date;
    private int plugNo;
    private String product;
    private int totalPrice;

    public PlugData(String marketName, String date, int plugNo, String product, int totalPrice) {
        this.marketName = marketName;
        this.date = date;
        this.plugNo = plugNo;
        this.product = product;
        this.totalPrice = totalPrice;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPlugNo() {
        return plugNo;
    }

    public void setPlugNo(int plugNo) {
        this.plugNo = plugNo;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    
   
}
