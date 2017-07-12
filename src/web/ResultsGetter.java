package web;

import application.DataBase.queryExcel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
/**
 * Created by Admin on 10.07.2017.
 */
public class ResultsGetter {
    private static HashMap<Integer, String> resultQueries = new HashMap<Integer, String>();

    public static HashMap<Integer, String> getResultQueries() {
        return resultQueries;
    }

    public String getResults(String country) {
        StringBuilder result = new StringBuilder();
        Statement statement = null;
        ArrayList<String> queries = new queryExcel(country, "START", path).getList();
        resultQueries.clear();
        try {
            getDBConnection();
            statement = connection.createStatement();
            for (int i = 0; i < queries.size(); i++) {
                String selectTableSQL = "SELECT * FROM `cfgm`.`result`" +
                        " WHERE country = \"" + country + "\" AND query = \"" + queries.get(i) + "\"";

                // выбираем данные с БД
                ResultSet rs = statement.executeQuery(selectTableSQL);
                rs.last();
                //System.out.println(rs.getRow());
                if (rs.getRow() > 1) {
                    result.append("Country = ").append(country).append(", query = ").append(queries.get(i))
                            .append(", count = ").append(rs.getRow())
                            .append("<input type=\"submit\" value=\"Export\" name=\"").append(i).append("\">")
                            .append("<input type=\"submit\" value=\"Delete\" name=\"Delete").append(i).append("\"> <br>");
                    resultQueries.put(i, queries.get(i));
                }
            }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Не вижу данных!");
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }

    public ArrayList<String> getStrResults(String country, String query) {
        ArrayList<String> result = new ArrayList<>();
        Statement statement = null;

        String selectTableSQL = "SELECT * FROM `cfgm`.`result`" +
                " WHERE country = \"" + country + "\" AND query = \"" + query + "\"";

        try {
            getDBConnection();
            statement = connection.createStatement();

            // выбираем данные с БД
            ResultSet rs = statement.executeQuery(selectTableSQL);

            while (rs.next()) {
                String row = "";
                for (int i = 1; i <= 8; i++) {
                    row += rs.getString(i) + ";";
                }
                result.add(row);
            }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public void deleteRows(String country, String query){
        Statement statement = null;

        String deleteTableSQL = "DELETE FROM `cfgm`.`result`" +
                " WHERE country = \"" + country + "\" AND query = \"" + query + "\"";

        try {
            getDBConnection();
            statement = connection.createStatement();

            statement.execute(deleteTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String USERNAME;
    private static String PASSWORD;
    private static String URL;
    private static final String CONFIG_PROPERTIES = "db.properties";
    private static Connection connection = null;
    private String path;


    public ResultsGetter(String absolutePath) {
        path = absolutePath;
        Properties prop = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(absolutePath + CONFIG_PROPERTIES);
            prop.load(is);
            USERNAME = prop.getProperty("username");
            PASSWORD = prop.getProperty("password");
            URL = prop.getProperty("url") + "?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
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
        init();
    }

    public boolean init() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("cant find jdbc");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean getDBConnection() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}