package bd.com.dev.pocketcash.model;

public class Withdraw {
    private String paymentId;
    private String paymentType;
    private String mNumber;
    private String paymentStatus;
    private long requestDate;
    private int requestCoin;
    private int payableMoney;
    private int coinRate;

    public Withdraw() {
    }

    public Withdraw(String paymentId, String paymentType, String mNumber, String paymentStatus, long requestDate, int requestCoin, int payableMoney, int coinRate) {
        this.paymentId = paymentId;
        this.paymentType = paymentType;
        this.mNumber = mNumber;
        this.paymentStatus = paymentStatus;
        this.requestDate = requestDate;
        this.requestCoin = requestCoin;
        this.payableMoney = payableMoney;
        this.coinRate = coinRate;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getmNumber() {
        return mNumber;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public long getRequestDate() {
        return requestDate;
    }

    public int getRequestCoin() {
        return requestCoin;
    }

    public int getPayableMoney() {
        return payableMoney;
    }

    public int getCoinRate() {
        return coinRate;
    }
}
