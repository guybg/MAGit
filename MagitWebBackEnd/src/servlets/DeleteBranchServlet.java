package servlets;

import com.google.gson.Gson;
import com.magit.logic.exceptions.InvalidNameException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.webLogic.users.UserAccount;
import com.magit.webLogic.users.UserManager;
import sun.misc.IOUtils;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

public class DeleteBranchServlet extends HttpServlet {

    private void prepareRedirectAjaxResponse(HttpServletRequest request, HttpServletResponse response, String repoDetails) throws IOException {
        if (isAjax(request)) {
            response.setContentType("application/json");
            try (PrintWriter out = response.getWriter()) {
                out.print(repoDetails);
                out.flush();
            }
        }
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount user = userManager.getUsers().get(usernameFromSession);
        String branchName = request.getParameter("name");
        user.deleteBranch(branchName);
        response.setStatus(200);
    }
}

