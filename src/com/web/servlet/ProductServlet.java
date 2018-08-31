package com.web.servlet;

import com.domain.*;
import com.google.gson.Gson;
import com.service.ProductService;
import com.utils.CommonsUtils;
import com.utils.JedisPoolUtils;
import com.utils.PaymentUtil;
import org.apache.commons.beanutils.BeanUtils;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.Key;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Kuexun on 2018/5/5.
 */
@WebServlet(name = "ProductServlet",urlPatterns = {"/product"})
public class ProductServlet extends BaseServlet {

	/*public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//���������ĸ�������method
		String methodName = request.getParameter("method");
		if("productList".equals(methodName)){
			productList(request,response);
		}else if("categoryList".equals(methodName)){
			categoryList(request,response);
		}else if("index".equals(methodName)){
			index(request,response);
		}else if("productInfo".equals(methodName)){
			productInfo(request,response);
		}

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	 */

    public void myOrders(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        //�ж��û��Ƿ��Ѿ���½
        User user = (User) session.getAttribute("user");
        ProductService service = new ProductService();
        //��ѯ���û������еĶ�����Ϣ�������ѯorders��)
        //�����е�ÿһ��order����������ǲ�������//ȱ��List<OrderItem>
        List<Order> orderList = service.findAllOrders(user.getUid());
        //ѭ�����еĶ��� Ϊÿ������������еĶ�����
        if(orderList != null)
        {
            for (Order order :
                    orderList) {
                String oid = order.getOid();
                //��װ���Ƕ��������͸ö������е���Ʒ����Ϣ
                List<Map<String, Object>> mapList = service.findAllOrderItemByOid(oid);
                //��mapListת����List<OrderItem>
                for(Map<String,Object> map:mapList) {
                    try {
                        //��map��ȡ��count��subtotal��װ��orderitem��
                        OrderItem item = new OrderItem();
                        BeanUtils.populate(item,map);
                        //��map��ȡ��pimage��pname��shop_price��װ��orderitem��
                        Product product = new Product();
                        BeanUtils.populate(product,map);

                        item.setProduct(product);
                        order.getOrderItems().add(item);

                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                //System.out.println(order);
            }
        }
        System.out.println(orderList);
        //orderList��װ���
        request.setAttribute("orderList",orderList);
        request.getRequestDispatcher("/order_list.jsp").forward(request,response);
    }

    public void confirmOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String[]> properties = request.getParameterMap();
        //�����ջ�����Ϣ
        Order order = new Order();
        try {
            BeanUtils.populate(order,properties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        ProductService service = new ProductService();
        service.updateOrderAdrr(order);
        //����֧��
        String orderid = request.getParameter("oid");
       // String money = order.getTotal()+"";

        String money = "0.1";
        // ����
        String pd_FrpId = request.getParameter("pd_FrpId");

        // ����֧����˾��Ҫ��Щ����
        String p0_Cmd = "Buy";
        String p1_MerId = ResourceBundle.getBundle("merchantInfo").getString("p1_MerId");
        String p2_Order = orderid;
        String p3_Amt = money;
        String p4_Cur = "CNY";
        String p5_Pid = "";
        String p6_Pcat = "";
        String p7_Pdesc = "";
        //֧���ɹ��ص���ַ---������֧����˾����ʡ��û�����
        //������֧�����Է�����ַ
        String p8_Url = ResourceBundle.getBundle("merchantInfo").getString("callback");
        String p9_SAF = "";
        String pa_MP = "";
        String pr_NeedResponse = "1";
        //����hmac ��Ҫ��Կ
        String keyValue = ResourceBundle.getBundle("merchantInfo").getString(
                "keyValue");
        String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt,
                p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc, p8_Url, p9_SAF, pa_MP,
                pd_FrpId, pr_NeedResponse, keyValue);


        String url = "https://www.yeepay.com/app-merchant-proxy/node?pd_FrpId="+pd_FrpId+
                "&p0_Cmd="+p0_Cmd+
                "&p1_MerId="+p1_MerId+
                "&p2_Order="+p2_Order+
                "&p3_Amt="+p3_Amt+
                "&p4_Cur="+p4_Cur+
                "&p5_Pid="+p5_Pid+
                "&p6_Pcat="+p6_Pcat+
                "&p7_Pdesc="+p7_Pdesc+
                "&p8_Url="+p8_Url+
                "&p9_SAF="+p9_SAF+
                "&pa_MP="+pa_MP+
                "&pr_NeedResponse="+pr_NeedResponse+
                "&hmac="+hmac;
        //�ض��򵽵�����֧��ƽ̨
        response.sendRedirect(url);

    }
    public void submitOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        //�ж��û��Ƿ��Ѿ���½

        User user = (User) session.getAttribute("user");
        Order order = new Order();
        //1.private String oid;//�ö����Ķ�����
        String oid = CommonsUtils.getUuid();
        order.setOid(oid);
        //2.private Date ordertime;//�µ�ʱ��
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        Date now = null;
        try {
            now = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        order.setOrdertime(now);
        //3.private double total;//�ö������ܽ��
        Cart cart = (Cart) session.getAttribute("cart");
        double total = cart.getTotal();
        order.setTotal(total);
        //4.private int state;//����֧��״̬ 1�����Ѹ��� 0����δ����
        order.setState(0);
        //5.private String addr;//�ջ���ַ
        order.setAddr(null);
        //6.private String name;//�ջ���
        order.setName(null);
        //7.private String telephone;//�ջ��˵绰
        order.setTelephone(null);
        //8.private User user;//�ö��������ĸ��û�
        order.setUser(user);

        //9.�ö����Ķ����� List<OrderItem> orderItems = new ArrayList<>();
        Map<String,CartItem> cartItems = cart.getCartItems();
        for(Map.Entry<String,CartItem> entry:cartItems.entrySet()){
            CartItem cartItem = entry.getValue();
            OrderItem orderItem = new OrderItem();
            orderItem.setItemid(CommonsUtils.getUuid());
            orderItem.setCount(cartItem.getBuyNum());
            orderItem.setSubtotal(cartItem.getSubtotal());
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setOrder(order);

            order.getOrderItems().add(orderItem);
        }

        ProductService service = new ProductService();
        service.submitOrder(order);

        session.setAttribute("order",order);


        response.sendRedirect(request.getContextPath()+"/order_info.jsp");
    }


    public void clearCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("cart");
        response.sendRedirect(request.getContextPath()+"/cart.jsp");
    }

        public void delProFromCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        String pid = request.getParameter("pid");
        Cart cart = (Cart) session.getAttribute("cart");

        if(cart != null)
        {
            Map<String,CartItem> cartItems = cart.getCartItems();
            cart.setTotal(cart.getTotal()-cartItems.get(pid).getSubtotal());
            cartItems.remove(pid);
            cart.setCartItems(cartItems);

        }
        session.setAttribute("cart",cart);
        response.sendRedirect(request.getContextPath()+"/cart.jsp");
    }
	//����Ʒ��ӵ����ﳵ
    public void addProductToCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        ProductService service = new ProductService();

