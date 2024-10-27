package order;

import investor.Investor;
import investor.InvestorFactory;
import io.InputInfo;
import io.InputReader;
import org.junit.jupiter.api.Test;
import system.SESystem;

import static org.junit.jupiter.api.Assertions.*;

class StockOrderTest {
    @Test
    void testStockOrder() {
        String[] args = {"order/test.txt", "10"};
        InputInfo info;
        try {
            info = InputReader.readInput(args);
        } catch (InputReader.InputException e) {
            fail("Should not throw exception");
            // If this throws, add "src/" to the path
            return;
        }

        SESystem system = new SESystem(info);
        Investor investor;
        try {
            investor = InvestorFactory.createInvestor(info.getInvestorCounts().keySet().iterator().next(), system, info.getWalletStocks(), info.getWalletCashCount());
        } catch (InvestorFactory.UnhandledInvestorTypeException e) {
            fail("Should not throw exception");
            return;
        }

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setType(null);
        orderInfo.setDueType(DueType.IMMEDIATE);
        orderInfo.setDueDate(1);
        orderInfo.setStockID("stockID");
        orderInfo.setQuantity(100);
        orderInfo.setPriceLimit(100);
        orderInfo.setInvestor(investor);

        assertThrows(StockOrder.BadOrderException.class, () -> new StockOrder(orderInfo));

        orderInfo.setType(OrderType.BUY);
        orderInfo.setDueType(null);

        assertThrows(StockOrder.BadOrderException.class, () -> new StockOrder(orderInfo));

        orderInfo.setDueType(DueType.DUE);
        orderInfo.setDueDate(null);

        assertThrows(StockOrder.BadOrderException.class, () -> new StockOrder(orderInfo));

        orderInfo.setDueDate(1);
        orderInfo.setStockID(null);

        assertThrows(StockOrder.BadOrderException.class, () -> new StockOrder(orderInfo));

        orderInfo.setStockID("stockID");
        orderInfo.setQuantity(-1);

        assertThrows(StockOrder.BadOrderException.class, () -> new StockOrder(orderInfo));

        orderInfo.setQuantity(0);

        assertThrows(StockOrder.BadOrderException.class, () -> new StockOrder(orderInfo));

        orderInfo.setQuantity(1);
        orderInfo.setPriceLimit(-1);

        assertThrows(StockOrder.BadOrderException.class, () -> new StockOrder(orderInfo));

        orderInfo.setPriceLimit(0);

        assertThrows(StockOrder.BadOrderException.class, () -> new StockOrder(orderInfo));

        orderInfo.setPriceLimit(1);
        orderInfo.setInvestor(null);

        assertThrows(StockOrder.BadOrderException.class, () -> new StockOrder(orderInfo));

        orderInfo.setInvestor(investor);
        orderInfo.setDueType(DueType.IMMEDIATE);

        assertDoesNotThrow(() -> new StockOrder(orderInfo));


    }

}