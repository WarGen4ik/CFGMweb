package application.View;

import application.Country;
import application.DataBase.DBObj.DBObj;
import application.DataBase.queryExcel;
import application.data.gather.companies.DataGatherer;
import application.data.gather.companies.exceptions.QuotaLimitException;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import web.ResultsGetter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Admin on 30.06.2017.
 */
public class MainClass {
    private static final String COUNTRIES_COORDS = "countries_coords.csv";
    private static final String SHORT_COUNTRIES_NAMES = "ShortCountriesNames.txt";
    private static final String CONFIG_PROPERTIES = "config.properties";

    private static Logger log = Logger.getLogger(MainClass.class);

    private ArrayList<String> allcompanies = new ArrayList<>();
    private String last_coord;
    private String last_country;
    private String last_query;
    private boolean notEnoughQuota = false;
    private boolean isAlive[];
    private int countGatherers = 0;
    private int number_query = 0;
    private long first_country_coord;
    private long maxCoord = 0;
    private ExecutorService service;
    private ArrayList<String> queries;
    private ArrayList<Gatherer> gatherers;
    private boolean isStopped = false;
    public static String absolutePath;
    private boolean isFinished = true;
    private static DBObj dbObj;


    public MainClass(String absolutePath) {
        this.absolutePath = absolutePath;
        dbObj = new DBObj(absolutePath);

        Properties countryProp = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(absolutePath + CONFIG_PROPERTIES);
            countryProp.load(is);
            setLast_coord(countryProp.getProperty("last_coord"));
            setLast_country(countryProp.getProperty("last_country"));
            setLast_query(countryProp.getProperty("last_query"));
            if (!getLast_query().equals("START"))
                DataGatherer.getIDs();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        gatherers = new ArrayList<>();
    }

    public boolean isDBWorking() {
        return dbObj.isWorking();
    }

    public boolean isInternetProblems() {
        return DataGatherer.internetProblem;
    }

    public Long getFirstCoord() {
        return first_country_coord;
    }

    public String getLast_coord() {
        return last_coord;
    }

    public void setLast_coord(String last_coord) {
        this.last_coord = last_coord;
    }

    public String getLast_country() {
        return last_country;
    }

    public void setLast_country(String last_country) {
        this.last_country = last_country;
    }

    public String getLast_query() {
        return last_query;
    }

    public void setLast_query(String last_query) {
        this.last_query = last_query;
    }

    public boolean isNotEnoughQuota() {
        return notEnoughQuota;
    }

    public void setNotEnoughQuota(boolean notEnoughQuota) {
        this.notEnoughQuota = notEnoughQuota;
    }

    public boolean isIsStopped() {
        return isStopped;
    }

