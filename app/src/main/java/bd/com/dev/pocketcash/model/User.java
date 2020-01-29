package bd.com.dev.pocketcash.model;

public class User {
    private String userId;
    private String name;
    private String mNumber;
    private String email;
    private String photoUrl;
    private String referralCode;
    private String userStatus;
    private long createdDate;

    public User() {
    }

    public User(String userId, String name, String mNumber, String email, String photoUrl, String referralCode, String userStatus, long createdDate) {
        this.userId = userId;
        this.name = name;
        this.mNumber = mNumber;
        this.email = email;
        this.photoUrl = photoUrl;
        this.referralCode = referralCode;
        this.userStatus = userStatus;
        this.createdDate = createdDate;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getmNumber() {
        return mNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public long getCreatedDate() {
        return createdDate;
    }
}
