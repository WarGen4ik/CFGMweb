package web;

import application.View.MainClass;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Admin on 30.06.2017.
 */
//@WebServlet("/")
public class WebConnector extends HttpServlet {

    private MainClass gatherer = null;

    @Override
    public void init() throws ServletException {
        // initialize log4j here
        ServletContext context = getServletContext();
        String log4jConfigFile = context.getInitParameter("log4j-config-location");
        String fullPath = context.getRealPath("") + File.separator + log4jConfigFile;

        PropertyConfigurator.configure(fullPath);

        super.init();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=utf-8");

        RequestDispatcher dispatcher = request.getRequestDispatcher("main.jsp");

        if (request.getParameter("Start") != null && request.getParameter("country") != null && request.getParameter("query") != null &&
                !request.getParameter("country").equals("0") && !request.getParameter("query").equals("")) {
            System.out.println("Start button pressed");
            if (gatherer != null && !gatherer.isFinished()) {
                if (dispatcher != null) {
                    dispatcher.forward(request, response);
                }
                return;
            }
            gatherer = new MainClass(getServletContext().getRealPath(""));

            String currCountry = getShortName(request.getParameter("country"));
            String currQuery = request.getParameter("query");
            boolean isNew = request.getParameter("isNew") != null;

            gatherer.startButtonPressed(currCountry.equals("") ? null : currCountry, currQuery.equals("") ? null : currQuery, true, isNew);
        }

        if (request.getParameter("Stop") != null) {
            if (gatherer != null && !gatherer.isFinished()) {
                gatherer.stopButtonPressed();
            }
        }

        if (request.getParameter("Delete") != null) {
            if (gatherer != null)
                gatherer.deleteTableResButtonPressed();
            else {
                gatherer = new MainClass(getServletContext().getRealPath(""));
                gatherer.deleteTableResButtonPressed();
            }
        }

        if (dispatcher != null) {
            dispatcher.forward(request, response);
        }
    }
    private String country = null;

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("UTF-8");

        RequestDispatcher dispatcher = request.getRequestDispatcher("main.jsp");

        if (request.getParameter("country") != null && request.getParameter("results") == null) {
            country = request.getParameter("country");
            response.getWriter().write(new QueryListGetter(request.getParameter("country"), request.getServletContext().getRealPath("")).getSelect());
        } else if (request.getParameter("query") != null)
            response.getWriter().write(request.getParameter("query"));

        if (request.getParameter("results") != null) {
            String result = new ResultsGetter(request.getServletContext().getRealPath("")).getResults(getShortName(request.getParameter("country")));
            if (!result.equals(""))
                response.getWriter().write(result);
        }

        if (request.getParameter("progress") != null) {
            if (gatherer != null)
                if (!gatherer.isFinished()) {
                    long coord = (Long.parseLong(gatherer.getLast_coord()) - gatherer.getFirstCoord());
                    response.getWriter().write((coord > 0 ? coord : 0) + "/" + (gatherer.getMaxCoord() - gatherer.getFirstCoord()));
                } else if (gatherer.isDBWorking()) {
                    response.getWriter().write("Data is saving to db...");
                } else if (gatherer.isNotEnoughQuota()) {
                    response.getWriter().write("Your quota has been ended");
                } else if (gatherer.isIsStopped()) {
                    response.getWriter().write("You have stopped gatherer");
                } else if (gatherer.isInternetProblems()) {
                    response.getWriter().write("You have internet problems, pls try again");
                } else if (gatherer.isFinished()) {
                    response.getWriter().write("Finished!");
                } else {
                    response.getWriter().write("");
                }
        } else {

            ArrayList<String> queries = ResultsGetter.getResultQueries();
            if (queries.size() > 0) {
                for (String p : queries) {
                    if (request.getParameter(p) != null) {
                        System.out.println(p);
                        processRequest(request, response, getShortName(country), p);
                    } else if (request.getParameter("Delete" + p) != null){
                        new ResultsGetter(getServletContext().getRealPath("")).deleteRows(getShortName(country),p);
                        if (dispatcher != null) {
                            dispatcher.forward(request, response);
                        }
                    }
                }
            }
        }
    }

    private static final String CONFIG_PROPERTIES = "countries.properties";

    private String getShortName(String country) {
        Properties countryProp = new Properties();
        InputStream is = null;
        String shortCountry = "";
        try {
            is = new FileInputStream(getServletContext().getRealPath("") + CONFIG_PROPERTIES);
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
        return shortCountry;
    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response, String country, String query)
            throws ServletException, IOException {
        String fileName = new FileCreator(country, query, getServletContext().getRealPath("")).CreateFile();
        //fileName = URLEncoder.encode(fileName,"UTF-8");
        String fileType = "application/octet-stream";
        // Find this file id in database to get file name, and file type

        // You must tell the browser the file type you are going to send
        // for example application/pdf, text/plain, text/html, image/jpg
        response.setContentType(fileType);
        response.setCharacterEncoding("UTF-8");

        // Make sure to show the download dialog
        response.setHeader("Content-disposition","attachment; filename=" + country + query.hashCode() + ".txt");
        String res = "";
        // Assume file name is retrieved from database
        // For example D:\\file\\test.pdf

        File my_file = new File(fileName);

        // This should send the file to browser
        ServletOutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream(my_file);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0){
            out.write(buffer, 0, length);
        }
        in.close();
        out.flush();
        /*try {
            BufferedReader reader = new BufferedReader(new FileReader(getServletContext().getRealPath("") + "results/" + country + query.hashCode() + ".csv"));
            String str;
            while ((str = reader.readLine()) != null){
                res += str + '\n';
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        OutputStream stream =  response.getOutputStream();
        PrintWriter out = new PrintWriter(stream);
        out.flush();
        stream.write(res.getBytes("windows-1251"));
        stream.flush();*/
    }
}
