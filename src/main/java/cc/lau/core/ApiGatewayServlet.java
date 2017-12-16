package cc.lau.core;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ApiGatewayServlet extends HttpServlet {
    ApplicationContext context;
    private ApiGatewayHandler apiGatewayHandler;


    @Override
    public void init() throws ServletException {
        super.init();
        context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        apiGatewayHandler = context.getBean(ApiGatewayHandler.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        apiGatewayHandler.handle(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        apiGatewayHandler.handle(req,resp);
    }
}
