package demoapp2;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String email = req.getParameter("email").trim();
        String password = req.getParameter("password").trim();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Women", "root", "Trisha");
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (storedPassword.equals(password)) {
                    
                    HttpSession session = req.getSession();
                    session.setAttribute("email", email);
                    
                    System.out.println("Login successful for: " + email);
                    res.sendRedirect("dashboard.html");
                } else {
                    res.sendRedirect("login.html?msg=" + URLEncoder.encode("Incorrect password", "UTF-8"));
                }
            } else {
                res.sendRedirect("login.html?msg=" + URLEncoder.encode("User does not exist. Please register.", "UTF-8"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            res.sendRedirect("login.html?msg=" + URLEncoder.encode("Server error", "UTF-8"));
        }
    }
}