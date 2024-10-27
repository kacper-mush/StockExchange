package investor;

import system.SESystem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SMATracker {
    private final Map<String, SMAInfo> smaInfoMap = new HashMap<>();

    public SMATracker(SESystem system) {
        Arrays.stream(system.getStockIDs()).forEach(stock -> smaInfoMap.put(stock, new SMAInfo(system, stock)));
    }

    public void updateSMA() {
        smaInfoMap.forEach((stock, smaInfo) -> smaInfo.updateSMA());
    }

    public double getSignalStrength(String stock) {
        return smaInfoMap.get(stock).getSignalStrength();
    }
}
