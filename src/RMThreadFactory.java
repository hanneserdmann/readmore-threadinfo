import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RMThreadFactory {
    private RMThread thread;
    final SharedVariables sv = new SharedVariables();

    /**
     * Übergeben wird die URL des Threads.
     * Es werden die Seiten als HTML heruntergeladen, geparsed und in die Datenbank geschrieben. Zeitgleich
     * wird in Thread Objekt erstellt und zurückgegeben.
     *
     * @param threadUrl
     */
    public RMThreadFactory(String threadUrl, JProgressBar progressBar, JTextArea progressTextArea) {
        Integer threadId = this.extractIdFromUrl(threadUrl);
        Integer threadLastPage;
        Integer postCount = 0;
        String threadTitle;
        //RMPost[] threadPostsBuffer;
        RMPost[] threadPosts;


        threadUrl = threadUrl.replace("#plast", "");

        HtmlDownloader downloader = new HtmlDownloader(threadUrl.concat("&page=1"));
        Document firstPage = Jsoup.parse(downloader.getContent());

        threadTitle = this.extractTitleFromDocument(firstPage);

        sv.threadUrl = threadUrl;
        sv.threadLastPage = this.extractLastPageFromDocument(firstPage);;
        sv.threadPostsBuffer = new RMPost[sv.threadLastPage * 25];

        progressTextArea.append("Download: " + threadTitle + "\r\n");


        try{
            List threads = new ArrayList();
            for (int i = 0; i < 10; i++) {
                Thread t = new Thread(new DownloadThread(progressBar, progressTextArea));
                t.start();
                threads.add(t);
            }

            for (int i = 0; i < 10; i++){
                ((Thread) threads.get(i)).join();
            }
        }catch(Exception e){e.printStackTrace();}

        //
/*
        for (Integer i = 1; i <= threadLastPage; i++) {
            downloader = new HtmlDownloader(threadUrl + "&page=" + i);
            Document pageData = Jsoup.parse(downloader.getContent());

            sv.threadPostsBuffer = this.extractPostsFromElements(pageData.select("#c_content div.forum_post"), i, sv.threadPostsBuffer);
            progressBar.setValue(Math.round((float) i / threadLastPage * 98));
            progressTextArea.append("     Page " + i + " / " + threadLastPage + "\r\n");
            progressTextArea.setCaretPosition(progressTextArea.getDocument().getLength());
        }
*/
        // Zählen wie viele Posts wirklich vorhanden sind
        for (RMPost post : sv.threadPostsBuffer) {
            if (post != null) {
                postCount++;
            }
        }

        // Post Array auf wirkliche Größe anpassen
        threadPosts = new RMPost[postCount];
        for (Integer i = 0; i < postCount; i++) {
            threadPosts[i] = sv.threadPostsBuffer[i];
        }

        progressTextArea.append("\r\n");
        this.thread = new RMThread(threadId, threadTitle, threadPosts, threadUrl);
    }

    public RMThread getThread() {
        return this.thread;
    }

    private Integer extractIdFromUrl(String threadUrl) {
        Pattern pattern = Pattern.compile("/(\\d+)[^/]*$");
        Matcher matcher = pattern.matcher(threadUrl);
        matcher.find();
        return Integer.parseInt(matcher.group(1));
    }

    private String extractTitleFromDocument(Document firstPage) {
        Element title = firstPage.select("#c_content > h1").first();
        return title.text();
    }

    private Integer extractLastPageFromDocument(Document firstPage) {
        Integer pageCount = 1;
        Elements paginationElements = firstPage.select("#c_content > div.pagination > ul > li > a");
        String[] pages;
        Element lagePage;

        if (paginationElements.size() > 1) {
            pageCount = Integer.parseInt(paginationElements.eq(paginationElements.size() - 2).text());
        }

        return pageCount;
    }

    private RMPost[] extractPostsFromElements(Elements postsElements, Integer page, RMPost[] postsArray) {
        Integer counter = 0;
        RMPostFactory postFactory;
        page--;

        for (Element postDiv : postsElements) {
            postFactory = new RMPostFactory(postDiv);
            postsArray[(page * 25) + counter] = postFactory.getPost();
            counter++;
        }

        return postsArray;
    }

    class SharedVariables{
        public volatile int pagesComplete = 0;
        public volatile int pagesCompleteProgress = 0;
        public volatile int threadLastPage;
        public volatile String threadUrl;
        public volatile RMPost[] threadPostsBuffer;
    }

    class DownloadThread implements Runnable{
        private JProgressBar progressBar;
        private JTextArea progressTextArea;

        public DownloadThread(JProgressBar progressBar, JTextArea progressTextArea){
            this.progressBar = progressBar;
            this.progressTextArea = progressTextArea;
        }

        @Override public void run(){
            try{
                while(sv.pagesComplete < sv.threadLastPage){
                    int i = ++sv.pagesComplete;
                    int tries = 0;
                    boolean successfullTry = false;
                    Document pageData = null;

                    // Falls der DL der Seite fehlschlägt es bis zu 5x versuchen
                    while(!successfullTry && tries < 5){
                        try{
                            tries++;
                            pageData = Jsoup.connect(sv.threadUrl + "&page=" + i).timeout(3000).post();
                            successfullTry = true;
                        }
                        catch(SocketTimeoutException stex){}
                    }

                    sv.threadPostsBuffer = extractPostsFromElements(pageData.select("#c_content div.forum_post"), i, sv.threadPostsBuffer);

                    sv.pagesCompleteProgress++;

                    progressBar.setValue(Math.round((float) sv.pagesCompleteProgress / sv.threadLastPage * 98));
                    progressTextArea.append("     Page " + sv.pagesCompleteProgress + " / " + sv.threadLastPage + "\r\n");
                    progressTextArea.setCaretPosition(progressTextArea.getDocument().getLength());
                }
            } catch(Exception ex){ex.printStackTrace();}
        }
    }
}
