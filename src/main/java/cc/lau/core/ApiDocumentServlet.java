package cc.lau.core;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cc on 17/12/18
 */
public class ApiDocumentServlet extends HttpServlet{
    ApplicationContext context;
    private ApiGatewayHandler apiGatewayHandler;


    @Override
    public void init() throws ServletException {
        super.init();
        context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        apiGatewayHandler = context.getBean(ApiGatewayHandler.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {

        List<ApiStore.APIDocument> list = apiGatewayHandler.getApiDocument();

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        StringBuilder result = new StringBuilder();
        result.append("<table border=\"1\"><tr><th>method</th><th>params</th><th>description</th></tr>");

        for (ApiStore.APIDocument apiDocument : list) {
            result.append( "<tr><td>"+apiDocument.getApiName()+"</td><td>"+apiDocument.getParams().replace("\\","")+"</td><td>"+apiDocument.getApiDescription()+"</td></tr>");
        }
        result.append("</table>");

        if (result != null) {
            try {
                response.getWriter().write(result.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req,resp);
    }
}
