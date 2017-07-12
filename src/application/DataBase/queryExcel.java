package application.DataBase;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Admin on 30.06.2017.
 */
public class queryExcel {
    private ArrayList<String> queries;
    private static final String QUERIES = "Translate.xls";

    public queryExcel(String lang, String last_query, String path) {
        queries = new ArrayList<>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path + QUERIES);
            Workbook workbook = new HSSFWorkbook(fis);
            String str = null;
            int i = 0;
            do {
                try {
                    str = workbook.getSheetAt(0).getRow(0).getCell(i).getStringCellValue();
                } catch (NullPointerException e)
                { str = null; }
                if (str == null || str.equals("")){
                    i = 0;
                    break;
                }
                if (str.equals(lang)) {
                    break;
                }
                i++;
            } while (true);
            int j = 1;
            boolean isAfterLastQuery = false;
            do {
                if (last_query.equals("START")) {
                    try {
                        str = workbook.getSheetAt(0).getRow(j).getCell(i).getStringCellValue();
                        queries.add(str);
                    } catch (NullPointerException e) { str = null; }
                } else {
                    try {
                        str = workbook.getSheetAt(0).getRow(j).getCell(i).getStringCellValue();
                        if (str.equals(last_query)){
                            isAfterLastQuery = true;
                        }
                    } catch (NullPointerException e) { str = null; }
                    if (isAfterLastQuery){
                        if (str != null)
                            queries.add(str);
                    }
                }
                j++;
            } while (str != null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public static boolean isNew(String lang, String query, String path){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path + QUERIES);
            Workbook workbook = new HSSFWorkbook(fis);
            String str = null;
            int i = 0;
            do {
                try {
                    str = workbook.getSheetAt(0).getRow(0).getCell(i).getStringCellValue();
                } catch (NullPointerException e)
                { str = null; }
                if (str == null || str.equals("")){
                    Cell cell = workbook.getSheetAt(0).getRow(0).createCell(i);
                    fis.close();
                    cell.setCellValue(lang);
                    FileOutputStream fos = new FileOutputStream(path + QUERIES);
                    workbook.write(fos);
                    fos.close();
                    return true;
                }
                if (str.equals(lang)) {
                    break;
                }
                i++;
            } while (true);
            int j = 1;

            do {
                try {
                    str = workbook.getSheetAt(0).getRow(j).getCell(i).getStringCellValue();
                } catch (NullPointerException e) { str = null; }
                if (str.equals("Пригородные поезда")){
                    str = str;
                }
                if (str.equals("")){
                    Cell cell = workbook.getSheetAt(0).getRow(j).createCell(i);
                    cell.setCellValue(query);
                    //fis.close();
                    FileOutputStream fos = new FileOutputStream(path + QUERIES);
                    workbook.write(fos);
                    fos.close();
                    break;
                }
                if (str.equals(query)){
                    fis.close();
                    break;
                }
                /*if (last_query.equals("START")) {
                    try {
                        str = workbook.getSheetAt(0).getRow(j).getCell(i).getStringCellValue();
                        queries.add(str);
                    } catch (NullPointerException e) { str = null; }
                } else {
                    try {
                        str = workbook.getSheetAt(0).getRow(j).getCell(i).getStringCellValue();
                        if (str.equals(last_query)){
                            isAfterLastQuery = true;
                        }
                    } catch (NullPointerException e) { str = null; }
                    if (isAfterLastQuery){
                        if (str != null)
                            queries.add(str);
                    }
                }*/
                j++;
            } while (str != null);
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }

    public ArrayList<String> getList(){ return queries; }
}
