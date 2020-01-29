package bd.com.dev.pocketcash.model;

public class ReferUser {
    private String userId;
    private String name;
    private String referPaymentStatus;
    private long referDate;

    public ReferUser() {
    }

    public ReferUser(String userId, String name, String referPaymentStatus, long referDate) {
        this.userId = userId;
        this.name = name;
        this.referPaymentStatus = referPaymentStatus;
        this.referDate = referDate;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }


    public String getReferPaymentStatus() {
        return referPaymentStatus;
    }

    public long getReferDate() {
        return referDate;
    }
}
