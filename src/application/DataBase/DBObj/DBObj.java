package application.DataBase.DBObj;


import application.Company;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by Admin on 04.07.2017.
 */
public class DBObj {

    private boolean isWorking = false;
    public synchronized void addRes(ArrayList<String> info, int startSave, int endSave) {
        isWorking = true;
        PreparedStatement pstatement = null;
        String insertTableSQL = "insert into `cfgm`.`result`"
                + " (`name`, `address`, `phone_number`, `form_phone_number`, `website`, `country`, `query`) values (?, ?, ?, ?, ?, ?, ?)";

        if (!getDBConnection()){
            isWorking = false;
            System.out.println("Cant get db connection!");
            return;
        }
        try {
            connection.setAutoCommit(false);
            pstatement = connection.prepareStatement(insertTableSQL);
            for (int i = startSave; i < endSave; i++) {
                System.out.println(info.get(i));
                String splitInfo[] = info.get(i).split(";");


                Company company = new Company(splitInfo);

                pstatement.setString(1, company.getName());
                pstatement.setString(2, company.getAddress());
                pstatement.setString(3, company.getPhone_number());
                pstatement.setString(4, company.getForm_phone_number());
                pstatement.setString(5, company.getWebsite());
                pstatement.setString(6, company.getCountry());
                pstatement.setString(7, company.getQuery());

                pstatement.addBatch();
            }
            pstatement.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (pstatement != null) {
            try {
                pstatement.close();
            }
            catch (SQLException e){
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            }
            catch (SQLException e){
                e.printStackTrace();
            }
        }
        isWorking = false;
    }

    public boolean deleteResTable() {
        if (isWorking)
            return false;
        String deleteTableSQL = "TRUNCATE TABLE cfgm.result";

        return sendToDB(deleteTableSQL);
    }

    private boolean sendToDB(String querySQL){
        Statement statement = null;

        if (!getDBConnection()){
            return false;
        }
        try {
            statement = connection.createStatement();

            // выполнить SQL запрос

            statement.executeUpdate(querySQL);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Не удалось добавить поле");
            return false;
        }

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
        return true;
    }

    private static String USERNAME;
    private static String PASSWORD;
    private static String URL;
    private static final  String CONFIG_PROPERTIES = "db.properties";
    private static Connection connection = null;


    public DBObj(String absolutePath){
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
    public boolean init(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch(ClassNotFoundException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean getDBConnection(){
        try{
            connection = DriverManager.getConnection(URL,USERNAME,PASSWORD);
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isWorking() { return isWorking; }
}
