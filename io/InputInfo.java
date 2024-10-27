package io;

import investor.InvestorType;

import java.util.Map;

public class InputInfo {
    private Map<InvestorType, Integer> investorCounts;
    private Map<String, Integer> stockPrices;
    private int walletCashCount;
    private Map<String, Integer> walletStocks;
    private int roundCount;

    public void setInvestorCounts(Map<InvestorType, Integer> investorCounts) {
        this.investorCounts = investorCounts;
    }

    public void setStocks(Map<String, Integer> stockPrices) {
        this.stockPrices = stockPrices;
    }

    public void setWalletCashCount(int walletCashCount) {
        this.walletCashCount = walletCashCount;
    }

    public void setWalletStocks(Map<String, Integer> walletStocks) {
        this.walletStocks = walletStocks;
    }

    public void setRoundCount(int roundCount) {
        this.roundCount = roundCount;
    }

    public Map<String, Integer> getStockPrices() {
        return stockPrices;
    }

    public Map<InvestorType, Integer> getInvestorCounts() {
        return investorCounts;
    }

    public String toString() {
        return "investor amounts: " + investorCounts + "\n" +
               "Stock prices: " + stockPrices + "\n" +
               "Wallet cash amount: " + walletCashCount + "\n" +
               "Wallet stocks: " + walletStocks + "\n" +
                "Round count: " + roundCount;
    }

    public Map<String, Integer> getWalletStocks() {
        return walletStocks;
    }

    public int getWalletCashCount() {
        return walletCashCount;
    }

    public int getRoundCount() {
        return roundCount;
    }
}
