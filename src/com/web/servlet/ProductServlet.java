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

		//获得请求的哪个方法的method
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
        //判断用户是否已经登陆
        User user = (User) session.getAttribute("user");
        ProductService service = new ProductService();
        //查询该用户的所有的订单信息（单表查询orders表)
        //集合中的每一个order对象的数据是不完整的//缺少List<OrderItem>
        List<Order> orderList = service.findAllOrders(user.getUid());
        //循环所有的订单 为每个订单填充所有的订单项
        if(orderList != null)
        {
            for (Order order :
                    orderList) {
                String oid = order.getOid();
                //封装的是多个订单项和该订单项中的商品的信息
                List<Map<String, Object>> mapList = service.findAllOrderItemByOid(oid);
                //将mapList转换成List<OrderItem>
                for(Map<String,Object> map:mapList) {
                    try {
                        //从map中取出count、subtotal封装到orderitem中
                        OrderItem item = new OrderItem();
                        BeanUtils.populate(item,map);
                        //从map中取出pimage、pname、shop_price封装到orderitem中
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
        //orderList封装完成
        request.setAttribute("orderList",orderList);
        request.getRequestDispatcher("/order_list.jsp").forward(request,response);
    }

    public void confirmOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String[]> properties = request.getParameterMap();
        //更新收货人信息
        Order order = new Order();
        try {
            BeanUtils.populate(order,properties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        ProductService service = new ProductService();
        service.updateOrderAdrr(order);
        //在线支付
        String orderid = request.getParameter("oid");
       // String money = order.getTotal()+"";

        String money = "0.1";
        // 银行
        String pd_FrpId = request.getParameter("pd_FrpId");

        // 发给支付公司需要哪些数据
        String p0_Cmd = "Buy";
        String p1_MerId = ResourceBundle.getBundle("merchantInfo").getString("p1_MerId");
        String p2_Order = orderid;
        String p3_Amt = money;
        String p4_Cur = "CNY";
        String p5_Pid = "";
        String p6_Pcat = "";
        String p7_Pdesc = "";
        //支付成功回调地址---第三方支付公司会访问、用户访问
        //第三方支付可以访问网址
        String p8_Url = ResourceBundle.getBundle("merchantInfo").getString("callback");
        String p9_SAF = "";
        String pa_MP = "";
        String pr_NeedResponse = "1";
        //加密hmac 需要密钥
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
        //重定向到第三方支付平台
        response.sendRedirect(url);

    }
    public void submitOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        //判断用户是否已经登陆

        User user = (User) session.getAttribute("user");
        Order order = new Order();
        //1.private String oid;//该订单的订单号
        String oid = CommonsUtils.getUuid();
        order.setOid(oid);
        //2.private Date ordertime;//下单时间
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
        //3.private double total;//该订单的总金额
        Cart cart = (Cart) session.getAttribute("cart");
        double total = cart.getTotal();
        order.setTotal(total);
        //4.private int state;//订单支付状态 1代表已付款 0代表未付款
        order.setState(0);
        //5.private String addr;//收货地址
        order.setAddr(null);
        //6.private String name;//收货人
        order.setName(null);
        //7.private String telephone;//收货人电话
        order.setTelephone(null);
        //8.private User user;//该订单属于哪个用户
        order.setUser(user);

        //9.该订单的订单项 List<OrderItem> orderItems = new ArrayList<>();
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
	//将商品添加道购物车
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
        //直接跳转到购物车页
        //request.getRequestDispatcher("/cart.jsp").forward(request,response);
        response.sendRedirect(request.getContextPath()+"/cart.jsp");

    }

        //显示商品的类别的的功能
    public void categoryList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductService service = new ProductService();

        //先从缓存中查询categoryList 如果有直接使用 没有在从数据库中查询 存到缓存中
        //1、获得jedis对象 连接redis数据库
        Jedis jedis = JedisPoolUtils.getJedis();
        String categoryListJson = jedis.get("categoryListJson");
        //2、判断categoryListJson是否为空
        if(categoryListJson==null){
            System.out.println("缓存没有数据 查询数据库");
            //准备分类数据
            List<Category> categoryList = service.findAllCategory();
            Gson gson = new Gson();
            categoryListJson = gson.toJson(categoryList);
            jedis.set("categoryListJson", categoryListJson);
        }

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(categoryListJson);
    }

    //显示首页的功能
    //显示商品的类别的的功能
    public void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductService service = new ProductService();

        //准备热门商品---List<Product>
        List<Product> hotProductList = service.findHotProductList();

        //准备最新商品---List<Product>
        List<Product> newProductList = service.findNewProductList();

        //准备分类数据
        //List<Category> categoryList = service.findAllCategory();

        //request.setAttribute("categoryList", categoryList);
        request.setAttribute("hotProductList", hotProductList);
        request.setAttribute("newProductList", newProductList);

        request.getRequestDispatcher("/index.jsp").forward(request, response);

    }

    //显示商品的详细信息功能
    public void productInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //获得当前页
        String currentPage = request.getParameter("currentPage");
        //获得商品类别
        String cid = request.getParameter("cid");

        //获得要查询的商品的pid
        String pid = request.getParameter("pid");

        ProductService service = new ProductService();
        Product product = service.findProductByPid(pid);

        request.setAttribute("product", product);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("cid", cid);


        //获得客户端携带cookie---获得名字是pids的cookie
        String pids = pid;
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for(Cookie cookie : cookies){
                if("pids".equals(cookie.getName())){
                    pids = cookie.getValue();
                    //1-3-2 本次访问商品pid是8----->8-1-3-2
                    //1-3-2 本次访问商品pid是3----->3-1-2
                    //1-3-2 本次访问商品pid是2----->2-1-3
                    //将pids拆成一个数组
                    String[] split = pids.split("-");//{3,1,2}
                    List<String> asList = Arrays.asList(split);//[3,1,2]
                    LinkedList<String> list = new LinkedList<String>(asList);//[3,1,2]
                    //判断集合中是否存在当前pid
                    if(list.contains(pid)){
                        //包含当前查看商品的pid
                        list.remove(pid);
                        list.addFirst(pid);
                    }else{
                        //不包含当前查看商品的pid 直接将该pid放到头上
                        list.addFirst(pid);
                    }
                    //将[3,1,2]转成3-1-2字符串
                    StringBuffer sb = new StringBuffer();
                    for(int i=0;i<list.size()&&i<7;i++){
                        sb.append(list.get(i));
                        sb.append("-");//3-1-2-
                    }
                    //去掉3-1-2-后的-
                    pids = sb.substring(0, sb.length()-1);
                }
            }
        }


        Cookie cookie_pids = new Cookie("pids",pids);
        response.addCookie(cookie_pids);

        request.getRequestDispatcher("/product_info.jsp").forward(request, response);

    }

    //根据商品的类别获得商品的列表
    public void productListByCid(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //获得cid
        String cid = request.getParameter("cid");

        String currentPageStr = request.getParameter("currentPage");
        if(currentPageStr==null) currentPageStr="1";
        int currentPage = Integer.parseInt(currentPageStr);
        int currentCount = 12;

        ProductService service = new ProductService();
        PageBean pageBean = service.findProductListByCid(cid,currentCount,currentPage);

        request.setAttribute("pageBean", pageBean);
        request.setAttribute("cid", cid);

        //定义一个记录历史商品信息的集合
        List<Product> historyProductList = new ArrayList<Product>();

        //获得客户端携带名字叫pids的cookie
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

        //将历史记录的集合放到域中
        request.setAttribute("historyProductList", historyProductList);

        request.getRequestDispatcher("/product_list.jsp").forward(request, response);


    }

}