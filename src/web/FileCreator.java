package web;


import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class FileCreator {
    private String country;
    private String query;
    private String path;
    public FileCreator(String country, String query, String path){
        this.country = country;
        this.query = query;
        this.path = path;
    }
    public String CreateFile(){
        String absolutePath = path + "results\\" + country + query.hashCode() + ".txt";

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(absolutePath)));
            ArrayList<String> list = new ResultsGetter(path).getStrResults(country, query);
            for (String str : list){
                bw.write(str);
                bw.newLine();
            }
            bw.flush();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return absolutePath;
    }
}
