package bd.com.dev.pocketcash.model;

public class Earning {
    private String earningId;
    private long time;
    private int coin;

    public Earning() {
    }

    public Earning(String earningId, long time, int coin) {
        this.earningId = earningId;
        this.time = time;
        this.coin = coin;
    }

    public String getEarningId() {
        return earningId;
    }

    public long getTime() {
        return time;
    }

    public int getCoin() {
        return coin;
    }
}
