package com.service;

import com.dao.AdminDao;
import com.domain.Category;
import com.domain.Order;
import com.domain.Product;
import com.sun.org.apache.xpath.internal.operations.Or;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by Kuexun on 2018/5/29.
 */
public class AdminService {

    public List<Category> findAddCategory() {
        AdminDao dao = new AdminDao();
        List<Category> categoryList = null;
        try {
            categoryList = dao.findAllCategory();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryList;
    }

    public void saveProcuct(Product product) {
        AdminDao dao = new AdminDao();
        try {
            dao.saveProcuct(product);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Order> findAllOrders() {

        AdminDao dao = new AdminDao();
        List<Order> orderList = null;
        try {
            orderList = dao.findAllOrders();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderList;
    }

    public List<Map<String, Object>> findOrderInfoByOid(String oid) {

        AdminDao dao = new AdminDao();
        List<Map<String, Object>> mapList = null;
        try {
            mapList = dao.findOrderInfoByOid(oid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mapList;
    }
}
