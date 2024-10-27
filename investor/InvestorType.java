package investor;

public enum InvestorType {
    RANDOM('R'),
    SMA('S');
    private final char uChar;
    public char getUniqueChar() { return uChar; }

    InvestorType(char uChar) {
        this.uChar = uChar;
    }
}
