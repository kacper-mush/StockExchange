package investor;

import system.SESystem;
import java.util.LinkedList;

public class SMAInfo {
    private final SESystem system;
    private final String stock;
    private final LinkedList<Integer> sma5List = new LinkedList<>();
    private final LinkedList<Integer> sma10List = new LinkedList<>();
    private double signalStrength = 0;
    private boolean isReady = false;
    private double prevSMA5;
    private boolean wasSMA5Above;

    public SMAInfo(SESystem system, String stock) {
        this.system = system;
        this.stock = stock;
    }

    public void updateSMA() {
        sma5List.addLast(system.getStockPrice(stock));
        sma10List.addLast(system.getStockPrice(stock));

        if (sma5List.size() > 5) {
            sma5List.removeFirst();
        }

        if (sma10List.size() > 10) {
            sma10List.removeFirst();
        }

        double sma5, sma10;

        if (!isReady) {
            if (sma10List.size() == 10) {
                isReady = true;
                sma5 = sma5List.stream().mapToInt(Integer::intValue).average().orElse(0);
                sma10 = sma10List.stream().mapToInt(Integer::intValue).average().orElse(0);
                wasSMA5Above = sma5 > sma10;
                prevSMA5 = sma5;
            }
            // no signal for now as this is the first time sma10 is calculated or sma10 is not ready yet
            return;
        }


        sma5 = sma5List.stream().mapToInt(Integer::intValue).average().orElse(0);
        sma10 = sma10List.stream().mapToInt(Integer::intValue).average().orElse(0);

        boolean isSMA5Above = sma5 > sma10;
        boolean isSignal = wasSMA5Above != isSMA5Above;
        double signalStrengthAbs = Math.abs(sma5 - prevSMA5);

        wasSMA5Above = isSMA5Above;
        prevSMA5 = sma5;

        signalStrength = isSignal ? (isSMA5Above ? signalStrengthAbs : -signalStrengthAbs) : 0;
        // signalStrength is 0 if there is no signal
        // signalStrength is positive if sma5 crosses above sma10 - buy signal
        // signalStrength is negative if sma5 crosses below sma10 - sell signal
    }

    public double getSignalStrength() {
        return signalStrength;
    }
}
