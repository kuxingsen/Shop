package com.dao;

import com.domain.User;
import com.utils.DataSourceUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;

/**
 * Created by Kuexun on 2018/4/26.
 */
public class UserDao {

    public int regist(User user) throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "insert into user values(?,?,?,?,?,?,?,?,?,?)";
        int update = 0;
        update = runner.update(sql, user.getUid(), user.getUsername(), user.getPassword(),
                user.getName(), user.getEmail(), user.getTelephone(),
                user.getBirthday(), user.getSex(), user.getState(),
                user.getCode()
        );
        return update;
    }

    public int active(String activeCode) throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "update user set state=? where code=?";
        int update = 0;
        update = runner.update(sql,1,activeCode);
        return update;
    }

    public Long checkUsername(String username) throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select count(*) from user where username=?";
        Long query = null;
        query = (Long)runner.query(sql, new ScalarHandler(), username);
        return query;
    }

    //用户登录的方法
    public User login(String username, String password) throws SQLException {
        QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
        String sql = "select * from user where username=? and password=?";
        return runner.query(sql, new BeanHandler<>(User.class), username,password);
    }
}
