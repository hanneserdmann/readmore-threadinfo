import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import org.sqlite.JDBC;

/**
 * Created by hanne_000 on 24.08.2014.
 */
public class ReadmoreThreadinfo extends JFrame {
    private JTabbedPane rootTabPanel;
    private JComboBox viewThreadComboBox;
    private JTextField einlesenField;
    private JButton einlesenButton;
    private JButton updateButton;
    private JComboBox updateComboBox;
    private JPanel rootJPanel;
    private JProgressBar progressBar;
    private JTextArea progressTextArea;
    private JButton viewThreadExportierenButton;
    private JButton viewThreadAnzeigenButton;
    private JEditorPane viewThreadEditorPane;
    private JTextField progressTextField;
    private JFileChooser fileChooser = new JFileChooser();

    private Connection dbh;
    private RMThreadDatabaseHandler threadDatabaseHandler;

    public static void main(String[] args) {
        JFrame frame = new JFrame("ReadmoreThreadinfo");
        frame.setContentPane(new ReadmoreThreadinfo().rootJPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public ReadmoreThreadinfo() {
        super("Readmore Threadinfo");

        try{

            //Class.forName("org.sqlite.JDBC");

            dbh = DriverManager.getConnection("jdbc:sqlite:data/readmore-threadinfo.db");
            threadDatabaseHandler = new RMThreadDatabaseHandler(dbh);

            updateUpdateComboBoxItems();

            einlesenButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        setButtonsEnabled(false);
                        String threadUrl = einlesenField.getText();

                        if (einlesenField.getText().equals("")){
                            throw new Exception();
                        }

                        Thread readThread = new Thread(new ReadThread(dbh, threadUrl));
                        readThread.start();
                        einlesenField.setText("");
                    }
                    catch(Exception ex){
                        JOptionPane.showMessageDialog(null, "Falsche Eingabe! Bitte gebe die komplette URL des Threads ein!\n\r(STRG-C + STRG-V funktioniert!");
                        setButtonsEnabled(true);
                    }
                }
            });

            updateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        setButtonsEnabled(false);
                        RMThread thread = (RMThread)updateComboBox.getSelectedItem();
                        Thread readThread = new Thread(new ReadThread(dbh, thread.getUrl()));
                        readThread.start();
                        einlesenField.setText("");
                    }
                    catch(Exception ex){
                        JOptionPane.showMessageDialog(null, "Der Thread konnte nicht geupdatet werden!");
                        setButtonsEnabled(true);
                    }
                }
            });

            viewThreadAnzeigenButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        openThreadinfoInBrowser();
                    }
                    catch(Exception ex){
                        JOptionPane.showMessageDialog(null, "Der Thread konnte nicht analysiert werden!");
                        setButtonsEnabled(true);
                    }
                }
            });

            viewThreadExportierenButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        exportThreadinfo();
                    }
                    catch(Exception ex){
                        JOptionPane.showMessageDialog(null, "Der Thread konnte nicht analysiert werden!");
                    }
                }
            });
        }
        catch(Exception ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    private void updateUpdateComboBoxItems(){
        updateComboBox.removeAllItems();
        viewThreadComboBox.removeAllItems();

        RMThread[] threads = threadDatabaseHandler.getAllThreadsTextOnly();
        if (threads != null){
            for (RMThread thread : threads){
                updateComboBox.addItem(thread);
                viewThreadComboBox.addItem(thread);
            }
        }
    }

    private void setButtonsEnabled(Boolean b){
        updateButton.setEnabled(b);
        einlesenButton.setEnabled(b);
        viewThreadAnzeigenButton.setEnabled(b);
        viewThreadExportierenButton.setEnabled(b);
    }

    private void resetProgress(){
        progressTextArea.setText("");
        progressBar.setValue(0);
    }

    private void openThreadinfoInBrowser() throws Exception{
        RMThread thread = (RMThread) viewThreadComboBox.getSelectedItem();
        File tempFile = File.createTempFile("tmpthreadinfo", ".html", new File(System.getProperty("java.io.tmpdir")));
        Desktop desktop = Desktop.getDesktop();

        writeThreadinfoHtml(thread.getId(), tempFile);
        desktop.browse(tempFile.toURI());
    }

    private void writeThreadinfoHtml(int threadId, File file) throws Exception{
        HtmlGenerator htmlGenerator = new HtmlGenerator(dbh, threadId);
        htmlGenerator.buildHtml();
        htmlGenerator.writeFile(file, htmlGenerator.getHtmlData());
    }

    private void exportThreadinfo() throws Exception{
        RMThread thread = (RMThread) viewThreadComboBox.getSelectedItem();

        fileChooser.setSelectedFile(new File("threadinfo-" + thread.getId() + ".html"));
        int returnVal = fileChooser.showSaveDialog(rootTabPanel);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            HtmlGenerator htmlGenerator = new HtmlGenerator(dbh, thread.getId());
            htmlGenerator.buildHtml();
            htmlGenerator.writeFile(file, htmlGenerator.getHtmlData());
        }
    };

    private class ReadThread implements Runnable{
        private Connection dbh;
        private String threadUrl;

        public ReadThread(Connection dbh, String threadUrl){
            this.dbh = dbh;
            this.threadUrl = threadUrl;
        }

        @Override public void run(){
            try{
                RMThread thread;
                RMThreadAnalyzer threadAnalyzer;
                RMThreadFactory threadFactory;

                resetProgress();

                // Daten auslesen
                threadFactory = new RMThreadFactory(threadUrl, progressBar, progressTextArea);
                thread = threadFactory.getThread();
                threadDatabaseHandler.writeToDatabase(thread);

                // Daten analysieren
                progressTextArea.append("Analyze: " + thread.getTitle()  + "\r\n");
                progressTextArea.setCaretPosition(progressTextArea.getDocument().getLength());

                threadAnalyzer = new RMThreadAnalyzer(dbh, thread);
                threadAnalyzer.analyze();

                progressBar.setValue(100);
                progressTextArea.append("     Analyze complete" + "\r\n\r\n");
                progressTextArea.setCaretPosition(progressTextArea.getDocument().getLength());
            } catch(Exception ex){ex.printStackTrace();}

            setButtonsEnabled(true);
            updateUpdateComboBoxItems();
        }
    }

















    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootTabPanel = new JTabbedPane();
        panel1.add(rootTabPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootTabPanel.addTab("Readmore Threadinfo", panel2);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootTabPanel.addTab("View", panel3);
    }
}
