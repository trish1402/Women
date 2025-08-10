package demoapp2;

import java.io.*;
import java.sql.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;


public class GetNearestPoliceStationServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Women";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Trisha";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        
        try {
            String latStr = request.getParameter("latitude");
            String lngStr = request.getParameter("longitude");
            
            if (latStr == null || lngStr == null) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Location coordinates required");
                out.print(jsonResponse.toString());
                return;
            }

            double userLat = Double.parseDouble(latStr);
            double userLng = Double.parseDouble(lngStr);

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            
            String sql = "SELECT station_id, station_name, address, contact_number, latitude, longitude, city, state, " +
                        "(6371 * acos(cos(radians(?)) * cos(radians(latitude)) * " +
                        "cos(radians(longitude) - radians(?)) + sin(radians(?)) * " +
                        "sin(radians(latitude)))) AS distance " +
                        "FROM police_stations " +
                        "ORDER BY distance LIMIT 5";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, userLat);
            pstmt.setDouble(2, userLng);
            pstmt.setDouble(3, userLat);

            ResultSet rs = pstmt.executeQuery();

            JsonArray stations = new JsonArray();
            while (rs.next()) {
                JsonObject station = new JsonObject();
                station.addProperty("stationId", rs.getInt("station_id"));
                station.addProperty("name", rs.getString("station_name"));
                station.addProperty("address", rs.getString("address"));
                station.addProperty("contact", rs.getString("contact_number"));
                station.addProperty("city", rs.getString("city"));
                station.addProperty("state", rs.getString("state"));
                station.addProperty("latitude", rs.getDouble("latitude"));
                station.addProperty("longitude", rs.getDouble("longitude"));
                station.addProperty("distance", Math.round(rs.getDouble("distance") * 100.0) / 100.0);
                stations.add(station);
            }

            jsonResponse.addProperty("status", "success");
            jsonResponse.add("stations", stations);

            rs.close();
            pstmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Error finding police stations: " + e.getMessage());
        }

        out.print(jsonResponse.toString());
    }
}
