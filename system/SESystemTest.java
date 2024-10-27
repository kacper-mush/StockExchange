package system;

import io.InputInfo;
import io.InputReader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SESystemTest {

    @Test
    void run() {
        // Only SMA's, nothing should change
        String[] args = {"system/testfiles/test1.txt", "10"};
        InputInfo info;
        try {
            info = InputReader.readInput(args);
        } catch (InputReader.InputException e) {
            fail("Should not throw exception");
            // If this throws, add "src/" to the path
            return;
        }

        SESystem system = new SESystem(info);
        system.run();
        assertEquals(system.getCurrentRound(), 10);
        for (String stockID : system.getStockIDs()) {
            assertEquals(system.getStockPrice(stockID), 100);
        }


        args = new String[]{"system/testfiles/testMoodle.txt", "1000"};

        try {
            info = InputReader.readInput(args);
        } catch (InputReader.InputException e) {
            fail("Should not throw exception");
            // If this throws, add "src/" to the path
            return;
        }

        system = new SESystem(info);
        system.run();
        assertEquals(system.getCurrentRound(), 1000);
        for (String stockID : system.getStockIDs()) {
            assertTrue(system.getStockPrice(stockID) > 50);
        }
    }
}