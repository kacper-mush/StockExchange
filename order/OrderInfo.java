package order;

import investor.Investor;

public class OrderInfo {
    private OrderType type;
    private DueType dueType;
    private Integer dueDate;
    private String stockID;
    private int quantity;
    private int priceLimit;
    private Investor investor;


    public OrderType getType() {
        return type;
    }

    public DueType getDueType() {
        return dueType;
    }

    public Integer getDueDate() {
        return dueDate;
    }

    public String getStockID() {
        return stockID;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPriceLimit() {
        return priceLimit;
    }

    public Investor getInvestor() {
        return investor;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public void setDueType(DueType dueType) {
        this.dueType = dueType;
    }

    public void setDueDate(Integer dueDate) {
        this.dueDate = dueDate;
    }

    public void setStockID(String stockID) {
        this.stockID = stockID;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPriceLimit(int priceLimit) {
        this.priceLimit = priceLimit;
    }

    public void setInvestor(Investor investor) {
        this.investor = investor;
    }
}
