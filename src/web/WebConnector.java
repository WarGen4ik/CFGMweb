package web;

import application.View.MainClass;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Admin on 30.06.2017.
 */
@WebServlet("/")
public class WebConnector extends HttpServlet {

    private MainClass gatherer = null;

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
                    response.getWriter().write((coord < 0 ? 0 : coord) + "/" + (gatherer.getMaxCoord() - gatherer.getFirstCoord()));
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

            HashMap<Integer, String> queries = ResultsGetter.getResultQueries();
            if (queries.size() > 0) {
                for (Map.Entry<Integer, String> p : queries.entrySet()) {
                    if (request.getParameter(Integer.toString(p.getKey())) != null) {
                        processRequest(request, response, getShortName(country), p.getValue(), Integer.toString(p.getKey()));
                    } else if (request.getParameter("Delete" + Integer.toString(p.getKey())) != null){
                        new ResultsGetter(getServletContext().getRealPath("")).deleteRows(getShortName(country),p.getValue());
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


    protected void processRequest(HttpServletRequest request, HttpServletResponse response, String country, String query, String num)
            throws ServletException, IOException {
        String fileName = new FileCreator(country, query, getServletContext().getRealPath(""), num).CreateFile();
        String fileType = "text/csv";
        // Find this file id in database to get file name, and file type

        // You must tell the browser the file type you are going to send
        // for example application/pdf, text/plain, text/html, image/jpg
        response.setContentType(fileType);

        // Make sure to show the download dialog
        response.setHeader("Content-disposition","attachment; filename=" + country + num + ".csv");

        // Assume file name is retrieved from database
        // For example D:\\file\\test.pdf

        File my_file = new File(fileName);

        // This should send the file to browser
        OutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream(my_file);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0){
            out.write(buffer, 0, length);
        }
        in.close();
        out.flush();
    }
}
