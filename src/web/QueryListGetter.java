package web;

import application.DataBase.queryExcel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by Admin on 05.07.2017.
 */
public class QueryListGetter {
    private static final String CONFIG_PROPERTIES = "countries.properties";
    private ArrayList<String> list;
    public QueryListGetter(String country, String path){
        Properties countryProp = new Properties();
        InputStream is = null;
        String shortCountry = "";
        try {
            is = new FileInputStream(path + CONFIG_PROPERTIES);
            countryProp.load(is);
            shortCountry = countryProp.getProperty(country);
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
        list = new queryExcel(shortCountry, "START", path).getList();
    }
    public String getSelect(){
        String select = "<select id=\"queryselect\">";
        select += "<option id=\"" + "0" + "\">Select query</option>";
        for (String query : list){
            if (!query.equals("") && query != null)
                select += "<option id=\"" + query + "\">" + query + "</option>";
        }
        select += "</select>";
        return select;
    }
}
