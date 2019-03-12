import Database.ConnectionUtils;
import Utility.MyUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

@WebFilter("/*")
public class JDBCFilter implements Filter {

    public JDBCFilter()
    {
    }
    @Override
    public void init(FilterConfig fConfig) throws ServletException {
    }
    
    @Override
    public void destroy()
    {
    }

    // Kiểm tra mục tiêu của request hiện tại là 1 Servlet?
    private boolean needJDBC(HttpServletRequest request) {
    System.out.println("JDBC Filter");
    // 
    // Servlet Url-pattern: /spath/*
    // 
    // => /spath
    String servletPath = request.getServletPath();
    // => /abc/mnp
    String pathInfo = request.getPathInfo();
    String urlPattern = servletPath;
    if (pathInfo != null) {
        // => /spath/*
        urlPattern = servletPath + "/*";
    }

    // Key: servletName.
    // Value: ServletRegistration
    Map<String, ? extends ServletRegistration> servletRegistrations = request.getServletContext()
        .getServletRegistrations();

    // Tập hợp tất cả các Servlet trong WebApp của bạn.
    Collection<? extends ServletRegistration> values = servletRegistrations.values();
    for (ServletRegistration sr : values) {
        Collection<String> mappings = sr.getMappings();
        if (mappings.contains(urlPattern)) {
            return true;
        }
    }
    return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
         
    HttpServletRequest req = (HttpServletRequest) request;
  
    if (this.needJDBC(req))
    {
        Connection conn = null;
        try {
            conn = ConnectionUtils.getConnection();
            conn.setAutoCommit(false);
            MyUtils.storeConnection(request, conn);

            chain.doFilter(request, response);

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            ConnectionUtils.rollbackQuietly(conn);
            
            request.setAttribute("txtThongBaoLoi","Có lỗi xảy ra!!!<br>"+e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);

            throw new ServletException();
        } finally {
            ConnectionUtils.closeQuietly(conn);
        }
    }
    else {
 
         chain.doFilter(request, response);
        
    } 
        
         
    }
 
    
}
 