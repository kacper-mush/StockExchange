package io;

import investor.InvestorType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class InputReader {
    public static class InputException extends Exception {
        public InputException(String message) {
            super(message);
        }
    }
    public static InputInfo readInput(String[] args) throws InputException {
        if (args.length != 2) {
            throw new InputException("Invalid number of arguments");
        }

        File file;
        try {
            file = new File(args[0]);
        }
        catch (NullPointerException e) {
            throw new InputException("File name is null");
        }

        Scanner lineReader;
        try {
            lineReader = new Scanner(file);
        }
        catch (FileNotFoundException e) {
            throw new InputException("File not found");
        }

        // Read input file
        InputInfo inputInfo = new InputInfo();

        try {
            inputInfo.setRoundCount(Integer.parseInt(args[1]));
        }
        catch (NumberFormatException e) {
            throw new InputException("Invalid round count: " + args[1]);
        }

        int inputLine = 0;
        while (lineReader.hasNextLine()) {
            String line = lineReader.nextLine();

            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#"))
                continue;

            switch (inputLine) {
                case 0:
                    // Read investors
                    inputInfo.setInvestorCounts(parseInvestors(line));
                    inputLine++;
                    break;
                case 1:
                    // Read stock names and last prices
                    inputInfo.setStocks(parseStocks(line));
                    inputLine++;
                    break;
                case 2:
                    inputInfo.setWalletCashCount(parseWalletCash(line));
                    inputInfo.setWalletStocks(parseWalletStocks(line, inputInfo.getStockPrices()));
                    inputLine++;
                    // Read wallet starting amounts
                    break;
            }
            if (inputLine == 3) {
                break;
            }
        }
        if (inputLine != 3) {
            throw new InputException("Too few lines in input file");
        }

        return inputInfo;
    }

    private static Map<InvestorType, Integer> parseInvestors(String line) throws InputException {
        InvestorType[] investorTypes = InvestorType.values();
        Map<InvestorType, Integer> investorAmounts = new HashMap<>();

        Scanner investorReader = new Scanner(line);
        while (investorReader.hasNext()) {
            String investor = investorReader.next();
            if (investor.length() != 1) {
                throw new InputException("Invalid investor specifier: " + investor);
            }
            char investorChar = investor.charAt(0);

            // Find corresponding investor type, increment amount in map, and if not found - throw exception
            boolean found = false;
            for (InvestorType type : investorTypes) {
                if (type.getUniqueChar() == investorChar) {
                    investorAmounts.putIfAbsent(type, 0);
                    investorAmounts.put(type, investorAmounts.get(type) + 1);
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new InputException("Invalid investor unique char: " + investorChar);
            }

        }
        return investorAmounts;
    }

    private static Map<String, Integer> parseStocks(String line) throws InputException {
        Map<String, Integer> stockPrices = new HashMap<>();
        Scanner stockReader = new Scanner(line);
        while (stockReader.hasNext()) {
            String stock = stockReader.next();
            String[] nameAndPrice = stock.split(":");
            if (nameAndPrice.length != 2) {
                throw new InputException("Invalid stock format: " + stock);
            }
            String stockName = nameAndPrice[0].toUpperCase();
            // Stock name must A-Z, and at most 5 characters
            boolean isNameValid = stockName.chars().allMatch(Character::isLetter) && stockName.length() <= 5;
            if (!isNameValid) {
                throw new InputException("Invalid stock name: " + stockName);
            }

            int stockPrice;
            try {
                stockPrice = Integer.parseInt(nameAndPrice[1]);
            }
            catch (NumberFormatException e) {
                throw new InputException("Invalid stock price: " + nameAndPrice[1]);
            }

            if (stockPrice <= 0) {
                throw new InputException("Stock price must be positive: " + stockPrice);
            }

            stockPrices.put(stockName, stockPrice);
        }
        return stockPrices;
    }

    private static int parseWalletCash(String line) throws InputException {
        int walletCashAmount;

        Scanner walletCashReader = new Scanner(line);
        if (!walletCashReader.hasNext()) {
            throw new InputException("Missing wallet cash amount");
        }

        try {
            walletCashAmount = Integer.parseInt(walletCashReader.next());
        }
        catch (NumberFormatException e) {
            throw new InputException("Invalid wallet cash amount: " + line);
        }
        if (walletCashAmount < 0) {
            throw new InputException("Wallet cash amount must be non-negative: " + walletCashAmount);
        }
        return walletCashAmount;
    }

    private static Map<String, Integer> parseWalletStocks(String line, Map<String, Integer> allStocks) throws InputException {
        Scanner scanner = new Scanner(line);
        String stringToSkip = scanner.next();

        if (!scanner.hasNext()) {
            throw new InputException("Missing wallet stocks");
        }

        Map<String, Integer> stockAmounts = parseStocks(line.substring(stringToSkip.length()));
        // Check if all stocks in wallet are valid
        if (!stockAmounts.keySet().stream().allMatch(allStocks::containsKey)) {
            throw new InputException("Invalid stock in wallet: " + stockAmounts.keySet());
        }

        return stockAmounts;
    }
}
