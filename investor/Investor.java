package investor;

import order.StockOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import system.SESystem;
public abstract class Investor {
    public static class CannotMakeOrderException extends Exception {
        public CannotMakeOrderException(String message) {
            super(message);
        }
    }
     protected Map<String, Integer> stocks;
     protected int cash;
     protected SESystem system;
     protected int totalBuys = 0;
     protected int totalSells = 0;
     protected static int lastId = 0;
     protected int id = lastId;
     protected int startNetWorth;

    public Investor(SESystem system, Map<String, Integer> walletStocks, int walletCashCount) {
        this.system = system;
        this.stocks = new HashMap<>(walletStocks);
        for (Map.Entry<String, Integer> entry : stocks.entrySet()) {
            if (entry.getValue() == 0) {
                stocks.remove(entry.getKey());
            }
        }
        this.cash = walletCashCount;
        lastId++;
        startNetWorth = calculateNetWorth();
    }

    public abstract Optional<StockOrder> decideAndOrder();
    public String toString() {
        ArrayList<String> stockStrings = new ArrayList<>();
        stocks.forEach((k, v) -> stockStrings.add(k + ":" + v));
        return id + ", Total: " + calculateNetWorth() + ", Cash: " + cash + ", Stocks: " + String.join(", ", stockStrings)
                + ", transactions: " + (totalBuys + totalSells);
    }

    public int getCash() {
        return cash;
    }

    public int getStartNetWorth() {
        return startNetWorth;
    }
    public int getStockCount(String stockID) {
        return stocks.getOrDefault(stockID, 0);
    }

    public void pay(int amount) {
        cash -= amount;
        totalBuys++;
    }

    public void pay(String stockID, int amount) {
        stocks.put(stockID, stocks.get(stockID) - amount);
        if (stocks.get(stockID) == 0) {
            stocks.remove(stockID);
        }
    }

    public void receive(int amount) {
        totalSells++;
        cash += amount;
    }

    public void receive(String stockID, int amount) {
        stocks.put(stockID, stocks.getOrDefault(stockID, 0) + amount);
    }

    public int getId() {
        return id;
    }

    public int calculateNetWorth() {
        int total = 0;
        for (Map.Entry<String, Integer> entry : stocks.entrySet()) {
            total += system.getStockPrice(entry.getKey()) * entry.getValue();
        }
        return total + cash;
    }
}
