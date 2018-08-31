package com.web.servlet;

import com.domain.Category;
import com.domain.Order;
import com.domain.OrderItem;
import com.domain.Product;
import com.google.gson.Gson;
import com.service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Kuexun on 2018/5/29.
 */
@WebServlet(name = "AdminServlet",urlPatterns = {"/admin"})
public class AdminServlet extends BaseServlet {
    public void findOrderInfoByOid(HttpServletRequest request, HttpServletResponse response) {
        String oid = request.getParameter("oid");

        AdminService service = new AdminService();
        List<Map<String,Object>> mapList = service.findOrderInfoByOid(oid);
        Gson gson = new Gson();
        String json = gson.toJson(mapList);
        System.out.println(json);
        response.setContentType("text/html;charset=UTF-8");
        try {
            response.getWriter().write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void findAllOrders(HttpServletRequest request, HttpServletResponse response){

        AdminService service = new AdminService();
        List<Order> orderList = service.findAllOrders();

        request.setAttribute("orderList",orderList);
        try {
            request.getRequestDispatcher("/admin/order/list.jsp").forward(request,response);
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
    }
    public void findAllCategory(HttpServletRequest request, HttpServletResponse response)
    {
        //提供一个List<Category>转成json字符串
        AdminService service = new AdminService();
        List<Category> categoryList = service.findAddCategory();

        Gson gson = new Gson();
        String json = gson.toJson(categoryList);
        response.setContentType("text/json;charset=utf-8");
        System.out.println(json);
        try {
            response.getWriter().write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
