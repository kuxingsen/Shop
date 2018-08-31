package com.web.servlet;

import com.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Kuexun on 2018/4/27.
 */
@WebServlet(name = "ActiveServlet",urlPatterns = {"/active"})
public class ActiveServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String activeCode = request.getParameter("activeCode");
        UserService service = new UserService();
        service.active(activeCode);
        response.sendRedirect(request.getContextPath()+"/login.jsp");
    }
}
