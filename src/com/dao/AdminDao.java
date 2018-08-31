package com.dao;

import com.domain.Category;
import com.domain.Order;
import com.domain.Product;
import com.utils.DataSourceUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by Kuexun on 2018/5/29.
 */
public class AdminDao {
    public List<Category> findAllCategory() throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select * from category";
        List<Category> categoryList = runner.query(sql, new BeanListHandler<Category>(Category.class));
        return categoryList;
    }

    public void saveProcuct(Product product) throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "insert into product values(?,?,?,?,?,?,?,?,?,?)";
        runner.update(sql, product.getPid(),product.getPname(),product.getMarket_price(),
                product.getShop_price(),product.getPimage(),product.getPdate(),
                product.getIs_hot(),product.getPdesc(),product.getPflag(),product.getCategory().getCid());
    }

    public List<Order> findAllOrders() throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select * from orders";
        List<Order> orderList = runner.query(sql, new BeanListHandler<>(Order.class));
        return orderList;
    }

    public List<Map<String, Object>> findOrderInfoByOid(String oid) throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select p.pimage,p.pname,p.shop_price,i.count,i.subtotal "+
                " from orderitem i,product p "+
                " where i.pid=p.pid and i.oid=? ";
        return runner.query(sql, new MapListHandler(), oid);
    }
}
