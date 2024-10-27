package investor;

import order.DueType;
import order.OrderInfo;
import order.OrderType;
import order.StockOrder;
import system.SESystem;

import java.util.*;

public class RandomInvestor extends Investor {
    private final double orderChance;
    private final double buyChance;
    private static final double ORDER_CHANCE_LOWER = 0.0001;
    private static final double ORDER_CHANCE_UPPER = 0.3;
    private static final double BUY_CHANCE_LOWER = 0.3;
    private static final double BUY_CHANCE_UPPER = 0.7;
    private static final int BUY_VARIATION_LOWER = 0;
    private static final int BUY_VARIATION_UPPER = 3;
    private static final int SELL_VARIATION_LOWER = -2;
    private static final int SELL_VARIATION_UPPER = 5;
    private static final double DUE_CHANCE = 0.8;
    private static final double PERSISTENT_CHANCE = 0.001;
    private static final double IMMEDIATE_CHANCE = 0.1;
    private static final int MIN_DUE_DATE = 1;
    private static final int MAX_DUE_DATE = 50;


    public RandomInvestor(SESystem system, Map<String, Integer> walletStocks, int walletCashCount) {
        super(system, walletStocks, walletCashCount);
        // gives the investors some diversity in their decision-making
        Random random = new Random();
        orderChance = random.nextDouble(ORDER_CHANCE_LOWER, ORDER_CHANCE_UPPER);
        buyChance = random.nextDouble(BUY_CHANCE_LOWER, BUY_CHANCE_UPPER);
    }

    public Optional<StockOrder> decideAndOrder() {
        Random random = new Random();

        // Randomly decide if we want to make an order
        if (random.nextDouble(0, 1) > orderChance) {
            return Optional.empty();
        }

        OrderInfo orderInfo = new OrderInfo();

        // Randomly choose between buying and selling
        if (random.nextDouble(0, 1) < buyChance) {
            orderInfo.setType(OrderType.BUY);
        } else {
            orderInfo.setType(OrderType.SELL);
        }

        try {
            setRandomValidStockID(random, orderInfo);
            setRandomPriceLimit(random, orderInfo);
            setRandomQuantity(random, orderInfo);
            setRandomDueType(random, orderInfo);
            orderInfo.setInvestor(this);
        } catch (CannotMakeOrderException e) {
            return Optional.empty();
        }

        try {
            return Optional.of(new StockOrder(orderInfo));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void setRandomValidStockID(Random random, OrderInfo orderInfo) throws CannotMakeOrderException {
        if (orderInfo.getType() == OrderType.BUY) {
            // We want to buy a stock that we can afford at least 1 unit of
            String[] stockIDs = Arrays.stream(system.getStockIDs())
                    .filter(stock -> system.getStockPrice(stock) <= cash)
                    .toArray(String[]::new);
            if (stockIDs.length == 0) {
                throw new CannotMakeOrderException("Cannot afford any stock");
            }
            orderInfo.setStockID(stockIDs[random.nextInt(stockIDs.length)]);
        } else {
            // we do not keep stock keys that have 0 quantity
            if (stocks.isEmpty()) {
                throw new CannotMakeOrderException("No stocks to sell");
            }
            orderInfo.setStockID(stocks.keySet().toArray(new String[0])[random.nextInt(stocks.size())]);
        }
    }

    private void setRandomPriceLimit(Random random, OrderInfo orderInfo) {
        int stockMarketPrice = system.getStockPrice(orderInfo.getStockID());

        ArrayList<Integer> possibleVariations = new ArrayList<>();
        if (orderInfo.getType() == OrderType.BUY) {
            for (int variation = BUY_VARIATION_LOWER; variation < BUY_VARIATION_UPPER + 1; variation++) {
                if (stockMarketPrice + variation <= cash && stockMarketPrice + variation > 0) {
                    possibleVariations.add(variation);
                }
            }
        } else {
            for (int variation = SELL_VARIATION_LOWER; variation < SELL_VARIATION_UPPER + 1; variation++) {
                if (stockMarketPrice + variation > 0) {
                    possibleVariations.add(variation);
                }
            }
        }
        if (possibleVariations.isEmpty()) {
            throw new RuntimeException("No possible variations");
        }

        // Calculate price limit based on stock price and variation
        orderInfo.setPriceLimit(stockMarketPrice + possibleVariations.get(random.nextInt(possibleVariations.size())));
    }

    private void setRandomQuantity(Random random, OrderInfo orderInfo) throws CannotMakeOrderException {
        int maxQuantity;
        if (orderInfo.getType() == OrderType.BUY) {
            // Calculate max quantity we can buy
            maxQuantity = cash / orderInfo.getPriceLimit();
        } else {
            maxQuantity = stocks.get(orderInfo.getStockID());
        }

        if (maxQuantity == 0) {
            throw new CannotMakeOrderException("Cannot afford any stock or no stock to sell");
        }

        orderInfo.setQuantity(random.nextInt(1, maxQuantity + 1));
    }

    private void setRandomDueType(Random random, OrderInfo orderInfo) {
        double dueTypeRandom = random.nextDouble(0, 1);
        if (dueTypeRandom < DUE_CHANCE) {
            orderInfo.setDueType(DueType.DUE);
        } else if (dueTypeRandom < DUE_CHANCE + PERSISTENT_CHANCE) {
            orderInfo.setDueType(DueType.PERSISTENT);
        } else if (dueTypeRandom < DUE_CHANCE + PERSISTENT_CHANCE + IMMEDIATE_CHANCE) {
            orderInfo.setDueType(DueType.IMMEDIATE);
        } else {
            orderInfo.setDueType(DueType.FULL_EXECUTION);
        }

        // If due type is DUE, set a random due date
        if (orderInfo.getDueType() == DueType.DUE) {
            orderInfo.setDueDate(system.getCurrentRound() + random.nextInt(MIN_DUE_DATE, MAX_DUE_DATE + 1));
        }
    }


    public String toString() {
        return super.toString() + ", order chance: " + orderChance + ", buy chance: " + buyChance;
    }
}
