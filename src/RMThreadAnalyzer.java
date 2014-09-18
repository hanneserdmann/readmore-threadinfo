import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class RMThreadAnalyzer {

    private Connection dbh;
    private RMThread thread;

    public RMThreadAnalyzer(Connection dbh, RMThread thread) {
        this.dbh = dbh;
        this.thread = thread;
    }

    public void analyze(){
        try {
            HashMap<Integer, RMThreadAnalyzerUser> user = new HashMap<Integer, RMThreadAnalyzerUser>();
            Integer postCount = 0;
            String sqlStatement;
            Statement stmt = this.dbh.createStatement();
            PreparedStatement pstmt;
            this.dbh.setAutoCommit(false);

            // Alle Posts durchgehen
            for (RMPost post : this.thread.getPosts()) {
                RMThreadAnalyzerUser singleUser;
                Document singleMessage;
                Integer postLength;

                // User Existiert noch nicht
                if ((singleUser = user.get(post.getAuthor().getId())) == null) {
                    singleUser = new RMThreadAnalyzerUser();
                    singleUser.userObject = post.getAuthor();
                }

                singleMessage = Jsoup.parse(post.getMessage());

                // Quotes Entfernen
                for (Element element : singleMessage.select("div.forum_ed_quote")) {
                    element.remove();
                }

                postLength = singleMessage.text().length();

                singleUser.postCount++;
                singleUser.allChars += postLength;
                if (postLength > singleUser.maxChars) {
                    singleUser.maxChars = postLength;
                }

                // Speichern
                user.put(post.getAuthor().getId(), singleUser);
                postCount++;
            }

            // Alle User durchgehen und die % sowie avg ausrechnen
            for (Map.Entry<Integer, RMThreadAnalyzerUser> entry : user.entrySet()) {
                RMThreadAnalyzerUser singleUser = entry.getValue();
                singleUser.avgChars = (double) singleUser.allChars / (double) singleUser.postCount;
                singleUser.percent = 100 / (double) postCount * (double) singleUser.postCount;
            }

            // Tabelle für die Auswertung leeren
            sqlStatement = " DELETE FROM statistic WHERE thread_id = " + this.thread.getId();
            stmt.executeUpdate(sqlStatement);

            // Daten eines Users einfügen
            for (Map.Entry<Integer, RMThreadAnalyzerUser> entry : user.entrySet()) {
                RMThreadAnalyzerUser singleUser = entry.getValue();

                sqlStatement = " INSERT INTO statistic (thread_id, user_id, postCount, maxChars, avgChars, allChars, percent) values (?, ?, ?, ?, ?, ?, ?)";
                pstmt = this.dbh.prepareStatement(sqlStatement);
                pstmt.setInt(1, this.thread.getId());
                pstmt.setInt(2, singleUser.userObject.getId());
                pstmt.setInt(3, singleUser.postCount);
                pstmt.setInt(4, singleUser.maxChars);
                pstmt.setDouble(5, singleUser.avgChars);
                pstmt.setInt(6, singleUser.allChars);
                pstmt.setDouble(7, singleUser.percent);
                pstmt.executeUpdate();
            }

            this.dbh.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private class RMThreadAnalyzerUser {
        public RMUser userObject = null;
        public Integer postCount = 0;
        public Integer allChars = 0;
        public Integer maxChars = 0;
        public Double avgChars = 0.0;
        public Double percent = 0.0;
    }
}

