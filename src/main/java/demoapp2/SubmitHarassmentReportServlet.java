package demoapp2;

import java.io.*;
import java.sql.*;
import java.util.UUID;
import java.sql.Types;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@MultipartConfig(
    maxFileSize = 52428800,      // 50MB
    maxRequestSize = 104857600,  // 100MB  
    fileSizeThreshold = 1048576  // 1MB
)
public class SubmitHarassmentReportServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Women";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Trisha"; 

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set CORS headers for AJAX requests
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement getUserStmt = null;
        ResultSet userResult = null;
        ResultSet generatedKeys = null;
        
        try {
            System.out.println("=== Harassment Report Servlet Called ===");
            
            // Get user session - TEMPORARILY BYPASS FOR TESTING
            HttpSession session = request.getSession();
            String userEmail = (String) session.getAttribute("email");
            
            // TEMPORARY FIX: Set test email if no session (remove this in production)
            if (userEmail == null) {
                userEmail = "test@example.com";
                session.setAttribute("email", userEmail);
                System.out.println("TEMP: Set test email for debugging");
            }
            
            System.out.println("User email from session: " + userEmail);

            // Get form parameters
            String reportType = request.getParameter("reportType");
            String reporterName = request.getParameter("reporterName");
            String contactNumber = request.getParameter("contactNumber");
            String email = request.getParameter("email");
            String ageStr = request.getParameter("age");
            String address = request.getParameter("address");
            String incidentDate = request.getParameter("incidentDate");
            String incidentTime = request.getParameter("incidentTime");
            String incidentLocation = request.getParameter("incidentLocation");
            String incidentDescription = request.getParameter("incidentDescription");
            String perpetratorDetails = request.getParameter("perpetratorDetails");
            String witnesses = request.getParameter("witnesses");
            String isAnonymousStr = request.getParameter("isAnonymous");
            String latitudeStr = request.getParameter("latitude");
            String longitudeStr = request.getParameter("longitude");
            String policeStation = request.getParameter("policeStation");

            System.out.println("Report Type: " + reportType);
            System.out.println("Incident Location: " + incidentLocation);
            System.out.println("Incident Description: " + incidentDescription);

            // Validate required fields
            if (incidentDescription == null || incidentDescription.trim().isEmpty()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Incident description is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            if (incidentLocation == null || incidentLocation.trim().isEmpty()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Incident location is required");
                out.print(jsonResponse.toString());
                return;
            }

            // Parse optional fields
            Integer age = null;
            if (ageStr != null && !ageStr.trim().isEmpty()) {
                try {
                    age = Integer.parseInt(ageStr);
                } catch (NumberFormatException e) {
                    System.out.println("Age parsing failed: " + ageStr);
                }
            }

            boolean isAnonymous = "true".equals(isAnonymousStr);
            Double latitude = null, longitude = null;
            
            if (latitudeStr != null && !latitudeStr.trim().isEmpty()) {
                try {
                    latitude = Double.parseDouble(latitudeStr);
                } catch (NumberFormatException e) {
                    System.out.println("Latitude parsing failed: " + latitudeStr);
                }
            }
            
            if (longitudeStr != null && !longitudeStr.trim().isEmpty()) {
                try {
                    longitude = Double.parseDouble(longitudeStr);
                } catch (NumberFormatException e) {
                    System.out.println("Longitude parsing failed: " + longitudeStr);
                }
            }

            // Generate unique case number
            String caseNumber = "WS" + System.currentTimeMillis() + 
                              UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            System.out.println("Generated case number: " + caseNumber);

            // Database connection and insertion
            System.out.println("Connecting to database...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connected successfully");

            // Get user ID from users table (create user if doesn't exist)
            Integer userId = null;
            String getUserIdSql = "SELECT id FROM users WHERE email = ?";
            getUserStmt = conn.prepareStatement(getUserIdSql);
            getUserStmt.setString(1, userEmail);
            userResult = getUserStmt.executeQuery();
            
            if (userResult.next()) {
                userId = userResult.getInt("id");
                System.out.println("Found existing user ID: " + userId);
            } else {
                // Create new user if doesn't exist
                String createUserSql = "INSERT INTO users (email, name, created_at) VALUES (?, ?, NOW())";
                PreparedStatement createUserStmt = conn.prepareStatement(createUserSql, Statement.RETURN_GENERATED_KEYS);
                createUserStmt.setString(1, userEmail);
                createUserStmt.setString(2, reporterName != null ? reporterName : "User");
                createUserStmt.executeUpdate();
                
                ResultSet userKeys = createUserStmt.getGeneratedKeys();
                if (userKeys.next()) {
                    userId = userKeys.getInt(1);
                    System.out.println("Created new user with ID: " + userId);
                }
                userKeys.close();
                createUserStmt.close();
            }

            // Insert harassment report
            String insertSql = "INSERT INTO harassment_reports " +
                "(user_id, report_type, reporter_name, contact_number, email, age, address, " +
                "incident_date, incident_time, incident_location, incident_description, " +
                "perpetrator_details, witnesses, police_station, case_number, is_anonymous, " +
                "latitude, longitude, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

            pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setObject(1, userId);
            pstmt.setString(2, reportType != null ? reportType : "General");
            pstmt.setString(3, isAnonymous ? "Anonymous" : reporterName);
            pstmt.setString(4, isAnonymous ? null : contactNumber);
            pstmt.setString(5, isAnonymous ? null : email);
            pstmt.setObject(6, age);
            pstmt.setString(7, isAnonymous ? null : address);
            
            // Handle date
            if (incidentDate != null && !incidentDate.trim().isEmpty()) {
                pstmt.setDate(8, Date.valueOf(incidentDate));
            } else {
                pstmt.setNull(8, Types.DATE);
            }
            
            // Handle time
            if (incidentTime != null && !incidentTime.trim().isEmpty()) {
                try {
                    pstmt.setTime(9, Time.valueOf(incidentTime + ":00"));
                } catch (IllegalArgumentException e) {
                    System.out.println("Time parsing failed: " + incidentTime);
                    pstmt.setNull(9, Types.TIME);
                }
            } else {
                pstmt.setNull(9, Types.TIME);
            }
            
            pstmt.setString(10, incidentLocation);
            pstmt.setString(11, incidentDescription);
            pstmt.setString(12, perpetratorDetails);
            pstmt.setString(13, witnesses);
            pstmt.setString(14, policeStation);
            pstmt.setString(15, caseNumber);
            pstmt.setBoolean(16, isAnonymous);
            pstmt.setObject(17, latitude);
            pstmt.setObject(18, longitude);

            System.out.println("Executing database insert...");
            int result = pstmt.executeUpdate();

            if (result > 0) {
                // Get the generated report ID
                generatedKeys = pstmt.getGeneratedKeys();
                int reportId = 0;
                if (generatedKeys.next()) {
                    reportId = generatedKeys.getInt(1);
                }

                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Report submitted successfully");
                jsonResponse.addProperty("caseNumber", caseNumber);
                jsonResponse.addProperty("reportId", reportId);
                
                System.out.println("Report submitted successfully: Case #" + caseNumber + 
                                 " by " + (isAnonymous ? "Anonymous" : userEmail));
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Failed to submit report");
                System.out.println("Database insert failed");
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error: " + e.getMessage());
            System.out.println("SQL Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Server error: " + e.getMessage());
        } finally {
            // Close all database resources
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (userResult != null) userResult.close();
                if (getUserStmt != null) getUserStmt.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing database resources: " + e.getMessage());
            }
        }

        out.print(jsonResponse.toString());
        out.flush();
    }
    
    // Handle OPTIONS requests for CORS
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}