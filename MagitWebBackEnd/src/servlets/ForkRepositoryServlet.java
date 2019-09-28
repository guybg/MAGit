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

public class ForkRepositoryServlet extends HttpServlet {
    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request,response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String fromUserName = request.getParameter("userName");
        String repositoryIdToFork = request.getParameter("repositoryToFork");
        String cloneName = request.getParameter("repositoryName");
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount currentAccount;
        UserAccount hisAccount;
        String message = "";
        response.setContentType("text/html");
        synchronized (this) {
            currentAccount = userManager.getUsers().get(usernameFromSession);
            hisAccount = userManager.getUsers().get(fromUserName);
            try {
                currentAccount.cloneRepository(hisAccount, repositoryIdToFork, cloneName);
                hisAccount.addNotification(usernameFromSession, "I cloned your repository named: " + hisAccount.getRepositories().get(repositoryIdToFork).get("name") + " with id: " + repositoryIdToFork);
            } catch (CloneException | InvalidNameException | IllegalPathException | PreviousCommitsLimitExceededException | RepositoryNotFoundException e) {//handle messages there todo
                message = e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        try (PrintWriter out = response.getWriter()) {
            out.println(message);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
