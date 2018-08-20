package com.ys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Struct;
import java.util.Random;

@WebServlet(name = "SecKillServlet",urlPatterns = "/doseckill")
public class SecKillServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userid = new Random(50000) + "";//随机生成客户编号

        String prodid = request.getParameter("prodid");

        boolean if_success = SecKill_redisByScript.doSecKill(userid,prodid);
        response.getWriter().print(if_success);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
}
