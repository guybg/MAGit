package servlets;

import com.google.gson.Gson;
import com.magit.logic.exceptions.BranchAlreadyExistsException;
import com.magit.logic.exceptions.InvalidNameException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.webLogic.users.UserAccount;
import com.magit.webLogic.users.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class CreateBranchServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount user = userManager.getUsers().get(usernameFromSession);
        String branchName = request.getParameter("branchName");
        String id = request.getParameter("id");
        try {
            Gson gson = new Gson();
            String branchInfo = gson.toJson(user.createBranch(branchName,id));
            try (PrintWriter out = response.getWriter()) {
                out.write(branchInfo);
                out.flush();
            }
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (BranchAlreadyExistsException | InvalidNameException | RepositoryNotFoundException | IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write(e.getMessage());
            } catch (IOException ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                ex.printStackTrace();
            }
        }
    }
}