    public void setIsStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public long getMaxCoord() {
        return maxCoord;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setButtonPressed() {
        saveProp("0", "AD", "START");
        setLast_country("AD");
        setLast_coord("0");
        setLast_query("START");
    }

    public void stopButtonPressed() {
        DataGatherer.internetProblem = true;
        setIsStopped(true);
    }

    public void deleteTableResButtonPressed() {
        if (!dbObj.deleteResTable()){
            log.info("Cant delete db, check ur db connection and try again");
        }
    }

    public void startButtonPressed(String currCountry, String query, boolean isOnce, boolean isNew) {
        //isNewQuery(query, currCountry);
        log.info("Start button pressed");
        if (currCountry != null) {
            setLast_country(currCountry);
            if (isNew) {
                setLast_coord("0");
                DataGatherer.toFileId(true, true);
            }
            saveProp(last_coord, last_country, null);
        }
        if (query != null) {
            setLast_query(query);
            if (isNew) {
                setLast_coord("0");
                DataGatherer.toFileId(true, true);
            }
            saveProp(last_coord, null, last_query);
        }
        setIsStopped(false);
        DataGatherer.internetProblem = false;
        isFinished = false;
        Thread backThread = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    allcompanies.clear();
                    DataGatherer.Release();
                    number_query = 0;

                    if (isOnce) {
                        queries = new ArrayList<>();
                        queries.add(getLast_query());
                    } else {
                        queries = getQuery(getLast_country());
                    }
                    do {
                        country = 0;
                        Thread thread = new Thread(() -> {
                            try {
                                ArrayList<Country> countryCoordsList = getCoords(getLast_country(), Long.parseLong(getLast_coord()));

                                setLast_country(countryCoordsList.get(0).getCountry());
                                int radius = 15000;
                                int startSave = 0;
                                int endSave;

                                do {
                                    gatherers.clear();
                                    countGatherers = 50;
                                    service = Executors.newCachedThreadPool();
                                    //log.info("COUNT = " + countGatherers);
                                    isAlive = new boolean[countGatherers];

                                    for (int i = 0; i < countGatherers; i++) {
                                        isAlive[i] = true;
                                        Gatherer gatherer = new Gatherer();
                                        gatherer.setList(getCountriesCoords(countryCoordsList));
                                        //log.info("ID COUNTY - " + country);
                                        if (gatherer.getList().size() == 0) {
                                            isAlive[i] = false;
                                            continue;
                                        }
                                        gatherer.setQuery(getLast_query().replace(" ", "+"));
                                        gatherer.setCountry(getLast_country());
                                        gatherer.setNumber(i);
                                        gatherer.setRadius(radius);
                                        gatherers.add(gatherer);
                                        log.info(gatherer.getName() + " has started. #" + i);
                                        service.submit(gatherer);
                                        Thread.sleep(500);
                                        if (isNotEnoughQuota() || DataGatherer.internetProblem) {
                                            break;
                                        }
                                    }
                                    //log.info("COUNT GATHERERS = " + gatherers.size());
                                    while (isAnyAlive()) {
                                        Thread.sleep(5000);
                                        if (checkInternetConnection()) {
                                            DataGatherer.internetProblem = true;
                                            break;
                                        }
                                        if (isStopped)
                                            break;
                                    }
                                    isAlive = null;
                                    service.shutdownNow();

                                    isStack = false;

                                    endSave = allcompanies.size();
                                    log.info(startSave + " " + endSave);
                                    DBThread dbThread = new DBThread();
                                    dbThread.setPoints(startSave, endSave);
                                    dbThread.start();
                                    startSave = endSave;
                                } while (isNextCoord && !isNotEnoughQuota() && !DataGatherer.internetProblem);
                                //log.info("number country   " + country);
                                number_query++;
                                isNextCoord = true;

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });

                        if (!getLast_query().equals("")) {
                            thread.start();
                            try {
                                thread.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        DataGatherer.toFileId(true, false);
                        if (isOnce)
                            break;
                        if (!(DataGatherer.internetProblem || isNotEnoughQuota())) {
                            nextQuery(queries);
                            setLast_coord("0");
                        }

                        saveProp(getLast_coord(), null, getLast_query());
                        log.info(getLast_query());
                    } while (!getLast_query().equals("END") && !isNotEnoughQuota() && !DataGatherer.internetProblem);

                    if (isOnce)
                        break;
                    if (!(DataGatherer.internetProblem || isNotEnoughQuota())) {
                        nextCountry();
                        saveProp(null, null, "START");
                        setLast_query("START");
                    }
                    DataGatherer.toFileId(true, false);

                    saveProp(null, getLast_country(), null);
                } while (!getLast_country().equals("END") && !isNotEnoughQuota() && !DataGatherer.internetProblem);


                if (isNotEnoughQuota()) {
                    log.info("Not enough quota");
                } else if (DataGatherer.internetProblem && !isIsStopped()) {
                    log.info("Intenet problems");
                } else if (isIsStopped()) {
                    log.info("U have stopped app");
                }

                service.shutdownNow();

                log.info("FINISHED");
                saveProp(Long.toString(country + first_country_coord), null, null);
                isFinished = true;

            }
        });
        backThread.start();
    }
    private boolean isNewQuery(String query, String country){
        return queryExcel.isNew(country, query, absolutePath);
    }

