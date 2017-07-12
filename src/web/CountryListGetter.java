package web;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Admin on 04.07.2017.
 */
public class CountryListGetter {
    private static final String COUNTRY_LIST = "countries_list.txt";
    private ArrayList<String> list;
    public CountryListGetter(String path){
        list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path + COUNTRY_LIST));
            String str;
            while ((str = reader.readLine()) != null){
                list.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getList(){
        return list;
    }

}
