package demoapp2;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

    
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String gender = req.getParameter("gender");
        String role = req.getParameter("role");
        String location = req.getParameter("location");

       
        String jdbcURL = "jdbc:mysql://localhost:3306/Women";
        String jdbcUsername = "root";         // Your MySQL username
        String jdbcPassword = "Trisha";       // Your MySQL password

        try {
            
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded successfully.");

            
            Connection con = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);

            
            PreparedStatement checkStmt = con.prepareStatement("SELECT * FROM users WHERE email = ?");
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                out.println("<script>alert('User already registered with this email. Please login.'); window.location='login.html';</script>");
            } else {
                // Insert user into database
                PreparedStatement insertStmt = con.prepareStatement(
                    "INSERT INTO users (name, email, password, gender, role, location) VALUES (?, ?, ?, ?, ?, ?)"
                );

                insertStmt.setString(1, name);
                insertStmt.setString(2, email);
                insertStmt.setString(3, password);
                insertStmt.setString(4, gender);
                insertStmt.setString(5, role);
                insertStmt.setString(6, location);

                insertStmt.executeUpdate();

                out.println("<script>alert('Registration successful! Please login.'); window.location='login.html';</script>");

                insertStmt.close();
            }

            rs.close();
            checkStmt.close();
            con.close();

        } catch (ClassNotFoundException e) {
            out.println("<h3>MySQL Driver not found: " + e.getMessage() + "</h3>");
            e.printStackTrace();
        } catch (SQLException e) {
            out.println("<h3>Database error: " + e.getMessage() + "</h3>");
            e.printStackTrace();
        } catch (Exception e) {
            out.println("<h3>Unexpected error: " + e.getMessage() + "</h3>");
            e.printStackTrace();
        }
    }
}