        String pid = request.getParameter("pid");

        int buyNum = Integer.parseInt(request.getParameter("buyNum"));
        Product product = service.findProductByPid(pid);

        CartItem item = new CartItem();
        item.setBuyNum(buyNum);
        item.setProduct(product);

        Cart cart = (Cart) session.getAttribute("cart");
        if(cart == null)
        {
            cart = new Cart();
        }
        Map<String, CartItem> cartItems = cart.getCartItems();
        if(cartItems.containsKey(pid))
        {
            int oldNum = cartItems.get(pid).getBuyNum();
            cart.setTotal(cart.getTotal()-product.getShop_price()*oldNum);
            buyNum += oldNum;
        }
        double subtotal = product.getShop_price()*buyNum;
        item.setSubtotal(subtotal);
        item.setBuyNum(buyNum);

        cartItems.put(pid,item);

        double cartTotal = cart.getTotal()+item.getSubtotal();
        cart.setTotal(cartTotal);
        session.setAttribute("cart",cart);
        System.out.println(cart);
        //ֱ����ת�����ﳵҳ
        //request.getRequestDispatcher("/cart.jsp").forward(request,response);
        response.sendRedirect(request.getContextPath()+"/cart.jsp");

    }

        //��ʾ��Ʒ�����ĵĹ���
    public void categoryList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductService service = new ProductService();

        //�ȴӻ����в�ѯcategoryList �����ֱ��ʹ�� û���ڴ����ݿ��в�ѯ �浽������
        //1�����jedis���� ����redis���ݿ�
        Jedis jedis = JedisPoolUtils.getJedis();
        String categoryListJson = jedis.get("categoryListJson");
        //2���ж�categoryListJson�Ƿ�Ϊ��
        if(categoryListJson==null){
            System.out.println("����û������ ��ѯ���ݿ�");
            //׼����������
            List<Category> categoryList = service.findAllCategory();
            Gson gson = new Gson();
            categoryListJson = gson.toJson(categoryList);
            jedis.set("categoryListJson", categoryListJson);
        }

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(categoryListJson);
    }

    //��ʾ��ҳ�Ĺ���
    //��ʾ��Ʒ�����ĵĹ���
    public void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductService service = new ProductService();

        //׼��������Ʒ---List<Product>
        List<Product> hotProductList = service.findHotProductList();

        //׼��������Ʒ---List<Product>
        List<Product> newProductList = service.findNewProductList();

        //׼����������
        //List<Category> categoryList = service.findAllCategory();

        //request.setAttribute("categoryList", categoryList);
        request.setAttribute("hotProductList", hotProductList);
        request.setAttribute("newProductList", newProductList);

        request.getRequestDispatcher("/index.jsp").forward(request, response);

    }

    //��ʾ��Ʒ����ϸ��Ϣ����
    public void productInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //��õ�ǰҳ
        String currentPage = request.getParameter("currentPage");
        //�����Ʒ���
        String cid = request.getParameter("cid");

        //���Ҫ��ѯ����Ʒ��pid
        String pid = request.getParameter("pid");

        ProductService service = new ProductService();
        Product product = service.findProductByPid(pid);

        request.setAttribute("product", product);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("cid", cid);


        //��ÿͻ���Я��cookie---���������pids��cookie
        String pids = pid;
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for(Cookie cookie : cookies){
                if("pids".equals(cookie.getName())){
                    pids = cookie.getValue();
                    //1-3-2 ���η�����Ʒpid��8----->8-1-3-2
                    //1-3-2 ���η�����Ʒpid��3----->3-1-2
                    //1-3-2 ���η�����Ʒpid��2----->2-1-3
                    //��pids���һ������
                    String[] split = pids.split("-");//{3,1,2}
                    List<String> asList = Arrays.asList(split);//[3,1,2]
                    LinkedList<String> list = new LinkedList<String>(asList);//[3,1,2]
                    //�жϼ������Ƿ���ڵ�ǰpid
                    if(list.contains(pid)){
                        //������ǰ�鿴��Ʒ��pid
                        list.remove(pid);
                        list.addFirst(pid);
                    }else{
                        //��������ǰ�鿴��Ʒ��pid ֱ�ӽ���pid�ŵ�ͷ��
                        list.addFirst(pid);
                    }
                    //��[3,1,2]ת��3-1-2�ַ���
                    StringBuffer sb = new StringBuffer();
                    for(int i=0;i<list.size()&&i<7;i++){
                        sb.append(list.get(i));
                        sb.append("-");//3-1-2-
                    }
                    //ȥ��3-1-2-���-
                    pids = sb.substring(0, sb.length()-1);
                }
            }
        }


        Cookie cookie_pids = new Cookie("pids",pids);
        response.addCookie(cookie_pids);

        request.getRequestDispatcher("/product_info.jsp").forward(request, response);

    }

    //������Ʒ���������Ʒ���б�
    public void productListByCid(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //���cid
        String cid = request.getParameter("cid");

        String currentPageStr = request.getParameter("currentPage");
        if(currentPageStr==null) currentPageStr="1";
        int currentPage = Integer.parseInt(currentPageStr);
        int currentCount = 12;

        ProductService service = new ProductService();
        PageBean pageBean = service.findProductListByCid(cid,currentCount,currentPage);

        request.setAttribute("pageBean", pageBean);
        request.setAttribute("cid", cid);

        //����һ����¼��ʷ��Ʒ��Ϣ�ļ���
        List<Product> historyProductList = new ArrayList<Product>();

        //��ÿͻ���Я�����ֽ�pids��cookie
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for(Cookie cookie:cookies){
                if("pids".equals(cookie.getName())){
                    String pids = cookie.getValue();//3-2-1
                    String[] split = pids.split("-");
                    for(String pid : split){
                        Product pro = service.findProductByPid(pid);
                        historyProductList.add(pro);
                    }
                }
            }
        }

        //����ʷ��¼�ļ��Ϸŵ�����
        request.setAttribute("historyProductList", historyProductList);

        request.getRequestDispatcher("/product_list.jsp").forward(request, response);


    }

}