package demoapp2;



import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class SendMessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Change DB credentials as per your setup
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Women";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Trisha"; // replace with actual password

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String senderEmail = request.getParameter("email");
        String message = request.getParameter("message");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        System.out.println("Received email: " + senderEmail);
        System.out.println("Received message: " + message);
        if (senderEmail == null || message == null || senderEmail.trim().isEmpty() || message.trim().isEmpty()) {
            System.out.println("Error: email or message is null/empty");
            out.print("{\"status\":\"fail\", \"reason\":\"empty params\"}");
            return;
        }

        

        try {
            // Load JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            String sql = "INSERT INTO chat_messages (sender_email, message) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, senderEmail);
            stmt.setString(2, message);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                out.print("{\"status\":\"success\"}");
            } else {
                out.print("{\"status\":\"fail\"}");
            }

            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\"}");
        }

        out.flush();
    }
}

