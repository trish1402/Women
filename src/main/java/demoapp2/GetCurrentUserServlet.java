package demoapp2;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


public class GetCurrentUserServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("email") != null) {
            String email = (String) session.getAttribute("email");
            out.print("{\"email\":\"" + email + "\"}");
        } else {
            out.print("{\"error\":\"not logged in\"}");
        }
        
        out.flush();
    }
}
