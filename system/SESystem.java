package system;

import investor.*;
import io.InputInfo;
import order.OrderType;
import order.StockOrder;
import utils.IndexPermutation;

import java.util.*;

public class SESystem {
    private final ArrayList<Investor> investors;
    private final HashMap<String, ArrayList<StockOrder>> buyOrdersSheet = new HashMap<>();
    private final HashMap<String, ArrayList<StockOrder>> sellOrdersSheet = new HashMap<>();
    private final Map<String, Integer> stockPrices;
    private final Map<String, Integer> firstStockPrices;
    private final int roundCount;
    private int currentRound = 0;
    private int totalTransactionCount = 0;

    private final SMATracker smaTracker;

    private final Comparator<StockOrder> buyOrdersComparator = Comparator.comparing(StockOrder::getPriceLimit).reversed()
            .thenComparing(StockOrder::getRound)
            .thenComparing(StockOrder::getPriorityInRound);

    private final Comparator<StockOrder> sellOrdersComparator = Comparator.comparing(StockOrder::getPriceLimit)
            .thenComparing(StockOrder::getRound)
            .thenComparing(StockOrder::getPriorityInRound);

    public SESystem(InputInfo inputInfo) {
        roundCount = inputInfo.getRoundCount();
        firstStockPrices = new HashMap<>(inputInfo.getStockPrices());
        stockPrices = inputInfo.getStockPrices();
        smaTracker = new SMATracker(this);

        for (String stockID : stockPrices.keySet()) {
            buyOrdersSheet.put(stockID, new ArrayList<>());
            sellOrdersSheet.put(stockID, new ArrayList<>());
        }

        investors = new ArrayList<>();

        for (InvestorType type : inputInfo.getInvestorCounts().keySet()) {
            int count = inputInfo.getInvestorCounts().get(type);
            for (int i = 0; i < count; i++) {
                try {
                    investors.add(InvestorFactory.createInvestor(type, this, inputInfo.getWalletStocks(), inputInfo.getWalletCashCount()));
                } catch (InvestorFactory.UnhandledInvestorTypeException e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            }
        }
    }

    public void run() {
        for (currentRound = 0; currentRound < roundCount; currentRound++) {
            // Make sure we don't handle invalid orders
            deleteOldOrders(currentRound);

            getInvestorOrders(currentRound);

            buyOrdersSheet.values().forEach(arrayList -> arrayList.sort(buyOrdersComparator));
            sellOrdersSheet.values().forEach(arrayList -> arrayList.sort(sellOrdersComparator));


            // Go through each sell order
            for (String stockID : stockPrices.keySet()) {
                ArrayList<StockOrder> possibleBuyOrders = buyOrdersSheet.get(stockID);

                for (StockOrder buyOrder : possibleBuyOrders) {
                    ArrayList<StockOrder> possibleSellOrders = sellOrdersSheet.get(stockID);

                    if (!buyOrder.canBeClosedWith(possibleSellOrders)) {
                        continue;
                    }
                    for (StockOrder sellOrder : possibleSellOrders) {
                        if (buyOrder.isFullyExecuted()) {
                            break;
                        }

                        Optional<Integer> closePrice = buyOrder.closeDealWith(sellOrder);
                        if (closePrice.isPresent()) {
                            stockPrices.put(buyOrder.getStockID(), closePrice.get());
                            totalTransactionCount++;
                        }
                    }
                }
            }
            smaTracker.updateSMA();
        }
    }

    public void printResults() {
        System.out.println("Investors: ");
        for (Investor investor : investors) {
            System.out.println(investor);
        }
        System.out.println("Stock Prices at start:"+ firstStockPrices);
        System.out.println("Stock Prices at end:"+ stockPrices);
        System.out.println("Starting net worth: " + investors.get(0).getStartNetWorth()); // There is at least 1 investor

        int randomCount = 0;
        int betterRandomCount = 0;
        int randomSum = 0;

        int smaCount = 0;
        int betterSMACount = 0;
        int smaSum = 0;

        int sumNetWorth = 0;

        // I know, it's hacky, but it's just for the sake of statistics
        for (Investor investor : investors) {
            int netWorth = investor.calculateNetWorth();
            sumNetWorth += netWorth;
            if (investor instanceof RandomInvestor) {
                randomSum += netWorth;
                randomCount++;
                if (investor.calculateNetWorth() > investors.get(0).getStartNetWorth()) {
                    betterRandomCount++;
                }
            } else if (investor instanceof SMAInvestor) {
                smaSum += netWorth;
                smaCount++;
                if (investor.calculateNetWorth() > investors.get(0).getStartNetWorth()) {
                    betterSMACount++;
                }
            }

        }
        System.out.println("Average net worth now: " + sumNetWorth / investors.size());
        System.out.println("Average net worth of random investors: " + randomSum / randomCount);
        System.out.println("Average net worth of SMA investors: " + smaSum / smaCount);
        double betterFactor = (double) (smaSum * randomCount) /(smaCount * randomSum);
        System.out.println("How many times are SMA investors better compared to random investors: " + betterFactor);
        System.out.println("Percent of random investors of which net worth increased: " + betterRandomCount * 100 / randomCount + "%");
        System.out.println("Percent of SMA investors of which net worth increased: " + betterSMACount * 100 / smaCount + "%");
        System.out.println("Total transactions: " + totalTransactionCount);
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getStockPrice(String stockID) {
        return stockPrices.get(stockID);
    }

    public String[] getStockIDs() {
        return stockPrices.keySet().toArray(new String[0]);
    }

    public double getSMASignalStrength(String stockID) {
        return smaTracker.getSignalStrength(stockID);
    }

    private void deleteOldOrders(int round) {
        buyOrdersSheet.values().forEach(array -> array.removeIf(order -> order.isOverdue(round) || order.isFullyExecuted()));
        sellOrdersSheet.values().forEach(array -> array.removeIf(order -> order.isOverdue(round) || order.isFullyExecuted()));
    }

    private void getInvestorOrders(int round) {
        IndexPermutation indexPermutation = new IndexPermutation(investors.size());
        int priority = 0;
        while (indexPermutation.hasNext()) {
            int idx = indexPermutation.getNext();
            Optional<StockOrder> stockOrder = investors.get(idx).decideAndOrder();

            if (stockOrder.isEmpty() || !isOrderValid(stockOrder.get())) {
                continue;
            }

            StockOrder order = stockOrder.get();
            order.setRound(round);
            order.setPriorityInRound(priority++);

            if (order.getType() == OrderType.BUY) {
                buyOrdersSheet.get(order.getStockID()).add(order);
            } else {
                sellOrdersSheet.get(order.getStockID()).add(order);
            }
        }
    }

    private boolean isOrderValid(StockOrder stockOrder) {
        int priceLimit = stockOrder.getPriceLimit();

        // price limit should be within 10 of the current price
        int stockPrice = stockPrices.get(stockOrder.getStockID());
        if (Math.abs(stockPrice - priceLimit) > 10) {
            return false;
        }

        // investor should have enough cash or stock to place the order
        if (stockOrder.getType() == OrderType.BUY
                && stockOrder.getQuantity() * priceLimit > stockOrder.getInvestor().getCash()) {
            return false;
        }

        if (stockOrder.getType() == OrderType.SELL &&
                stockOrder.getQuantity() > stockOrder.getInvestor().getStockCount(stockOrder.getStockID())) {
            return false;
        }
        return true;
    }
}
