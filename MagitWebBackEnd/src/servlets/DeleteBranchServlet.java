package servlets;

import com.google.gson.Gson;
import com.magit.logic.exceptions.*;
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount user = userManager.getUsers().get(usernameFromSession);
        String branchName = request.getParameter("name");
        String id = request.getParameter("id");
        try {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            user.deleteBranch(branchName, id);
        } catch (BranchDeletedRemotelyException e){
            String remoteUserName = user.getRepositories().get(id).get("remoteUser");
            UserAccount receiverUserAccount = userManager.getUsers().get(remoteUserName);
            receiverUserAccount.addNotification(usernameFromSession, "I deleted your branch named " + e.getmBranchName()+".");
        }
        catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write(ex.getMessage());
            } catch (IOException ignored) {
            }
        }
    }
}

