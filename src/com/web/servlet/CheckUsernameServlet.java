package com.web.servlet;

import com.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Kuexun on 2018/4/28.
 */
@WebServlet(name = "CheckUsernameServlet",urlPatterns = {"/checkUsername"})
public class CheckUsernameServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //获取用户名
        String username=request.getParameter("username");
        UserService service = new UserService();
        boolean isExist = service.checkUsername(username);
        String json="{\"isExist\":"+isExist+"}";
       // System.out.println(json);
        response.getWriter().write(json);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
