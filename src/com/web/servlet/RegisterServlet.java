package com.web.servlet;

import com.domain.User;
import com.service.UserService;
import com.utils.CommonsUtils;
import com.utils.MailUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import javax.mail.MessagingException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.SimpleFormatter;

/**
 *
 * Created by Kuexun on 2018/4/26.
 */
@WebServlet(name="Register",urlPatterns = {"/register"})
public class RegisterServlet extends javax.servlet.http.HttpServlet {
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        Map<String, String[]> properties = request.getParameterMap();
        User user = new User();
        try {
            //转换String为date格式
            ConvertUtils.register(new Converter() {
                @Override
                public Object convert(Class aClass, Object o) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
                    Date parse = null;
                    try {
                        parse = format.parse(o.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return parse;
                }
            }, Date.class);

            BeanUtils.populate(user, properties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        //设置uid
        user.setUid(CommonsUtils.getUuid());
        //设置telephone
        user.setTelephone(null);
        //设置时候激活
        user.setState(0);
        //设置激活码
        String activeCode = CommonsUtils.getUuid();
        user.setCode(activeCode);

        //将user传递给service
        UserService service = new UserService();
        boolean isRegisterSuccess = service.regist(user);
        if(isRegisterSuccess)
        {
            String msg = "恭喜注册成功，点击激活！<a href='http://localhost:8080/active?activeCode="+activeCode+"'>" +
                    "http://localhost:8080/active?activeCode="+activeCode+"</>";
            try {
                MailUtils.sendMail(user.getEmail(),msg);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            //跳转到成功页面
            response.sendRedirect(request.getContextPath()+"/registerSuccess.jsp");
        }else {
            response.sendRedirect(request.getContextPath()+"/registerFail.jsp");
        }
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {

    }
}
