package order;

import investor.Investor;

import java.util.ArrayList;
import java.util.Optional;

public class StockOrder {

    public static class BadOrderException extends Exception {
        public BadOrderException(String message) {
            super(message);
        }
    }
    private final OrderType type;
    private final DueType dueType;
    private final Integer dueDate;
    private final String stockID;
    private int quantity;
    private final int priceLimit;
    private final Investor investor;
    private boolean isNew = true;
    private int round = -1; // will be set by the system
    private int priorityInRound = -1; // will be set by the system

    public StockOrder(OrderInfo orderInfo) throws BadOrderException {
        this.type = orderInfo.getType();
        this.dueType = orderInfo.getDueType();
        this.dueDate = orderInfo.getDueDate();
        this.stockID = orderInfo.getStockID();
        this.quantity = orderInfo.getQuantity();
        this.priceLimit = orderInfo.getPriceLimit();
        this.investor = orderInfo.getInvestor();
        runChecks();
    }

    public void setRound(int round) {
        this.round = round;
    }

    public void setPriorityInRound(int priorityInRound) {
        this.priorityInRound = priorityInRound;
    }

    public int getRound() { return round; }

    public OrderType getType() {
        return type;
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

    public String toString() {
        return "StockOrder{" +
                "type=" + type +
                ", priceLimit=" + priceLimit +
                   ", quantity=" + quantity +
                ", dueType=" + dueType +
                ", dueDate=" + dueDate +
                ", stockID='" + stockID + '\'' +
                ", investor=" + investor.getId() +
                ", round=" + round +
                ", priorityInRound=" + priorityInRound +
                '}';
    }

    public int getPriorityInRound() { return priorityInRound; }

    public boolean isFullyExecuted() {
        return quantity == 0;
    }

    public boolean isOverdue(int currentRound) {
        return switch (dueType) {
            case DUE -> dueDate < currentRound;
            case IMMEDIATE -> currentRound > round;
            case FULL_EXECUTION -> {
                if (currentRound > round) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    public Optional<Integer> closeDealWith(StockOrder dealOrder) {
        Optional<Integer> closingPrice = canBeClosedWith(dealOrder);
        if (closingPrice.isEmpty()) {
            return Optional.empty();
        }

        int quantityToClose = Math.min(quantity, dealOrder.quantity);
        int fullPrice = quantityToClose * closingPrice.get();

        Investor seller = type == OrderType.BUY ? dealOrder.investor : investor;
        Investor buyer = type == OrderType.BUY ? investor : dealOrder.investor;

        /* Useful for debugging
        System.out.println("Deal closed: " + quantityToClose + " stocks of " + stockID +
                " for full price " + fullPrice + " at " + closingPrice + " per stock");
        System.out.println("Sold by investor: " + seller.getId() + ", bought by investor: " + buyer.getId());
        System.out.println("Offer 1: " + this);
        System.out.println("Offer 2: " + dealOrder);

         */

        buyer.pay(fullPrice);
        seller.receive(fullPrice);

        seller.pay(stockID, quantityToClose);
        buyer.receive(stockID, quantityToClose);

        quantity -= quantityToClose;
        dealOrder.quantity -= quantityToClose;
        isNew = false;
        dealOrder.isNew = false;
        return closingPrice;
    }

    public Optional<Integer> canBeClosedWith(StockOrder dealOrder) {
        int quantityToClose = Math.min(quantity, dealOrder.quantity);

        if (quantityToClose == 0 || !arePricesCloseable(dealOrder)) {
            return Optional.empty();
        }

        // The second hand deal cant be a full execution, it doesn't work that way
        if (dealOrder.dueType == DueType.FULL_EXECUTION) {
            return Optional.empty();
        }

        int closingPrice = getClosingPrice(dealOrder);
        int fullPrice = quantityToClose * closingPrice;

        Investor seller = type == OrderType.BUY ? dealOrder.investor : investor;
        Investor buyer = type == OrderType.BUY ? investor : dealOrder.investor;


        if (buyer.getCash() >= fullPrice && seller.getStockCount(stockID) >= quantityToClose) {
            return Optional.of(closingPrice);
        }
        return Optional.empty();
    }

    public boolean canBeClosedWith(ArrayList<StockOrder> ordersToPairWith) {
        if (dueType != DueType.FULL_EXECUTION) {
            for (StockOrder matchingOrder : ordersToPairWith) {
                if (canBeClosedWith(matchingOrder).isPresent()) {
                    return true;
                }
            }
            return false;
        }

        int quantityGathered = 0;
        int priceGathered = 0;
        int quantityToClose;
        // This order is full execution, we can only close with it if it can be closed fully
        for (StockOrder matchingOrder : ordersToPairWith) {
            Optional<Integer> closingPrice = canBeClosedWith(matchingOrder);
            if (closingPrice.isEmpty()) {
                continue;
            }
            quantityToClose = Math.min(quantity, matchingOrder.quantity);
            if (quantity - quantityGathered < quantityToClose) {
                quantityToClose = quantity - quantityGathered;
                priceGathered += quantityToClose * closingPrice.get();
                break;
            }
            quantityGathered += quantityToClose;
            priceGathered += quantityToClose * closingPrice.get();
        }
        if (quantityGathered == quantity && priceGathered <= investor.getCash()) {
            return true;
        }
        return false;
    }

    public boolean arePricesCloseable(StockOrder order) {
        if (type == OrderType.BUY) {
            return priceLimit >= order.priceLimit;
        } else {
            return priceLimit <= order.priceLimit;
        }
    }

    private int getClosingPrice(StockOrder dealOrder) {
        if (round < dealOrder.getRound()) {
            return priceLimit;
        } else if (round > dealOrder.getRound()) {
            return dealOrder.priceLimit;
        } else if (priorityInRound < dealOrder.getPriorityInRound()) {
            return priceLimit;
        } else {
            return dealOrder.priceLimit;
        }
    }

    private void runChecks() throws BadOrderException {
        if (priceLimit <= 0) {
            throw new BadOrderException("Price limit must be positive");
        }

        if (quantity <= 0) {
            throw new BadOrderException("Quantity must be positive");
        }

        if (dueType == null) {
            throw new BadOrderException("Due type must be specified");
        }

        if (dueType == DueType.DUE && dueDate == null) {
            throw new BadOrderException("Due date must be specified for due orders");
        }

        if (type == null) {
            throw new BadOrderException("Order type must be specified");
        }

        if (stockID == null) {
            throw new BadOrderException("Stock ID must be specified");
        }

        if (investor == null) {
            throw new BadOrderException("Investor must be specified");
        }

        if (type == OrderType.BUY && investor.getCash() < priceLimit * quantity) {
            throw new BadOrderException("Investor does not have enough cash to place the order");
        }

        if (type == OrderType.SELL && investor.getStockCount(stockID) < quantity) {
            throw new BadOrderException("Investor does not have enough stocks to place the order");
        }
    }
}
