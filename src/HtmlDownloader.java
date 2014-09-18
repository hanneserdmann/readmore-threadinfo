import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class HtmlDownloader {
    private String websiteUrl = "";
    private String content = "";

    public HtmlDownloader(String websiteUrl) {

        URL urlObject;
        BufferedReader br;
        InputStream is;

        String buffer = "";

        this.websiteUrl = websiteUrl;

        try {
            urlObject = new URL(this.getUrl());
            is = urlObject.openStream();
            br = new BufferedReader(new InputStreamReader(is));

            while ((buffer = br.readLine()) != null) {
                this.content = this.content.concat(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return this.websiteUrl;
    }

    public String getContent() {
        return this.content;
    }
}