import java.util.GregorianCalendar;

public class RMPost {
    private Integer id;
    private RMUser author;
    private GregorianCalendar date;
    private Integer number;
    private String message = "";

    public RMPost(Integer id, RMUser author, Integer number, GregorianCalendar date, String message) {
        this.id = id;
        this.author = author;
        this.number = number;
        this.date = date;
        this.message = message;
    }

    public RMUser getAuthor() {
        return this.author;
    }

    public GregorianCalendar getDate() {
        return this.date;
    }

    public Integer getNumber() {
        return this.number;
    }

    public String getMessage() {
        return this.message;
    }

    public Integer getId(){
        return this.id;
    }
}