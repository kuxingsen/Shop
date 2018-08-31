package com.dao;

import com.domain.Category;
import com.domain.Order;
import com.domain.OrderItem;
import com.domain.Product;
import com.utils.DataSourceUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by Kuexun on 2018/5/2.
 */
public class ProductDao {
    public List<Product> findHotProductList() throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select * from product where is_hot = ? limit ?,?";
        return runner.query(sql,new BeanListHandler<Product>(Product.class),1,0,9);

    }

    public List<Product> findNewProductList() throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select * from product order by pdate desc limit ?,?";
        return runner.query(sql,new BeanListHandler<Product>(Product.class),0,9);

    }

    public List<Category> findAllCategory() throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select * from category";
        return runner.query(sql,new BeanListHandler<Category>(Category.class));

    }

    public int getCount(String cid) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select count(*) from product where cid=?";
        Long query = (Long)queryRunner.query(sql,new ScalarHandler(),cid);
        return query.intValue();
    }

    public List<Product> findProductByPage(String cid, int index, int currentCount) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select * from product where cid = ? limit ?,?";
        List<Product> products = queryRunner.query(sql, new BeanListHandler<Product>(Product.class), cid, index, currentCount);
        return products;
    }

    public Product fingProductByPid(String pid) throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select * from product where pid=?";
        return runner.query(sql,new BeanHandler<Product>(Product.class),pid);
    }

    public void addOrders(Order order) throws SQLException {
        QueryRunner runner = new QueryRunner();
        String sql = "insert into orders values(?,?,?,?,?,?,?,?)";
        Connection connection = DataSourceUtils.getConnection();
        runner.update(connection,sql,order.getOid(),order.getOrdertime(),order.getTotal(),order.getState(),
                order.getAddr(),order.getName(),order.getTelephone(),order.getUser().getUid()
        );
    }

    public void addOrderItem(Order order) throws SQLException {
        QueryRunner runner = new QueryRunner();
        String sql = "insert into orderitem values(?,?,?,?,?)";
        Connection connection = DataSourceUtils.getConnection();
        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem item : orderItems) {
            runner.update(connection, sql, item.getItemid(), item.getCount(), item.getSubtotal(),
                    item.getProduct().getPid(), item.getOrder().getOid());
        }
    }

    public void updateOrderAdrr(Order order) throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "update orders set address=?,name = ?,telephone=? where oid=?";
        runner.update(sql,order.getAddr(),order.getName(),order.getTelephone(),order.getOid());

    }

    public List<Order> findAllOrders(String uid) throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select * from orders where uid=?";
        return runner.query(sql,new BeanListHandler<>(Order.class),uid);
    }

    public List<Map<String, Object>> findAllOrderItemByOid(String oid) throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select i.count,i.subtotal,p.pimage,p.pname,p.shop_price from orderitem i,product p where i.oid=? and i.pid = p.pid";
        List<Map<String, Object>> mapList = runner.query(sql, new MapListHandler(), oid);
        return mapList;
    }
}
