package demoapp2;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class FetchMessagesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Update DB credentials as needed
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Women";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Trisha"; // change it

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            String sql = "SELECT sender_email, message, timestamp FROM chat_messages ORDER BY timestamp ASC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            StringBuilder json = new StringBuilder();
            json.append("[");

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{");
                json.append("\"email\":\"").append(rs.getString("sender_email")).append("\",");
                json.append("\"message\":\"").append(rs.getString("message")).append("\",");
                json.append("\"time\":\"").append(rs.getString("timestamp")).append("\"");
                json.append("}");
                first = false;
            }

            json.append("]");

            out.print(json.toString());

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.print("[]"); // fallback if error
        }

        out.flush();
    }
}
