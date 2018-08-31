package com.web.servlet;

import com.domain.Category;
import com.domain.Product;
import com.service.AdminService;
import com.utils.CommonsUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kuexun on 2018/5/31.
 */
@WebServlet(name = "AdminAddProductServlet", urlPatterns = {"/adminAddProduct"})
public class AdminAddProductServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Ŀ�ģ��ռ��������� ��װһ��Productʵ�� ���ϴ�ͼƬ�浽����������

        Product product = new Product();
        Map<String,Object> map = new HashMap<>();

        try {
            //���������ļ����
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> parseRequest = upload.parseRequest(request);
            for(FileItem item:parseRequest)
            {
                boolean formField = item.isFormField();
                if(formField)
                {
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString("UTF-8");
                    map.put(fieldName,fieldValue);
                }else {
                    String fileName = item.getName();
                    String path = this.getServletContext().getRealPath("upload");
                    System.out.println(path);
                    InputStream in = item.getInputStream();
                    OutputStream out  = new FileOutputStream(path+"/"+fileName);
                    IOUtils.copy(in,out);
                    in.close();
                    out.close();
                    item.delete();

                    map.put("pimage","upload/"+fileName);
                }
            }
            BeanUtils.populate(product,map);
            //δ��װ��ȫ
            product.setPid(CommonsUtils.getUuid());
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//�������ڸ�ʽ
                product.setPdate(df.parse(df.format(new Date())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            product.setPflag(0);
            Category category = new Category();
            category.setCid(map.get("cid").toString());
            product.setCategory(category);

            AdminService service = new AdminService();
            service.saveProcuct(product);
            System.out.println(product);


        } catch (FileUploadException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
