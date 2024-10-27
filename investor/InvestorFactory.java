package investor;

import system.SESystem;

import java.util.Map;

public class InvestorFactory {
    public static class UnhandledInvestorTypeException extends Exception {
        public UnhandledInvestorTypeException(String message) {
            super(message);
        }
    }
    public static Investor createInvestor(InvestorType type, SESystem system, Map<String, Integer> walletStocks, int walletCashCount) throws UnhandledInvestorTypeException {
        return switch (type) {
            case SMA -> new SMAInvestor(system, walletStocks, walletCashCount);
            case RANDOM -> new RandomInvestor(system, walletStocks, walletCashCount);
            default -> throw new UnhandledInvestorTypeException("Unhandled InvestorType: " + type);
        };
    }
}
