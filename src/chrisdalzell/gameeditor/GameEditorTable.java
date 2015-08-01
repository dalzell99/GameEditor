package chrisdalzell.gameeditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GameEditorTable extends JFrame {

    JTable table;
    MyTableModel tableModel;
    TableRowSorter<TableModel> sorter;
    JScrollPane scrollPane;
    JMenuBar menuBar;
    JButton saveButton;
    JButton refreshButton;
    JButton addButton;
    JButton deleteButton;
    JButton pdfDrawButton;
    JButton pdfResultsButton;
    Label progress;
    JDatePickerImpl datePicker1;
    JDatePickerImpl datePicker2;

    public GameEditorTable() {
        // Set the frame characteristics
        setTitle("Game Editor Table");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight() - 40;
        setSize(width, height);

        JPanel statusBar = new JPanel();

        statusBar.add(new Label("Start of week:"));
        UtilDateModel model1 = new UtilDateModel();
        model1.setDate(2015, 3, 21);
        Properties p1 = new Properties();
        p1.put("text.today", "Today");
        p1.put("text.month", "Month");
        p1.put("text.year", "Year");
        JDatePanelImpl datePanel1 = new JDatePanelImpl(model1, p1);
        datePicker1 = new JDatePickerImpl(datePanel1, new DateLabelFormatter());
        datePicker1.addActionListener(e -> {
            UtilDateModel model = (UtilDateModel) datePicker1.getModel();
            Date d1 = new Date(new GregorianCalendar(model.getYear(), model.getMonth(), model.getDay()).getTimeInMillis());
            model = (UtilDateModel) datePicker2.getModel();
            Date d2 = new Date(new GregorianCalendar(model.getYear(), model.getMonth(), model.getDay()).getTimeInMillis());

            RowFilter<TableModel, Integer> low = RowFilter.dateFilter(RowFilter.ComparisonType.AFTER, d1, 0);
            RowFilter<TableModel, Integer> high = RowFilter.dateFilter(RowFilter.ComparisonType.BEFORE, d2, 0);

            List<RowFilter<TableModel, Integer>> filters = Arrays.asList(low, high);
            RowFilter<TableModel, Integer> filter = RowFilter.andFilter(filters);
            sorter.setRowFilter(filter);
        });
        statusBar.add(datePicker1);

        statusBar.add(new Label("End of week:"));
        UtilDateModel model2 = new UtilDateModel();
        model2.setDate(2015, 3, 30);
        Properties p2 = new Properties();
        p2.put("text.today", "Today");
        p2.put("text.month", "Month");
        p2.put("text.year", "Year");
        JDatePanelImpl datePanel2 = new JDatePanelImpl(model2, p2);
        datePicker2 = new JDatePickerImpl(datePanel2, new DateLabelFormatter());
        datePicker2.addActionListener(e -> {
            UtilDateModel model = (UtilDateModel) datePicker1.getModel();
            Date d1 = new Date(new GregorianCalendar(model.getYear(), model.getMonth(), model.getDay()).getTimeInMillis());
            model = (UtilDateModel) datePicker2.getModel();
            Date d2 = new Date(new GregorianCalendar(model.getYear(), model.getMonth(), model.getDay()).getTimeInMillis());

            RowFilter<TableModel, Integer> low = RowFilter.dateFilter(RowFilter.ComparisonType.AFTER, d1, 0);
            RowFilter<TableModel, Integer> high = RowFilter.dateFilter(RowFilter.ComparisonType.BEFORE, d2, 0);

            List<RowFilter<TableModel, Integer>> filters = Arrays.asList(low, high);
            RowFilter<TableModel, Integer> filter = RowFilter.andFilter(filters);
            sorter.setRowFilter(filter);
        });
        statusBar.add(datePicker2);

        pdfDrawButton = new JButton("Create PDF Draw");
        pdfDrawButton.addActionListener(e -> createPDFDraw(tableModel.getGames()));
        statusBar.add(pdfDrawButton);

        pdfResultsButton = new JButton("Create PDF Results");
        pdfResultsButton.addActionListener(e -> createPDFResults(tableModel.getGames()));
        statusBar.add(pdfResultsButton);

        add(statusBar, BorderLayout.PAGE_START);

        tableModel = new MyTableModel();
        table = new JTable(tableModel);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setFillsViewportHeight(true);
        sorter = new TableRowSorter<>(tableModel);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        table.setRowSorter(sorter);

        scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        menuBar = new JMenuBar();
        saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            if (tableModel.getRowsChanged().size() > 0) {
                ArrayList<Integer> changes = new ArrayList<>();
                for (Integer i : tableModel.getRowsChanged()) {
                    changes.add(i);
                }
                saveChanges(changes, tableModel.getGames());
                tableModel.getRowsChanged().clear();
            } else {
                JOptionPane.showMessageDialog(null, "There are no changes to save");
            }
        });
        menuBar.add(saveButton);

        refreshButton = new JButton("Refresh Table");
        refreshButton.addActionListener(e -> tableModel.refreshGames());
        menuBar.add(refreshButton);

        addButton = new JButton("Add Game To Database");
        addButton.addActionListener(e -> new AddGameToDatabase().setVisible(true));
        menuBar.add(addButton);

        deleteButton = new JButton("Delete Selected Game");
        deleteButton.addActionListener(e -> {
            if (tableModel.getRowsChanged().size() > 0) {
                int choice = JOptionPane.showConfirmDialog(null, "Do you want to save changes?");
                if (choice == JOptionPane.YES_OPTION) {
                    ArrayList<Integer> changes = new ArrayList<>();
                    for (Integer i : tableModel.getRowsChanged()) {
                        changes.add(i);
                    }
                    saveChanges(changes, tableModel.getGames());
                    tableModel.getRowsChanged().clear();
                }
            }
            if (table.getSelectedRowCount() > 0) {
                int choice = 0;
                if (table.getSelectedRowCount() == 1) {
                    choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this game?");
                } else {
                    choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete these games?");
                }
                if (choice == JOptionPane.YES_OPTION) {
                    for (int row : table.getSelectedRows()) {
                        tableModel.deleteGame(table.convertRowIndexToModel(row));
                    }
                    tableModel.refreshGames();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a game first");
            }
        });
        menuBar.add(deleteButton);

        progress = new Label("Changes Made: 0");
        menuBar.add(progress);

        setJMenuBar(menuBar);

        // Add listener to window close button
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // Check if there are any unsaved changes
                if (tableModel.getRowsChanged().size() > 0) {
                    try {
                        int choice = JOptionPane.showOptionDialog(
                                null,
                                "Do you want to save the changes?",
                                "Unsaved Changes",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                null,
                                null);
                        if (choice == JOptionPane.YES_OPTION) {
                            // If user wants to save changes, save them then exit
                            ArrayList<Integer> changes = new ArrayList<>();
                            for (Integer i : tableModel.getRowsChanged()) {
                                changes.add(i);
                            }
                            saveChanges(changes, tableModel.getGames());
                            System.exit(0);
                        } else if (choice == JOptionPane.CANCEL_OPTION) {
                            // If cancel selected, do nothing
                        } else if (choice == JOptionPane.NO_OPTION) {
                            // If the User doesn't want to save changes, exit
                            System.exit(0);
                        }
                    } catch (HeadlessException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    // If there are no unsaved changes, exit
                    System.exit(0);
                }
            }
        });
    }

    /**
     * Upload rows with changes to server
     */
    private void saveChanges(ArrayList<Integer> rowsChanged, ArrayList<Game> games) {
        for (int i = 0; i < rowsChanged.size(); i += 1) {
            progress.setText("Saved: " + i + "/" + rowsChanged.size());
            Integer row = rowsChanged.get(i);
            Game g = games.get(row);
            HttpClient httpclient = HttpClientBuilder.create().build();
            HttpPost httppost = new HttpPost("http://www.possumpam.com/rugby-scoring-app-scripts/update_game_info.php");
            try {
                // Store game info in List and add to httppost
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("gameID", String.valueOf(g.getGameID())));
                nameValuePairs.add(new BasicNameValuePair("location", g.getLocation()));
                nameValuePairs.add(new BasicNameValuePair("time", g.getTime()));
                nameValuePairs.add(new BasicNameValuePair("ref", g.getRef()));
                nameValuePairs.add(new BasicNameValuePair("assRef1", g.getAssRef1()));
                nameValuePairs.add(new BasicNameValuePair("assRef2", g.getAssRef2()));
                nameValuePairs.add(new BasicNameValuePair("homeScore", String.valueOf(g.getHomeTeamScore())));
                nameValuePairs.add(new BasicNameValuePair("awayScore", String.valueOf(g.getAwayTeamScore())));
                nameValuePairs.add(new BasicNameValuePair("changed", String.valueOf(g.getChanged())));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute httppost and retrieve response
                HttpResponse response = httpclient.execute(httppost);

                // Convert response to String
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                is.close();
                if (sb.toString().equals("failure")) { System.out.println(sb); }
            } catch (Exception e) {
                // If there's a problem serverside, display error
                e.printStackTrace();
            }
        }
        progress.setText("Changes Made: 0");
    }

    /**
     * Custom table model
     */
    class MyTableModel extends AbstractTableModel {
        private String[] columnNames = {"Date", "Division", "Home Team", "Home Score", "Away Team", "Away Score", "Time",
                "Location", "Ref", "Assistant Ref 1", "Assistant Ref 2"};
        private ArrayList<Game> games = new ArrayList<>();
        HashSet<Integer> rowsChanged = new HashSet<>();
        private List<String> divisions = Arrays.asList("Div 1", "Women", "Div 2", "Div 3", "Colts",
                "U18", "U16", "U14.5", "U13", "U11.5", "U10", "U8.5", "U7");

        public MyTableModel() {
            games = getAllGames();
        }

        public void refreshGames() {
            progress.setText("Rebuilding table");
            games = getAllGames();
            fireTableDataChanged();
            rowsChanged.clear();
            progress.setText("Changes Made: 0");
        }

        public void deleteGame(int row) {
            progress.setText("Deleting game from database");
            try {
                HttpClient httpclient = HttpClientBuilder.create().build();
                HttpPost httppost = new HttpPost("http://www.possumpam.com/rugby-scoring-app-scripts/delete_game.php");

                List<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("gameID", String.valueOf(games.get(row).getGameID())));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Return all games between start and end dates
                HttpResponse response = httpclient.execute(httppost);

                // Retrieve json data to be processed
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                is.close();
                if (sb.toString().equals("failure")) {
                    System.out.println(sb);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<Game> getAllGames() {
            ArrayList<Game> games = new ArrayList<>();
            String result = "";

            try {
                HttpClient httpclient = HttpClientBuilder.create().build();
                HttpPost httppost = new HttpPost("http://www.possumpam.com/rugby-scoring-app-scripts/get_all_games.php");

                // Return all games between start and end dates
                HttpResponse response = httpclient.execute(httppost);

                // Retrieve json data to be processed
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                is.close();
                result = sb.toString();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "An error has occurred while retrieving the game list.\n" +
                        "Please check your internet connection and try again later.");
                e.printStackTrace();
            }

            try {
                // Check if any games were retrieved. This prevents most JSONExceptions.
                if (!result.equals("")) {
                    JSONParser parser = new JSONParser();
                    try {
                        Object obj = parser.parse(result);
                        JSONArray array = (JSONArray) obj;
                        for (Object anArray : array) {
                            JSONObject g = (JSONObject) anArray;
                            games.add(new Game(
                                    Long.parseLong(String.valueOf(g.get("GameID"))),
                                    String.valueOf(g.get("homeTeamName")),
                                    Integer.parseInt(String.valueOf(g.get("homeTeamScore"))),
                                    String.valueOf(g.get("awayTeamName")),
                                    Integer.parseInt(String.valueOf(g.get("awayTeamScore"))),
                                    String.valueOf(g.get("ref")),
                                    String.valueOf(g.get("assRef1")),
                                    String.valueOf(g.get("assRef2")),
                                    String.valueOf(g.get("location")),
                                    Integer.parseInt(String.valueOf(g.get("minutesPlayed"))),
                                    String.valueOf(g.get("time")),
                                    new ArrayList<>(),
                                    String.valueOf(g.get("changed"))));
                        }
                    } catch (ParseException pe) {
                        System.out.println("position: " + pe.getPosition());
                        pe.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return games;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return games.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public HashSet<Integer> getRowsChanged() {
            return rowsChanged;
        }

        public ArrayList<Game> getGames() {
            return games;
        }

        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return games.get(row).getDate();
                case 1:
                    int divID = Integer.parseInt(String.valueOf(games.get(row).getGameID()).substring(12));
                    return divisions.get(divID);
                case 2:
                    return games.get(row).getHomeTeamName();
                case 3:
                    return games.get(row).getHomeTeamScore();
                case 4:
                    return games.get(row).getAwayTeamName();
                case 5:
                    return games.get(row).getAwayTeamScore();
                case 6:
                    return games.get(row).getTime();
                case 7:
                    return games.get(row).getLocation();
                case 8:
                    return games.get(row).getRef();
                case 9:
                    return games.get(row).getAssRef1();
                case 10:
                    return games.get(row).getAssRef2();
                default:
                    return "";
            }
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return col >= 5 || col == 3;
        }

        public void setValueAt(Object value, int row, int col) {
            switch (col) {
                case 3:
                    games.get(row).setHomeTeamScore(Integer.parseInt(String.valueOf(value)));
                    break;
                case 5:
                    games.get(row).setAwayTeamScore(Integer.parseInt(String.valueOf(value)));
                    break;
                case 6:
                    games.get(row).setTime(String.valueOf(value));
                    break;
                case 7:
                    games.get(row).setLocation(String.valueOf(value));
                    break;
                case 8:
                    games.get(row).setRef(String.valueOf(value));
                    break;
                case 9:
                    games.get(row).setAssRef1(String.valueOf(value));
                    break;
                case 10:
                    games.get(row).setAssRef2(String.valueOf(value));
                    break;
            }
            games.get(row).setChanged("y");
            rowsChanged.add(row);
            progress.setText("Changes Made: " + rowsChanged.size());
            fireTableCellUpdated(row, col);
        }
    }

    /**
     * Create PDF of this weeks games
     */
    private void createPDFDraw(ArrayList<Game> games) {
        List<String> divisions = Arrays.asList("LUISETTI SEEDS DIVISION 1", "WOMENS - CUP", "ELLESMERE DIVISION 2",
                "ELLESMERE DIVISION 3", "ELLES/MID CANT COMBINED COLTS", "ELLES/MID CANT/NC COMBINED U18",
                "ELLESMERE/MID CANT NC U16", "ELLESMERE U14", "ELLESMERE U13", "ELLESMERE U11.5", "ELLESMERE U10",
                "ELLESMERE U8.5", "ELLESMERE U7");
        //String clubDays = JOptionPane.showInputDialog("Enter club days for this week");

        String outputFileName = "I:\\Draw.pdf";
        /*JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(GameEditorTable.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (fc.getSelectedFile().exists()) {
                int choice = JOptionPane.showConfirmDialog(fc, "Do you want to overwrite this file?");
                if (choice == JOptionPane.YES_OPTION) {
                    outputFileName = fc.getSelectedFile().getPath();
                }
            } else {
                outputFileName = fc.getSelectedFile().getPath();
            }
        }

        if (!outputFileName.endsWith(".pdf")) {
            outputFileName += ".pdf";
        }*/
        
        try {
            // Create a document and add a page to it
            PDDocument document = new PDDocument();
            PDPage page1 = new PDPage(PDPage.PAGE_SIZE_A4);
            PDRectangle rect = page1.getMediaBox(); // rect can be used to get the page width and height
            document.addPage(page1);

            // Create a new font object selecting one of the PDF base fonts
            PDFont fontPlain = PDType1Font.HELVETICA;
            PDFont fontBold = PDType1Font.HELVETICA_BOLD;
            // Start a new content stream which will "hold" the to be created content
            PDPageContentStream cos = new PDPageContentStream(document, page1);

            int line = 1;

            Font font = new Font("Helvetica", Font.BOLD, 12);
            FontMetrics metrics = getGraphics().getFontMetrics(font);

            // Define a text content stream using the selected font, move the cursor and draw some text
            cos.beginText();
            String titleString = "ELLESMERE RUGBY SUB UNION";
            cos.setFont(fontBold, 12);
            int adv = metrics.stringWidth(titleString);
            cos.moveTextPositionByAmount((rect.getWidth() / 2) - (adv / 2), rect.getHeight() - 16 * (++line));
            cos.drawString(titleString);
            cos.endText();

            cos.beginText();
            cos.setFont(fontBold, 12);
            UtilDateModel model = (UtilDateModel) datePicker1.getModel();
            GregorianCalendar d1 = new GregorianCalendar(model.getYear(), model.getMonth(), model.getDay());
            d1.add(Calendar.DAY_OF_MONTH, 1);
            model = (UtilDateModel) datePicker2.getModel();
            GregorianCalendar d2 = new GregorianCalendar(model.getYear(), model.getMonth(), model.getDay());
            d2.add(Calendar.DAY_OF_MONTH, -1);
            String startDate = getDateString(d1);
            String endDate = getDateString(d2);
            String drawString = "DRAW FOR " + startDate + " TO " + endDate;
            adv = metrics.stringWidth(drawString);
            cos.moveTextPositionByAmount((rect.getWidth() / 2) - (adv / 2), rect.getHeight() - 16 * (++line));
            cos.drawString(drawString);
            cos.endText();

            cos.beginText();
            cos.setFont(fontBold, 12);
            //String clubDayString = "CLUB DAYS: " + clubDays.toUpperCase();
            String clubDayString = "CLUB DAYS: ";
            adv = metrics.stringWidth(clubDayString);
            cos.moveTextPositionByAmount((rect.getWidth() / 2) - (adv / 2), rect.getHeight() - 16 * (++line));
            cos.drawString(clubDayString);
            cos.endText();

            // add an image
            try {
                BufferedImage awtImage = ImageIO.read(new File("luisetti.png"));
                PDXObjectImage ximage = new PDPixelMap(document, awtImage);
                float scale = 0.5f; // alter this value to set the image size
                cos.drawXObject(ximage, 190, 705, ximage.getWidth() * scale, ximage.getHeight() * scale);
            } catch (FileNotFoundException fnfex) {
                System.out.println("No image for you");
            }

            line += 8;

            int div = -1;

            for (int j = 0; j < divisions.size(); j += 1) {
                ArrayList<Game> divGames = getDivGames(games, j);

                if (!(line + divGames.size() >= 67)) {
                    if (divGames.size() > 0) {
                        cos.beginText();
                        cos.setFont(fontBold, 12);
                        cos.moveTextPositionByAmount(30, rect.getHeight() - 12 * (++line));
                        cos.drawString(divisions.get(j));
                        cos.endText();

                        for (Game g : divGames) {
                            cos.beginText();
                            cos.setFont(fontPlain, 10);
                            if (g.getChanged().equals("y")) {
                                cos.setNonStrokingColor(Color.RED);
                            } else {
                                cos.setNonStrokingColor(Color.BLACK);
                            }

                            cos.moveTextPositionByAmount(30, rect.getHeight() - 12 * (++line));
                            cos.drawString(g.getPDFDateString());

                            cos.moveTextPositionByAmount(60, 0);
                            cos.drawString(g.getHomeTeamName());

                            cos.moveTextPositionByAmount(125, 0);
                            cos.drawString("vs " + g.getAwayTeamName());

                            cos.moveTextPositionByAmount(125, 0);
                            cos.drawString(g.getTime());

                            cos.moveTextPositionByAmount(50, 0);
                            cos.drawString(g.getLocation());

                            if (!g.getRef().equals("")) {
                                cos.moveTextPositionByAmount(80, 0);
                                cos.drawString("Ref: " + g.getRef());
                            }

                            String string = "";
                            if (!g.getAssRef1().equals("")) {
                                string += g.getAssRef1();
                            }
                            if (!g.getAssRef2().equals("")) {
                                string += " and " + g.getAssRef2();
                            }
                            if (!string.equals("")) {
                                cos.setFont(fontBold, 10);
                                cos.moveTextPositionByAmount(-380, -12);
                                cos.drawString("Assistant Refs: ");

                                cos.setFont(fontPlain, 10);
                                cos.moveTextPositionByAmount(74, 0);
                                cos.drawString(string);
                                line += 1;
                            }
                            cos.endText();
                        }

                        line += 1;
                    }
                } else {
                    div = j;
                    break;
                }

                div = -1;
            }

            // Make sure that the content stream is closed:
            cos.close();

            if (div != -1) {
                PDPage page2 = new PDPage(PDPage.PAGE_SIZE_A4);
                PDRectangle rect2 = page2.getMediaBox(); // rect can be used to get the page width and height
                document.addPage(page2);

                // Start a new content stream which will "hold" the to be created content
                PDPageContentStream cos2 = new PDPageContentStream(document, page2);

                line = 3;

                for (int k = div; k < divisions.size(); k += 1) {
                    ArrayList<Game> divGames = getDivGames(games, k);

                    if (!(line + divGames.size() >= 60)) {
                        if (divGames.size() > 0) {
                            cos2.beginText();
                            cos2.setFont(fontBold, 12);
                            cos2.moveTextPositionByAmount(30, rect2.getHeight() - 12 * (++line));
                            cos2.drawString(divisions.get(k));
                            cos2.endText();

                            for (Game g : divGames) {
                                cos2.beginText();
                                cos2.setFont(fontPlain, 10);
                                if (g.getChanged().equals("y")) {
                                    cos2.setNonStrokingColor(Color.RED);
                                } else {
                                    cos2.setNonStrokingColor(Color.BLACK);
                                }
                                cos2.moveTextPositionByAmount(30, rect.getHeight() - 12 * (++line));
                                cos2.drawString(g.getPDFDateString());

                                cos2.moveTextPositionByAmount(60, 0);
                                cos2.drawString(g.getHomeTeamName());

                                cos2.moveTextPositionByAmount(125, 0);
                                cos2.drawString("vs " + g.getAwayTeamName());

                                cos2.moveTextPositionByAmount(125, 0);
                                cos2.drawString(g.getTime());

                                cos2.moveTextPositionByAmount(50, 0);
                                cos2.drawString(g.getLocation());

                                if (!g.getRef().equals("")) {
                                    cos2.moveTextPositionByAmount(80, 0);
                                    cos2.drawString("Ref: " + g.getRef());
                                }

                                String string = "";
                                if (!g.getAssRef1().equals("")) {
                                    string += g.getAssRef1();
                                }
                                if (!g.getAssRef2().equals("")) {
                                    string += " and " + g.getAssRef2();
                                }
                                if (!string.equals("")) {
                                    cos2.setFont(fontBold, 10);
                                    cos2.moveTextPositionByAmount(-380, -12);
                                    cos2.drawString("Assistant Refs: ");

                                    cos2.setFont(fontPlain, 10);
                                    cos2.moveTextPositionByAmount(74, 0);
                                    cos2.drawString(string);
                                    line += 1;
                                }
                                cos2.endText();
                            }

                            line += 1;
                        }
                    } else {
                        div = k;
                        break;
                    }

                    div = -1;
                }

                cos2.close();
            }
            
            if (div != -1) {
                PDPage page3 = new PDPage(PDPage.PAGE_SIZE_A4);
                PDRectangle rect3 = page3.getMediaBox(); // rect can be used to get the page width and height
                document.addPage(page3);

                // Start a new content stream which will "hold" the to be created content
                PDPageContentStream cos3 = new PDPageContentStream(document, page3);

                line = 3;

                for (int k = div; k < divisions.size(); k += 1) {
                    ArrayList<Game> divGames = getDivGames(games, k);

                    if (!(line + divGames.size() >= 60)) {
                        if (divGames.size() > 0) {
                            cos3.beginText();
                            cos3.setFont(fontBold, 12);
                            cos3.moveTextPositionByAmount(30, rect3.getHeight() - 12 * (++line));
                            cos3.drawString(divisions.get(k));
                            cos3.endText();

                            for (Game g : divGames) {
                                cos3.beginText();
                                cos3.setFont(fontPlain, 10);
                                if (g.getChanged().equals("y")) {
                                    cos3.setNonStrokingColor(Color.RED);
                                } else {
                                    cos3.setNonStrokingColor(Color.BLACK);
                                }

                                cos3.moveTextPositionByAmount(30, rect.getHeight() - 12 * (++line));
                                cos3.drawString(g.getPDFDateString());

                                cos3.moveTextPositionByAmount(60, 0);
                                cos3.drawString(g.getHomeTeamName());

                                cos3.moveTextPositionByAmount(125, 0);
                                cos3.drawString("vs " + g.getAwayTeamName());

                                cos3.moveTextPositionByAmount(125, 0);
                                cos3.drawString(g.getTime());

                                cos3.moveTextPositionByAmount(50, 0);
                                cos3.drawString(g.getLocation());

                                if (!g.getRef().equals("")) {
                                    cos3.moveTextPositionByAmount(80, 0);
                                    cos3.drawString("Ref: " + g.getRef());
                                }

                                String string = "";
                                if (!g.getAssRef1().equals("")) {
                                    string += g.getAssRef1();
                                }
                                if (!g.getAssRef2().equals("")) {
                                    string += " and " + g.getAssRef2();
                                }
                                if (!string.equals("")) {
                                    cos3.setFont(fontBold, 10);
                                    cos3.moveTextPositionByAmount(-380, -12);
                                    cos3.drawString("Assistant Refs: ");

                                    cos3.setFont(fontPlain, 10);
                                    cos3.moveTextPositionByAmount(74, 0);
                                    cos3.drawString(string);
                                    line += 1;
                                }
                                cos3.endText();
                            }

                            line += 1;
                        }
                    } else {
                        div = k;
                        break;
                    }

                    div = -1;
                }

                cos3.close();
            }
            
            if (div != -1) {
                PDPage page4 = new PDPage(PDPage.PAGE_SIZE_A4);
                PDRectangle rect4 = page4.getMediaBox(); // rect can be used to get the page width and height
                document.addPage(page4);

                // Start a new content stream which will "hold" the to be created content
                PDPageContentStream cos4 = new PDPageContentStream(document, page4);

                line = 3;

                for (int k = div; k < divisions.size(); k += 1) {
                    ArrayList<Game> divGames = getDivGames(games, k);

                    if (!(line + divGames.size() >= 60)) {
                        if (divGames.size() > 0) {
                            cos4.beginText();
                            cos4.setFont(fontBold, 12);
                            cos4.moveTextPositionByAmount(30, rect4.getHeight() - 12 * (++line));
                            cos4.drawString(divisions.get(k));
                            cos4.endText();

                            for (Game g : divGames) {
                                cos4.beginText();
                                cos4.setFont(fontPlain, 10);
                                if (g.getChanged().equals("y")) {
                                    cos4.setNonStrokingColor(Color.RED);
                                } else {
                                    cos4.setNonStrokingColor(Color.BLACK);
                                }

                                cos4.moveTextPositionByAmount(30, rect.getHeight() - 12 * (++line));
                                cos4.drawString(g.getPDFDateString());

                                cos4.moveTextPositionByAmount(60, 0);
                                cos4.drawString(g.getHomeTeamName());

                                cos4.moveTextPositionByAmount(125, 0);
                                cos4.drawString("vs " + g.getAwayTeamName());

                                cos4.moveTextPositionByAmount(125, 0);
                                cos4.drawString(g.getTime());

                                cos4.moveTextPositionByAmount(50, 0);
                                cos4.drawString(g.getLocation());

                                if (!g.getRef().equals("")) {
                                    cos4.moveTextPositionByAmount(80, 0);
                                    cos4.drawString("Ref: " + g.getRef());
                                }

                                String string = "";
                                if (!g.getAssRef1().equals("")) {
                                    string += g.getAssRef1();
                                }
                                if (!g.getAssRef2().equals("")) {
                                    string += " and " + g.getAssRef2();
                                }
                                if (!string.equals("")) {
                                    cos4.setFont(fontBold, 10);
                                    cos4.moveTextPositionByAmount(-380, -12);
                                    cos4.drawString("Assistant Refs: ");

                                    cos4.setFont(fontPlain, 10);
                                    cos4.moveTextPositionByAmount(74, 0);
                                    cos4.drawString(string);
                                    line += 1;
                                }
                                cos4.endText();
                            }

                            line += 1;
                        }
                    } else {
                        div = k;
                        break;
                    }

                    div = -1;
                }

                cos4.close();
            }

            if (div != -1) {
                JOptionPane.showMessageDialog(null, "The page limit of 4 was reached. Please \nchange the dates to include less games.");
            }

            // Save the results and ensure that the document is properly closed:
            document.save(outputFileName);
            document.close();
            JOptionPane.showMessageDialog(null, "PDF created");
        } catch (Exception e) {
        	JOptionPane.showMessageDialog(null, "Error. Please send me an email with this: " + e.toString());
        }
    }

    /**
     * Create PDF of this weeks games
     */
    private void createPDFResults(ArrayList<Game> games) {
        List<String> divisions = Arrays.asList("LUISETTI SEEDS DIVISION 1", "WOMENS - CUP", "ELLESMERE DIVISION 2",
                "ELLESMERE DIVISION 3", "ELLES/MID CANT COMBINED COLTS", "ELLES/MID CANT/NC COMBINED U18",
                "ELLESMERE/MID CANT NC U16", "ELLESMERE U14", "ELLESMERE U13", "ELLESMERE U11.5", "ELLESMERE U10",
                "ELLESMERE U8.5", "ELLESMERE U7");

        String outputFileName = "Simple.pdf";
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(GameEditorTable.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (fc.getSelectedFile().exists()) {
                int choice = JOptionPane.showConfirmDialog(fc, "Do you want to overwrite this file?");
                if (choice == JOptionPane.YES_OPTION) {
                    outputFileName = fc.getSelectedFile().getPath();
                }
            } else {
                outputFileName = fc.getSelectedFile().getPath();
            }
        }

        if (!outputFileName.endsWith(".pdf")) {
            outputFileName += ".pdf";
        }

        try {
            // Create a document and add a page to it
            PDDocument document = new PDDocument();
            PDPage page1 = new PDPage(PDPage.PAGE_SIZE_A4);
            PDRectangle rect = page1.getMediaBox(); // rect can be used to get the page width and height
            document.addPage(page1);

            // Create a new font object selecting one of the PDF base fonts
            PDFont fontPlain = PDType1Font.HELVETICA;
            PDFont fontBold = PDType1Font.HELVETICA_BOLD;
            // Start a new content stream which will "hold" the to be created content
            PDPageContentStream cos = new PDPageContentStream(document, page1);

            int line = 1;

            Font font = new Font("Helvetica", Font.BOLD, 12);
            FontMetrics metrics = getGraphics().getFontMetrics(font);

            // Define a text content stream using the selected font, move the cursor and draw some text
            cos.beginText();
            String titleString = "ELLESMERE RUGBY SUB UNION";
            cos.setFont(fontBold, 12);
            int adv = metrics.stringWidth(titleString);
            cos.moveTextPositionByAmount((rect.getWidth() / 2) - (adv / 2), rect.getHeight() - 16 * (++line));
            cos.drawString(titleString);
            cos.endText();

            cos.beginText();
            cos.setFont(fontBold, 12);
            UtilDateModel model = (UtilDateModel) datePicker1.getModel();
            GregorianCalendar d1 = new GregorianCalendar(model.getYear(), model.getMonth(), model.getDay());
            d1.add(Calendar.DAY_OF_MONTH, 1);
            model = (UtilDateModel) datePicker2.getModel();
            GregorianCalendar d2 = new GregorianCalendar(model.getYear(), model.getMonth(), model.getDay());
            d2.add(Calendar.DAY_OF_MONTH, -1);
            String startDate = getDateString(d1);
            String endDate = getDateString(d2);
            String drawString = "RESULTS FOR " + startDate + " TO " + endDate;
            adv = metrics.stringWidth(drawString);
            cos.moveTextPositionByAmount((rect.getWidth() / 2) - (adv / 2), rect.getHeight() - 16 * (++line));
            cos.drawString(drawString);
            cos.endText();

            // add an image
            try {
                BufferedImage awtImage = ImageIO.read(new File("luisetti.png"));
                PDXObjectImage ximage = new PDPixelMap(document, awtImage);
                float scale = 0.5f; // alter this value to set the image size
                cos.drawXObject(ximage, 190, 730, ximage.getWidth() * scale, ximage.getHeight() * scale);
            } catch (FileNotFoundException fnfex) {
                System.out.println("No image for you");
            }

            line += 6;

            int div = -1;

            for (int j = 0; j < divisions.size(); j += 1) {
                ArrayList<Game> divGames = getDivGames(games, j);

                if (!(line + divGames.size() >= 67)) {
                    if (divGames.size() > 0) {
                        cos.beginText();
                        cos.setFont(fontBold, 12);
                        cos.moveTextPositionByAmount(40, rect.getHeight() - 12 * (++line));
                        cos.drawString(divisions.get(j));
                        cos.endText();

                        for (Game g : divGames) {
                            cos.beginText();
                            if (g.getHomeTeamScore() > g.awayTeamScore) {
                                cos.setFont(fontBold, 10);
                            } else {
                                cos.setFont(fontPlain, 10);
                            }
                            cos.moveTextPositionByAmount(40, rect.getHeight() - 12 * (++line));
                            cos.drawString(g.getHomeTeamName() + " " + g.getHomeTeamScore());

                            if (g.getHomeTeamScore() < g.awayTeamScore) {
                                cos.setFont(fontBold, 10);
                            } else {
                                cos.setFont(fontPlain, 10);
                            }
                            cos.moveTextPositionByAmount(125, 0);
                            cos.drawString("vs " + g.getAwayTeamName() + " " + g.getAwayTeamScore());
                            cos.endText();
                        }

                        line += 1;
                    }
                } else {
                    div = j;
                    break;
                }

                div = -1;
            }

            // Make sure that the content stream is closed:
            cos.close();

            if (div != -1) {
                PDPage page2 = new PDPage(PDPage.PAGE_SIZE_A4);
                PDRectangle rect2 = page2.getMediaBox(); // rect can be used to get the page width and height
                document.addPage(page2);

                // Start a new content stream which will "hold" the to be created content
                PDPageContentStream cos2 = new PDPageContentStream(document, page2);

                line = 3;

                for (int j = div; j < divisions.size(); j += 1) {
                    ArrayList<Game> divGames = getDivGames(games, j);

                    if (!(line + divGames.size() >= 67)) {
                        if (divGames.size() > 0) {
                            cos2.beginText();
                            cos2.setFont(fontBold, 12);
                            cos2.moveTextPositionByAmount(40, rect2.getHeight() - 12 * (++line));
                            cos2.drawString(divisions.get(j));
                            cos2.endText();

                            for (Game g : divGames) {
                                cos2.beginText();
                                if (g.getHomeTeamScore() > g.awayTeamScore) {
                                    cos2.setFont(fontBold, 10);
                                } else {
                                    cos2.setFont(fontPlain, 10);
                                }
                                cos2.moveTextPositionByAmount(40, rect2.getHeight() - 12 * (++line));
                                cos2.drawString(g.getHomeTeamName() + " " + g.getHomeTeamScore());

                                if (g.getHomeTeamScore() < g.awayTeamScore) {
                                    cos2.setFont(fontBold, 10);
                                } else {
                                    cos2.setFont(fontPlain, 10);
                                }
                                cos2.moveTextPositionByAmount(125, 0);
                                cos2.drawString("vs " + g.getAwayTeamName() + " " + g.getAwayTeamScore());
                                cos2.endText();
                            }

                            line += 1;
                        }
                    } else {
                        div = j;
                        break;
                    }

                    div = -1;
                }


                cos2.close();
            }

            if (div != -1) {
                PDPage page3 = new PDPage(PDPage.PAGE_SIZE_A4);
                PDRectangle rect3 = page3.getMediaBox(); // rect can be used to get the page width and height
                document.addPage(page3);

                // Start a new content stream which will "hold" the to be created content
                PDPageContentStream cos3 = new PDPageContentStream(document, page3);

                line = 3;

                for (int j = div; j < divisions.size(); j += 1) {
                    ArrayList<Game> divGames = getDivGames(games, j);

                    if (!(line + divGames.size() >= 67)) {
                        if (divGames.size() > 0) {
                            cos3.beginText();
                            cos3.setFont(fontBold, 12);
                            cos3.moveTextPositionByAmount(40, rect3.getHeight() - 12 * (++line));
                            cos3.drawString(divisions.get(j));
                            cos3.endText();

                            for (Game g : divGames) {
                                cos3.beginText();
                                if (g.getHomeTeamScore() > g.awayTeamScore) {
                                    cos3.setFont(fontBold, 10);
                                } else {
                                    cos3.setFont(fontPlain, 10);
                                }
                                cos3.moveTextPositionByAmount(40, rect3.getHeight() - 12 * (++line));
                                cos3.drawString(g.getHomeTeamName() + " " + g.getHomeTeamScore());

                                if (g.getHomeTeamScore() < g.awayTeamScore) {
                                    cos3.setFont(fontBold, 10);
                                } else {
                                    cos3.setFont(fontPlain, 10);
                                }
                                cos3.moveTextPositionByAmount(125, 0);
                                cos3.drawString("vs " + g.getAwayTeamName() + " " + g.getAwayTeamScore());
                                cos3.endText();
                            }

                            line += 1;
                        }
                    } else {
                        div = j;
                        break;
                    }

                    div = -1;
                }


                cos3.close();
            }

            if (div != -1) {
                PDPage page4 = new PDPage(PDPage.PAGE_SIZE_A4);
                PDRectangle rect4 = page4.getMediaBox(); // rect can be used to get the page width and height
                document.addPage(page4);

                // Start a new content stream which will "hold" the to be created content
                PDPageContentStream cos4 = new PDPageContentStream(document, page4);

                line = 3;

                for (int j = div; j < divisions.size(); j += 1) {
                    ArrayList<Game> divGames = getDivGames(games, j);

                    if (!(line + divGames.size() >= 67)) {
                        if (divGames.size() > 0) {
                            cos4.beginText();
                            cos4.setFont(fontBold, 12);
                            cos4.moveTextPositionByAmount(40, rect4.getHeight() - 12 * (++line));
                            cos4.drawString(divisions.get(j));
                            cos4.endText();

                            for (Game g : divGames) {
                                cos4.beginText();
                                if (g.getHomeTeamScore() > g.awayTeamScore) {
                                    cos4.setFont(fontBold, 10);
                                } else {
                                    cos4.setFont(fontPlain, 10);
                                }
                                cos4.moveTextPositionByAmount(40, rect4.getHeight() - 12 * (++line));
                                cos4.drawString(g.getHomeTeamName() + " " + g.getHomeTeamScore());

                                if (g.getHomeTeamScore() < g.awayTeamScore) {
                                    cos4.setFont(fontBold, 10);
                                } else {
                                    cos4.setFont(fontPlain, 10);
                                }
                                cos4.moveTextPositionByAmount(125, 0);
                                cos4.drawString("vs " + g.getAwayTeamName() + " " + g.getAwayTeamScore());
                                cos4.endText();
                            }

                            line += 1;
                        }
                    } else {
                        div = j;
                        break;
                    }

                    div = -1;
                }

                cos4.close();
            }

            if (div != -1) {
                JOptionPane.showMessageDialog(null, "The page limit of 4 was reached. Please \nchange the dates to include less games.");
            }

            // Save the results and ensure that the document is properly closed:
            document.save(outputFileName);
            document.close();
            JOptionPane.showMessageDialog(null, "PDF created");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets all the games in a certain division
     * @param games Arraylist of all game objects
     * @param j ID for division games need to be in
     * @return All the games in a certain division
     */
    private ArrayList<Game> getDivGames(ArrayList<Game> games, int j) {
        ArrayList<Game> divGames = new ArrayList<>();
        UtilDateModel model = (UtilDateModel) datePicker1.getModel();
        int startWeek = Integer.parseInt(pad(model.getYear()) + pad(model.getMonth() + 1) + pad(model.getDay()));
        model = (UtilDateModel) datePicker2.getModel();
        int endWeek = Integer.parseInt(pad(model.getYear()) + pad(model.getMonth() + 1) + pad(model.getDay()));

        for (Game g : games) {
            int divID = Integer.parseInt(String.valueOf(g.getGameID()).substring(12, 14));
            int dateInt = Integer.parseInt(String.valueOf(g.getGameID()).substring(0, 8));
            if (divID == j && dateInt > startWeek && dateInt < endWeek) {
                divGames.add(g);
            }
        }

        return divGames;
    }

    // Pads single digit ints with a leading zero to keep 2 character length
    private String pad(int c) {
        return c >= 10 ? String.valueOf(c) : "0" + String.valueOf(c);
    }

    private String getDateString(Calendar c) {
        String dateString = "";
        dateString += Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").get(c.get(Calendar.DAY_OF_WEEK));
        dateString += " ";
        dateString += c.get(Calendar.DAY_OF_MONTH);
        dateString += " ";
        dateString += Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                "Aug", "Sep", "Oct", "Nov", "Dec").get(c.get(Calendar.MONTH));

        return dateString.toUpperCase();
    }

    public static void main(String[] args) {
        new GameEditorTable().setVisible(true);
    }
}
