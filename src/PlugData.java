public class PlugData {
    
    private String marketName;
    private String date;
    private String plugNo;
    private String product;
    private String totalPrice;

    public PlugData(String marketName, String date, String plugNo, String product, String totalPrice) {
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

    public String getPlugNo() {
        return plugNo;
    }

    public void setPlugNo(String plugNo) {
        this.plugNo = plugNo;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    
   
}
