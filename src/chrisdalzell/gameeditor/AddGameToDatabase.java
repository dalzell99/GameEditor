package chrisdalzell.gameeditor;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class AddGameToDatabase extends JFrame {

    private static final long serialVersionUID = 1L;

    public static List<String> divisions = Arrays.asList("Div 1", "Women", "Div 2", "Div 3", "Colts",
            "U18", "U16", "U14.5", "U13", "U11.5", "U10", "U8.5", "U7");
    List<String> teamsDiv1 = Arrays.asList("Hornby", "Waihora", "Lincoln", "Rakaia", "Methven", "Southbridge", "BDI", "Glenmark", "Darfield",
            "Ashley", "Prebbleton", "Celtic", "Saracens", "Oxford", "Ohoka", "Kaiapoi", "West Melton", "Southern", "Hampstead", "Rolleston");
    List<String> teamsWomen = Arrays.asList("Hornby/Burnside", "BDI", "University", "Sydenham", "HSOB", "Linwood", "Suburbs", "Kaiapoi", "Marist Albion", "Christchurch");
    List<String> teamsDiv2 = new ArrayList<String>() {{add("Springston"); add("Diamond Harbour"); add("Darfield"); add("Banks Peninsula");
            add("Southbridge"); add("Kirwee"); add("Lincoln"); add("Prebbleton");}};
    List<String> teamsDiv3 = Arrays.asList("Hornby", "Waihora", "Kirwee", "Springston", "BDI", "Lincoln", "Rolleston", "West Melton");
    List<String> teamsColts = new ArrayList<String>(){{ add("Banks Peninsula"); add( "Waihora"); add( "Prebbleton"); add( "Celtic"); add( "Lincoln Red"); add( "Lincoln Black"); add( "West Melton"); add( "Darfield"); add(
            "Springston"); add( "Kirwee");}};
    List<String> teamsU18 = Arrays.asList("Malvern Combined", "Waihora", "Rangiora High School", "Methven/Rakaia", "Hurunui",
            "Kaiapoi", "Ashley/Oxford", "West Melton/Rolleston", "Lincoln", "Celtic");
    List<String> teamsU16 = Arrays.asList("Ashley/Amberley", "Oxford", "Waihora", "Rolleston", "Prebbleton", "West Melton/Southbridge", "Celtic",
            "Malvern", "Lincoln", "Kaiapoi/Woodend", "Hampstead", "Hurunui", "Methven", "Saracens");
    List<String> teamsU145 = Arrays.asList("Rolleston Gold", "Rolleston Black", "Prebbleton", "Malvern Combined", "West Melton", "Waihora", "Lincoln/Springston", "Duns/Irwell/Leeston");
    List<String> teamsU13 = new ArrayList<String>() {{ add("Rolleston Black"); add("Rolleston Gold"); add("West Melton"); add("Lincoln"); add("Waihora");
            add("Prebbleton White"); add("Springston/Lincoln"); add("Prebbleton Blue"); add("Darfield"); add("Southbridge"); add("Malvern Combined");}};
    List<String> teamsU115 = Arrays.asList("Rolleston Black", "Rolleston Gold", "Lincoln", "Southbridge", "Waihora",
            "Duns/Irwell", "West Melton Gold", "West Melton Blue", "Prebbleton Blue", "Prebbleton White", "Banks Peninsula/Waihora",
            "Leeston", "Malvern Combined", "Prebbleton Green", "Prebbleton Red", "Springston");
    List<String> teamsU10 = Arrays.asList("Rolleston Black", "Rolleston Gold", "Lincoln Red", "Lincoln Black", "Waihora White", "Waihora Black",
            "Duns/Irwell", "West Melton Gold", "West Melton Blue", "Prebbleton Blue", "Prebbleton White", "Banks Peninsula",
            "Leeston/Southbridge", "Prebbleton Green", "Prebbleton Red", "Springston", "Selwyn", "Darfield", "Rolleston Red", "Rolleston Blue");
    List<String> teamsU85 = Arrays.asList("Rolleston Black", "Rolleston Gold", "Rolleston White", "Lincoln Red", "Lincoln Black", "Waihora White", "Waihora Black", "Waihora Red",
            "Duns/Irwell", "West Melton Gold", "West Melton Blue", "Prebbleton Blue", "Prebbleton White", "Banks Peninsula",
            "Leeston Red", "Leeston Black", "Prebbleton Green", "Prebbleton Red", "Springston Black", "Springston Green", "Selwyn", "Darfield", "Sheffield", "Rolleston Red",
            "Leeston White", "West Melton White", "Kirwee", "Southbridge");
    List<String> teamsU7 = new ArrayList<String>() {{
        add("Rolleston Black");
        add("Rolleston Gold");
        add("Rolleston Red");
        add("Rolleston Blue");
        add("Rolleston White");
        add("Lincoln Red");
        add("Lincoln Black");
        add("Lincoln Green");
        add("Lincoln White");
        add("Waihora White");
        add("Waihora Black");
        add("Waihora Red");
        add("Waihora Gold");
        add("Waihora Green");
        add("Duns/Irwell Blue");
        add("Duns/Irwell Black");
        add("West Melton Gold");
        add("West Melton Blue");
        add("West Melton White");
        add("West Melton Red");
        add("West Melton Black");
        add("Prebbleton 1");
        add("Prebbleton 2");
        add("Prebbleton 3");
        add("Prebbleton 4");
        add("Prebbleton 5");
        add("Prebbleton 6");
        add("Prebbleton 7");
        add("Prebbleton 8");
        add("Banks Peninsula Maroon");
        add("Banks Peninsula Gold");
        add("Leeston Red");
        add("Leeston Black");
        add("Leeston White");
        add("Springston Black");
        add("Springston Green");
        add("Springston Red");
        add("Selwyn Black");
        add("Selwyn Green");
        add("Darfield Red");
        add("Darfield Blue");
        add("Sheffield");
        add("Kirwee Red");
        add("Kirwee Yellow");
        add("Kirwee White");
        add("Kirwee Gold");
        add("Southbridge White");
        add("Southbridge Blue");
        add("Southbridge Black");
        add("Diamond Harbour White");
        add("Diamond Harbour Blue");
    }};

    public AddGameToDatabase() {
        // Sort all the team collections alphabetically except womens
        Collections.sort(teamsDiv1);
        Collections.sort(teamsDiv2);
        Collections.sort(teamsDiv3);
        Collections.sort(teamsColts);
        Collections.sort(teamsU18);
        Collections.sort(teamsU16);
        Collections.sort(teamsU145);
        Collections.sort(teamsU13);
        Collections.sort(teamsU115);
        Collections.sort(teamsU10);
        Collections.sort(teamsU85);
        Collections.sort(teamsU7);
        teamsU7.add(18, "Team Removed");
        teamsU7.add("Rolleston Silver");
        teamsDiv2.add(1, "BDI");
        teamsColts.add(1, "BDI");
        teamsU13.add(1, "Leeston/Duns/Irwell");

        Label lblDiv = new Label("Division");
        Label lblHome = new Label("Home Team");
        Label lblAway = new Label("Away Team");
        Label lblDate = new Label("Date");

        final JComboBox<String> cbxDiv = new JComboBox<>();
        final JComboBox<String> cbxHome = new JComboBox<>();
        final JComboBox<String> cbxAway = new JComboBox<>();

        cbxDiv.addActionListener(e -> {
            cbxHome.removeAllItems();
            cbxAway.removeAllItems();
            switch (cbxDiv.getSelectedIndex()) {
                case 0:
                    for (String s : teamsDiv1) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 1:
                    for (String s : teamsWomen) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 2:
                    for (String s : teamsDiv2) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 3:
                    for (String s : teamsDiv3) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 4:
                    for (String s : teamsColts) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 5:
                    for (String s : teamsU18) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 6:
                    for (String s : teamsU16) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 7:
                    for (String s : teamsU145) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 8:
                    for (String s : teamsU13) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 9:
                    for (String s : teamsU115) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 10:
                    for (String s : teamsU10) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 11:
                    for (String s : teamsU85) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
                case 12:
                    for (String s : teamsU7) {
                        cbxHome.addItem(s);
                        cbxAway.addItem(s);
                    }
                    break;
            }
        });

        divisions.forEach(cbxDiv::addItem);
        for (String s : teamsDiv1) {
            cbxHome.addItem(s);
            cbxAway.addItem(s);
        }

        UtilDateModel model = new UtilDateModel();
        model.setDate(2015, 2, 2);
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        final JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        JButton btnCreate = new JButton("Create Game");
        btnCreate.addActionListener(arg0 -> {
            String home = (String) cbxHome.getSelectedItem();
            String away = (String) cbxAway.getSelectedItem();

            int div = cbxDiv.getSelectedIndex();
            int homeID = cbxHome.getSelectedIndex();
            int awayID = cbxAway.getSelectedIndex();

            Date selectedDate = (Date) datePicker.getModel().getValue();
            Calendar cal = Calendar.getInstance();
            cal.setTime(selectedDate);
            String date = cal.get(Calendar.YEAR) + pad(cal.get(Calendar.MONTH) + 1) + pad(cal.get(Calendar.DAY_OF_MONTH));

            String gameID = createGameID(div, homeID, awayID, date);

            try {
                upload(gameID, home, away);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        JPanel pnl = (JPanel) getContentPane();
        GroupLayout layout = new GroupLayout(pnl);
        pnl.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(lblDiv)
                                .addComponent(cbxDiv))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(lblHome)
                                .addComponent(cbxHome))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(lblAway)
                                .addComponent(cbxAway))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(lblDate)
                                .addComponent(datePicker))
                        .addComponent(btnCreate)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(lblDiv)
                                .addComponent(cbxDiv))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(lblHome)
                                .addComponent(cbxHome))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(lblAway)
                                .addComponent(cbxAway))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(lblDate)
                                .addComponent(datePicker))
                        .addComponent(btnCreate)
        );

        setTitle("Create Game");
        setSize(960, 540);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        pack();
    }

    // Uses Apaches HTTPComponents Library to upload data to php script
    private void upload(String gameID, String home, String away) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://www.possumpam.com/rugby-scoring-app-scripts/create_game.php");

        // Add all game details to List<NameValuePair> and add to HttpPost
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("gameID", gameID));
        nameValuePairs.add(new BasicNameValuePair("homeTeam", home));
        nameValuePairs.add(new BasicNameValuePair("awayTeam", away));
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        CloseableHttpResponse response2;
        response2 = httpclient.execute(httpPost);

        // Convert response to String
        HttpEntity entity = response2.getEntity();
        InputStream is = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        is.close();
        System.out.println(sb);
    }

    // Pads single digit ints with a leading zero to keep 2 character length
    private String pad(int c) {
        return c >= 10 ? String.valueOf(c) : "0" + String.valueOf(c);
    }

    // Create gameID from date, teams and division
    private String createGameID(int div, int home, int away, String date) {
        String gameID = "";

        // Add date to gameID
        gameID += date;

        // Add teamIDs to gameID. If teamID less than 10,
        // add a 0 to preserve gameID length
        gameID += pad(home);
        gameID += pad(away);

        // Add divisionID to gameID.
        gameID += pad(div);

        System.out.println(gameID);
        return gameID;
    }
}
