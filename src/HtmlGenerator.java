import java.io.*;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

public class HtmlGenerator {
    private Connection dbh;
    private Integer threadId;
    private String htmlData = "";

    public HtmlGenerator(Connection dbh, Integer threadId){
        this.dbh = dbh;
        this.threadId = threadId;
    }

    public void buildHtml(){
        String returnHtml = "";
        String template = "";

        try{
            Statement stmt = dbh.createStatement();
            String sqlQuery;
            ResultSet res;
            DecimalFormat f = new DecimalFormat("#0.000");

            sqlQuery  = " SELECT t1.*, t2.name AS userName" +
                        " FROM statistic AS t1" +
                        " INNER JOIN user AS t2 ON (t1.user_id = t2.id)" +
                        " WHERE t1.thread_id = " + this.threadId +
                        " ORDER BY t1.percent DESC, t1.allChars DESC, t1.user_id DESC";
            res = stmt.executeQuery(sqlQuery);

            while(res.next()){
                returnHtml+= "\t\t"   + "<tr>"  + "\n";
                returnHtml+= "\t\t\t" + "<td>"  + res.getString("userName") + "</td>" + "\n";
                returnHtml+= "\t\t\t" + "<td>"  + res.getInt("user_id") + "</td>" + "\n";
                returnHtml+= "\t\t\t" + "<td>"  + res.getInt("postCount") + "</td>" + "\n";
                returnHtml+= "\t\t\t" + "<td>"  + res.getInt("allChars") + "</td>" + "\n";
                returnHtml+= "\t\t\t" + "<td>"  + res.getInt("maxChars") + "</td>" + "\n";
                returnHtml+= "\t\t\t" + "<td>"  + f.format(res.getDouble("avgChars")) + "</td>" + "\n";
                returnHtml+= "\t\t\t" + "<td>"  + f.format(res.getDouble("percent")) + "</td>" + "\n";
                returnHtml+= "\t\t"   + "</tr>" + "\n";
            }

            template = this.readFile("data/template.html");
            template = template.replace("{$POSTS}", returnHtml);

            // Threadtitel auslesen
            sqlQuery =  " SELECT title FROM thread WHERE id = " + this.threadId +
                        " LIMIT 1";
            res = stmt.executeQuery(sqlQuery);

            while(res.next()){
                template = template.replace("{$TITLE}", res.getString("title"));
            }

            this.htmlData = template;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader( new FileReader(file));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        return stringBuilder.toString();
    }

    public void writeFile(String fileName) throws IOException{
        byte dataToWrite[] = this.htmlData.getBytes(Charset.forName("UTF-8"));
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(dataToWrite);
        out.close();
    }

    public void writeFile(File file, String data) throws IOException{
        byte dataToWrite[] = data.getBytes(Charset.forName("UTF-8"));
        FileOutputStream out = new FileOutputStream(file);
        out.write(dataToWrite);
        out.close();
    }

    public String getHtmlData(){
        return htmlData;
    }
}