    private static synchronized boolean checkInternetConnection() {
        Boolean result = true;
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL("http://ru.stackoverflow.com/").openConnection();
            con.setRequestMethod("HEAD");
            result = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            result = true;
        } finally {
            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static boolean isStack = false;
    private static int prev_count_companies = 0;

    private synchronized boolean isAnyAlive() {
        int count_alive = 0;
        for (int i = 0; i < isAlive.length; i++) {
            if (isAlive[i])
                count_alive++;
        }
        if (isStack)
            return false;
        if (allcompanies.size() == prev_count_companies) {
            isStack = true;
        }
        prev_count_companies = allcompanies.size();

        for (int i = 0; i < isAlive.length; i++) {
            if (isAlive[i])
                return true;
        }
        return false;
    }
    /*private synchronized boolean isAnyAlive(){
        for (Gatherer gatherer : gatherers) {
            if (gatherer.isAlive())
                return true;
        }
        return false;
    }*/

    private ArrayList<String> getQuery(String country) {
        return new queryExcel(country, getLast_query(), absolutePath).getList();
    }

    private void nextQuery(ArrayList<String> queries) {
        if (!(number_query >= queries.size()) && !getLast_query().equals("")) {
            setLast_query(queries.get(number_query));
        } else {
            setLast_query("END");
        }
    }

    private void nextCountry() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(MainClass.absolutePath + SHORT_COUNTRIES_NAMES));
            String countryLine;
            while ((countryLine = reader.readLine()) != null) {
                if (countryLine.equals(getLast_country())) {
                    setLast_country(reader.readLine());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private static int country = 0;
    private static boolean isNextCoord = true;

    private ArrayList<Country> getCountriesCoords(ArrayList<Country> list) {
        ArrayList<Country> countries = new ArrayList<>();

        int old_coord = country;
        while (old_coord + 5 != country && country < list.size()) {
            countries.add(list.get(country));
            country++;
        }
        if (country >= list.size()) {
            isNextCoord = false;
        }

        return countries;
    }

    private ArrayList<Country> getCoords(String country, Long id) {
        ArrayList<Country> countryList = new ArrayList<>();
        BufferedReader reader = null;
        try {
            String coordLine;
            reader = new BufferedReader(new FileReader(MainClass.absolutePath + COUNTRIES_COORDS));
            boolean temp = false;
            while ((coordLine = reader.readLine()) != null) {
                String splitLine[] = coordLine.split(";");
                if (splitLine[1].equals(country) && !temp) {
                    first_country_coord = Long.parseLong(splitLine[0]);
                    temp = true;
                }
                if (splitLine[1].equals(country) && (id < Long.parseLong(splitLine[0]))) {
                    countryList.add(new Country(country,
                            splitLine[2].replace(",", "."),
                            splitLine[3].replace(",", "."),
                            Integer.parseInt(splitLine[0])));
                    maxCoord = Long.parseLong(splitLine[0]);
                }
            }
        } catch (NullPointerException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        if (countryList.size() == 0 && !getLast_coord().equals("0")) {
            setLast_coord("0");
            return getCoords(country, 0L);
        }
        return countryList;
    }

    private synchronized void saveProp(String id, String country, String query) {
        Properties countryProp = new Properties();
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(MainClass.absolutePath + CONFIG_PROPERTIES);
            countryProp.load(is);
            is.close();

            if (id != null)
                countryProp.setProperty("last_coord", id);
            if (country != null)
                countryProp.setProperty("last_country", country);
            if (query != null)
                countryProp.setProperty("last_query", query);

            os = new FileOutputStream(MainClass.absolutePath + CONFIG_PROPERTIES);
            countryProp.store(os, null);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class Gatherer extends Thread {

        private String query;
        private String country;
        private Map<String, String> companies;
        private ArrayList<Country> list;
        private int radius;
        private int number;

        Gatherer() {
        }

        private void setQuery(String query) {
            this.query = query;
        }

        private void setCountry(String country) {
            this.country = country;
        }

        @Override
        public void run() {
            DataGatherer dataGatherer = new DataGatherer();
            for (int i = 0; i < list.size(); i++) {
                if (isNotEnoughQuota() || DataGatherer.internetProblem || isStopped) {
                    break;
                }
                try {
                    Pair<Map<String, String>, QuotaLimitException> gather =
                            dataGatherer.gather(list.get(i).getCountry(),
                                    Double.parseDouble(list.get(i).getLat()),
                                    Double.parseDouble(list.get(i).getLng()),
                                    query,
                                    radius);
                    companies = gather.getKey();

                    if (gather.getValue() != null) {
                        if (!isNotEnoughQuota()) {
                            setLast_coord(Long.toString(list.get(i).getID()));
                            saveProp(getLast_coord(), getLast_country(), null);
                        }
                        setNotEnoughQuota(true);
                        saveData();
                        break;
                    } else {
                        saveData();
                        setLast_coord(Long.toString(list.get(i).getID()));
                        saveProp(getLast_coord(), null, null);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    log.info("array index out of bounds exception");
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isAlive[number] = false;
        }

        private synchronized void saveData() {
            for (Map.Entry<String, String> entry : companies.entrySet()) {
                boolean temp = true;
                for (String str : allcompanies) {
                    if ((str.equals(entry.getValue()) && !entry.getValue().equals("None"))) {
                        temp = false;
                    }
                }

                if (temp) {
                    int countCopies = 0;
                    for (Map.Entry<String, String> entry1 : companies.entrySet()) {
                        if ((entry1.getValue().equals(entry.getValue()) && !entry.getValue().equals("None"))) {
                            countCopies++;
                        }
                    }
                    if (countCopies > 1)
                        temp = false;
                }

                if (temp) {
                    allcompanies.add(entry.getKey() + ";" + entry.getValue() + ";" + this.country + ";" + this.query.replace('+', ' '));
                }
            }
            companies.clear();
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public void setList(ArrayList<Country> list) {
            this.list = list;
        }

        public ArrayList<Country> getList() {
            return list;
        }


        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }

    public class DBThread extends Thread {
        private int start, end;

        public void setPoints(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            dbObj.addRes(allcompanies, start, end);
        }
    }
}
