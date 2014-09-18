import java.util.GregorianCalendar;

public class RMUser {
    private Integer id = 0;
    private Integer postCount = 0;
    private String name = "";
    private GregorianCalendar regDate;

    public RMUser(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public RMUser(Integer id, String name, Integer postCount, GregorianCalendar regDate) {
        this.id = id;
        this.name = name;
        this.postCount = postCount;
        this.setRegDate(regDate);
    }

    public RMUser(Integer id, String name, Integer postCount, String germanDate) {
        this.id = id;
        this.name = name;
        this.postCount = postCount;
        this.setRegDateFromGermanString(germanDate);
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Integer getPostCount() {
        return this.postCount;
    }

    public GregorianCalendar getRegDate() {
        return this.regDate;
    }

    public void setRegDate(GregorianCalendar regDate) {
        this.regDate = regDate;
    }

    public void setpostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public void setRegDateFromGermanString(String germanDate) {
        String[] dateParts = germanDate.split("\\.");
        this.regDate = new GregorianCalendar(Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[0]));
    }
}