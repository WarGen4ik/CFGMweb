package application.data.gather.Countries;

import java.util.ArrayList;

/**
 * Created by Admin on 30.06.2017.
 */
public class CoordsGatherer {
    private static class Point{
        double x;
        double y;
    }

    public static ArrayList<String> gatherLocations(double lat, double lng, double radius1) {

        ArrayList<String> list = new ArrayList<>();
        Point p = new Point();
        p.x = lat;
        p.y = lng;
        double radius = radius1/1000;

        double interval = (0.01472 * radius/4);

        p.x += interval;
        p.y -= interval;

        double savey = p.y;

        for (int k = 0; k < 2; k++) {
            for (int j = 0; j < 2; j++) {
                list.add(Double.toString(p.x) + ";" + Double.toString(p.y));
                p.y += interval*2;
            }
            p.x -= interval*2;
            p.y = savey;
        }

        return list;
    }
}
