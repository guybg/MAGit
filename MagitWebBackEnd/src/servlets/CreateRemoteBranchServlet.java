package servlets;

import com.google.gson.Gson;
import com.magit.logic.exceptions.*;
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

public class CreateRemoteBranchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount user = userManager.getUsers().get(usernameFromSession);
        String branchName = request.getParameter("name");
        try {
            user.createRemoteTrackingBranch(branchName);
        } catch (RepositoryNotFoundException | BranchAlreadyExistsException | BranchNotFoundException | InvalidNameException | RemoteReferenceException | IOException e) {
            
        }
    }
}
