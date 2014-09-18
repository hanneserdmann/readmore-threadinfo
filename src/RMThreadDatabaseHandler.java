import java.sql.*;

public class RMThreadDatabaseHandler {
    private Connection dbh;

    public RMThreadDatabaseHandler(Connection dbh) {
        this.dbh = dbh;
    }

    public void writeToDatabase(RMThread thread) {
        try {
            String sqlStatement = "";
            Statement stmt = dbh.createStatement();
            PreparedStatement pstmt = null;
            dbh.setAutoCommit(false);

            // Clear Table Post
            sqlStatement = " DELETE FROM post WHERE thread_id = " + thread.getId();
            stmt.executeUpdate(sqlStatement);

            // Clear Table Thread
            sqlStatement = " DELETE FROM thread WHERE id = " + thread.getId();
            stmt.executeUpdate(sqlStatement);

            // Tabelle für die Auswertung leeren
            sqlStatement = " DELETE FROM statistic WHERE thread_id = " + thread.getId();
            stmt.executeUpdate(sqlStatement);

            // Insert into Thread Table
            sqlStatement = " INSERT INTO thread (id, title, url) values (?, ?, ?)";
            pstmt = dbh.prepareStatement(sqlStatement);
            pstmt.setInt(1, thread.getId());
            pstmt.setString(2, thread.getTitle());
            pstmt.setString(3, thread.getUrl());
            pstmt.executeUpdate();

            // Insert Posts and User
            for (RMPost post : thread.getPosts()) {

                // Post einfügen
                sqlStatement = " INSERT INTO post (thread_id, number, user_id, date, message, id) values (?, ?, ?, ?, ?, ?)";
                pstmt = dbh.prepareStatement(sqlStatement);
                pstmt.setInt(1, thread.getId());
                pstmt.setInt(2, post.getNumber());
                pstmt.setInt(3, post.getAuthor().getId());
                pstmt.setString(4, new Timestamp(post.getDate().getTimeInMillis()).toString());
                pstmt.setString(5, post.getMessage());
                pstmt.setInt(6, post.getId());
                pstmt.executeUpdate();

                // User einfügen, zuerst alten Datensatz entfernen
                sqlStatement = " DELETE FROM user WHERE id = " + post.getAuthor().getId();
                stmt.executeUpdate(sqlStatement);

                // Daten einfügen
                sqlStatement = " INSERT INTO user (id, name, postCount, regDate) values (?, ?, ?, ?)";
                pstmt = dbh.prepareStatement(sqlStatement);
                pstmt.setInt(1, post.getAuthor().getId());
                pstmt.setString(2, post.getAuthor().getName());
                pstmt.setInt(3, post.getAuthor().getPostCount());
                pstmt.setString(4, new Timestamp(post.getAuthor().getRegDate().getTimeInMillis()).toString());
                pstmt.executeUpdate();
            }

            dbh.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RMThread[] getAllThreadsTextOnly(){
        RMThread[] threads = null;

        try{
            int count,
                i = 0;
            String sqlQuery = "";
            Statement stmt = dbh.createStatement();
            ResultSet res;// = stmt.executeQuery(sqlQuery);

            // Anzahl der Threads in der DB ermitteln
            sqlQuery =  "SELECT COUNT(id) AS rowcount FROM thread";
            res = stmt.executeQuery(sqlQuery);
            count = res.getInt("rowcount");

            if (count > 0){
                sqlQuery = "SELECT id, title, url FROM thread ORDER BY id ASC";
                res = stmt.executeQuery(sqlQuery);

                threads = new RMThread[count];

                while(res.next()){
                    threads[i++] = new RMThread(res.getInt("id"), res.getString("title"), res.getString("url"));
                }
            }
        }catch(Exception ex){ex.printStackTrace();};


        return threads;
    }
}
