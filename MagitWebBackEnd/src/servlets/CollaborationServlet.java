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
import java.text.ParseException;

public class CollaborationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount user = userManager.getUsers().get(usernameFromSession);
        String action = request.getParameter("action");
        String repositoryId = request.getParameter("repository-id");

        try {
            if(action.equals("push")){
                synchronized (getServletContext()){
                    user.push(repositoryId);
                    try(PrintWriter out = response.getWriter()) {
                        out.write("Pushed successfully!");
                    }
                }
            }else if(action.equals("pull")){
                synchronized (getServletContext()){
                    user.pull(repositoryId);
                    try(PrintWriter out = response.getWriter()) {
                        out.write("pulled successfully!");
                    }
                }
            }else{
                try(PrintWriter out = response.getWriter()) {
                    out.write("Invalid action type.");
                }
            }
        } catch (IOException | ParseException | PreviousCommitsLimitExceededException | CommitNotFoundException | UnhandledMergeException | RepositoryNotFoundException | FastForwardException | MergeException | UncommitedChangesException | RemoteBranchException | PushException | RemoteReferenceException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try(PrintWriter out = response.getWriter()) {
                out.write(e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }catch (MergeNotNeededException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try(PrintWriter out = response.getWriter()) {
                out.write("Local repository is up-to-date.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
