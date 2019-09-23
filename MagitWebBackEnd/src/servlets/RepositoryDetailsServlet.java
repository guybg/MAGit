package servlets;

import com.magit.webLogic.users.UserAccount;
import com.magit.webLogic.users.UserManager;
import utils.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RepositoryDetailsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getQueryString();
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        if (null == userManager) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

    }
}
