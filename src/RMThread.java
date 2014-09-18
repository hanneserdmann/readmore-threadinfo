public class RMThread {
    private String title = "";
    private String url = "";
    private Integer id;
    private RMUser creator;
    private RMPost[] posts;

    public RMThread(Integer id, String title, RMPost[] posts, String url) {
        this.id = id;
        this.title = title;
        this.posts = posts;
        this.creator = posts[0].getAuthor();
        this.url = url;
    }

    public RMThread(int id, String title, String url){
        this.id = id;
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return this.title;
    }

    public RMUser getCreator() {
        return this.creator;
    }

    public RMPost[] getPosts() {
        return this.posts;
    }

    public Integer getId() {
        return this.id;
    }

    public String getUrl(){
        return this.url;
    }

    @Override public String toString(){

        return getId() + " | " + getTitle();
    }
}
