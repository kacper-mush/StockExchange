package investor;

import order.DueType;
import order.OrderInfo;
import order.OrderType;
import order.StockOrder;
import system.SESystem;

import java.util.*;

public class SMAInvestor extends Investor {
    private static final int SMA_PERIOD = 5;
    private final double aggression;
    private static final double AGGRESSION_LOWER = 0.5;
    private static final double AGGRESSION_UPPER = 2;
    private double strongestSignalAbs = 0;
    public SMAInvestor(SESystem system, Map<String, Integer> walletStocks, int walletCashCount) {
        super(system, walletStocks, walletCashCount);
        Random random = new Random();
        aggression = random.nextDouble(AGGRESSION_LOWER, AGGRESSION_UPPER);
    }
    public Optional<StockOrder> decideAndOrder() {
        OrderInfo orderInfo = new OrderInfo();

        double bestSignalOverall;
        try {
            bestSignalOverall = setBestStockAndGetSignal(orderInfo);
        } catch (CannotMakeOrderException e) {
            return Optional.empty();
        }

        // The stronger the signal the more we are willing to waste on the stock to get it as fast as possible
        // Scaled from 0 to 1
        double signalScaleFactor = Math.min(Math.abs(bestSignalOverall) * aggression / strongestSignalAbs, 1);

        setQuantity(orderInfo, signalScaleFactor);
        setPriceLimit(orderInfo, signalScaleFactor);
        orderInfo.setDueType(DueType.DUE);
        orderInfo.setDueDate(system.getCurrentRound() + SMA_PERIOD);
        orderInfo.setInvestor(this);

        try {
            return Optional.of(new StockOrder(orderInfo));
        } catch (StockOrder.BadOrderException e) {
            return Optional.empty();
        }
    }

    private double setBestStockAndGetSignal(OrderInfo orderInfo) throws CannotMakeOrderException {
        String[] stockIDs = system.getStockIDs();

        String bestStockSell = null;
        String bestStockBuy = null;
        double bestSignalSell = 0;
        double bestSignalBuy = 0;

        for (String stockID : stockIDs) {
            // SMA signal strength is the product of the stock price and the SMA signal strength
            double newSignal = system.getSMASignalStrength(stockID) * system.getStockPrice(stockID);
            if (newSignal > bestSignalBuy) {
                bestStockBuy = stockID;
                bestSignalBuy = newSignal;
            }
            if (newSignal < bestSignalSell) {
                bestStockSell = stockID;
                bestSignalSell = newSignal;
            }
        }

        if (-bestSignalSell > strongestSignalAbs) {
            strongestSignalAbs = -bestSignalSell;
        }
        if (bestSignalBuy > strongestSignalAbs) {
            strongestSignalAbs = bestSignalBuy;
        }

        if (bestStockSell == null && bestStockBuy == null) {
            throw new CannotMakeOrderException("No stocks to buy or sell with SMA signal");
        }

        double bestSignalOverall;
        // Best signal is the one with the highest absolute value and the one that we can afford
        if (bestStockSell == null || getStockCount(bestStockSell) == 0) {
            orderInfo.setStockID(bestStockBuy);
            bestSignalOverall = bestSignalBuy;
            orderInfo.setType(OrderType.BUY);
        } else if (bestStockBuy == null || system.getStockPrice(bestStockBuy) > cash) {
            orderInfo.setStockID(bestStockSell);
            bestSignalOverall = bestSignalSell;
            orderInfo.setType(OrderType.SELL);
        }
        else {
            if (Math.abs(bestSignalBuy) - Math.abs(bestSignalSell) > 0) {
                orderInfo.setStockID(bestStockBuy);
                bestSignalOverall = bestSignalBuy;
                orderInfo.setType(OrderType.BUY);
            } else {
                orderInfo.setStockID(bestStockSell);
                bestSignalOverall = bestSignalSell;
                orderInfo.setType(OrderType.SELL);
            }
        }

        if (orderInfo.getStockID() == null) {
            throw new CannotMakeOrderException("No stocks to buy or sell with SMA signal");
        }
        return bestSignalOverall;
    }

    private void setPriceLimit(OrderInfo orderInfo, double signalScaleFactor) {
        int variation = (int)Math.ceil(10 * signalScaleFactor);
        if (orderInfo.getType() == OrderType.SELL) {
            variation = -variation;
        }

        orderInfo.setPriceLimit(system.getStockPrice(orderInfo.getStockID()) + variation);
    }

    private void setQuantity(OrderInfo orderInfo, double signalScaleFactor) {
        int maxQuantity;
        if (orderInfo.getType() == OrderType.BUY) {
            maxQuantity = (int)Math.floor((double)cash / system.getStockPrice(orderInfo.getStockID()));
        } else {
            maxQuantity = getStockCount(orderInfo.getStockID());
        }

        orderInfo.setQuantity((int)Math.ceil(signalScaleFactor * maxQuantity));
    }

    public String toString() {
        return super.toString() + ", SMA aggression: " + aggression;
    }
}
