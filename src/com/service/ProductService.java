package com.service;

import com.dao.ProductDao;
import com.domain.*;
import com.utils.DataSourceUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by Kuexun on 2018/5/2.
 */
public class ProductService {

    public List<Product> findHotProductList() {
        ProductDao dao = new ProductDao();
        List<Product> productList = null;
        try {
            productList = dao.findHotProductList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    public List<Product> findNewProductList() {
        ProductDao dao = new ProductDao();
        List<Product> productList = null;
        try {
            productList = dao.findNewProductList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    public List<Category> findAllCategory() {
        ProductDao dao = new ProductDao();
        List<Category> categoryList = null;
        try {
            categoryList = dao.findAllCategory();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryList;
    }

    public PageBean<Product> findProductListByCid(String cid,int currentCount,int currentPage) {
        ProductDao dao = new ProductDao();
        PageBean<Product> pageBean = new PageBean<>();
        pageBean.setCurrentPage(currentPage);
        pageBean.setCurrentCount(currentCount);

        int totalCount = 0;
        try {
            totalCount = dao.getCount(cid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        pageBean.setTotalCount(totalCount);

        int totalPage = (int) Math.ceil(1.0*totalCount/currentCount);
        pageBean.setTotalPage(totalPage);

        int index = (currentPage-1)*currentCount;
        List<Product> products = null;
        try {
            products = dao.findProductByPage(cid,index,currentCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        pageBean.setList(products);
        //System.out.println(pageBean);
        return pageBean;
    }

    public Product findProductByPid(String pid) {

        ProductDao dao = new ProductDao();
        Product product = null;
        try {
            product = dao.fingProductByPid(pid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }
    //�ύ����
    public void submitOrder(Order order) {
        ProductDao dao = new ProductDao();
        try {
            //1.��ʼ����
            DataSourceUtils.startTransaction();
            //2.����dao�洢order�����ݵķ���
            dao.addOrders(order);
            //3.����dao�洢orderitem�����ݵķ���
            dao.addOrderItem(order);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                DataSourceUtils.commitAndRelease();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateOrderAdrr(Order order) {

        ProductDao dao = new ProductDao();
        try {
            dao.updateOrderAdrr(order);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Order> findAllOrders(String uid) {
        ProductDao dao = new ProductDao();
        List<Order> orderList = null;
        try {
            orderList = dao.findAllOrders(uid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        System.out.println(orderList);
        return orderList;

    }

    public List<Map<String, Object>> findAllOrderItemByOid(String oid) {
        ProductDao dao = new ProductDao();
        List<Map<String, Object>> orderItemList = null;
        try {
            orderItemList = dao.findAllOrderItemByOid(oid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(orderItemList);
        return orderItemList;
    }
}
