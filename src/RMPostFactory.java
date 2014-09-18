import org.jsoup.nodes.Element;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RMPostFactory {
    private RMPost post;

    /**
     * Übergeben wird ein Div Element (aus jsoup) das den Post beinhaltet.
     *
     * @param postElement
     */
    public RMPostFactory(Element postElement) {
        Integer postId = Integer.parseInt(postElement.select(".head > span > a").first().attr("href").replaceFirst("^.*/(\\d+)$", "$1"));
        Integer postNumber = Integer.parseInt(postElement.select(".head > span > a").first().text().replace("#", ""));
        GregorianCalendar postDate = this.extractDateTimeFromString(postElement.select(".head").first().text());
        String postMessage = postElement.select(".post div[id^=post_]").first().html();

        RMUser postAuthor = extractUserFromElement(postElement);
        this.post = new RMPost(postId, postAuthor, postNumber, postDate, postMessage);
    }

    public RMPost getPost() {
        return this.post;
    }

    private GregorianCalendar extractDateTimeFromString(String rawDate) {
        String[] rawDateBuffer = rawDate.replaceFirst("^(#[\\d]+?)\\s*([\\d]{2}\\.|heute|gestern)", "$2").split(",");
        String[] timeBuffer = rawDateBuffer[1].replace("Uhr", "").trim().split(":");
        String[] dateBuffer = rawDateBuffer[0].trim().split("\\.");

        GregorianCalendar date = new GregorianCalendar();

        date.set(Calendar.HOUR, Integer.parseInt(timeBuffer[0]));
        date.set(Calendar.MINUTE, Integer.parseInt(timeBuffer[1]));

        if (rawDateBuffer[0].equals("gestern")) {
            date.add(Calendar.DATE, -1);
        } else if (!rawDateBuffer[0].equals("heute")) {
            date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateBuffer[0]));
            date.set(Calendar.MONTH, Integer.parseInt(dateBuffer[1]) - 1);
            date.set(Calendar.YEAR, Integer.parseInt(dateBuffer[2]));
        }

        return date;
    }

    private RMUser extractUserFromElement(Element postElement) {
        String postAuthorName;
        String postAuthorGermanDate;
        Integer postAuthorId;
        Integer postAuthorPostcount;

        Pattern pattern;
        Matcher matcher;


        // Username
        postAuthorName = postElement.select(".user > a").first().attr("title");

        // UserId
        pattern = Pattern.compile("(\\d+)[\\w\\s\\.,-]*$");
        matcher = pattern.matcher(postElement.select(".user > a").first().attr("href"));
        matcher.find();

        postAuthorId = Integer.parseInt(matcher.group(1));

        // Postcount
        pattern = Pattern.compile("^(\\d+)[\\w\\sä.:]+?");
        matcher = pattern.matcher(postElement.select(".stats").first().text());
        matcher.find();
        postAuthorPostcount = Integer.parseInt(matcher.group(1));

        // RegDate
        pattern = Pattern.compile("([\\d]{2}\\.[\\d]{2}\\.[\\d]{4})$");
        matcher = pattern.matcher(postElement.select(".stats").first().text());

        // Bei gelöschten Accounts wird das Reg. Datum nicht angezeigt, deshalb der Fallback
        if (matcher.find()) {
            postAuthorGermanDate = matcher.group(1);
        } else {
            postAuthorGermanDate = "00.00.0000";
        }

        return new RMUser(postAuthorId, postAuthorName, postAuthorPostcount, postAuthorGermanDate);
    }
}
