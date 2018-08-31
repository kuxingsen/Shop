package com.service;

import com.dao.UserDao;
import com.domain.User;
import com.sun.org.apache.bcel.internal.classfile.Code;

import java.sql.SQLException;

/**
 * Created by Kuexun on 2018/4/26.
 */
public class UserService {
    public boolean regist(User user) {
        UserDao dao = new UserDao();
        int rows = 0;
        try {
            rows = dao.regist(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows > 0;

    }

    public void active(String activeCode) {
        UserDao dao = new UserDao();
        try {
            dao.active(activeCode);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkUsername(String username) {

        UserDao dao = new UserDao();
        Long num = 0L;
        try {
            num = dao.checkUsername(username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return num > 0;
    }

    //用户登录的方法
    public User login(String username, String password) throws SQLException {
        UserDao dao = new UserDao();
        return dao.login(username,password);
    }

}
