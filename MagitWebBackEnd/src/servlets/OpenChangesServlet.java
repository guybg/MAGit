package servlets;

import com.google.gson.Gson;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.utils.jstree.JsTreeItem;
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
import java.util.ArrayList;

public class OpenChangesServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        UserAccount user = userManager.getUsers().get(usernameFromSession);
        String id = request.getParameter("id");
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        response.setContentType("application/json");
        try {
            ArrayList<JsTreeItem> openChangesJstreeArray = user.getWorkingCopyStatusJsTree(id);
            Gson gson = new Gson();
            String openChangesJstree = gson.toJson(openChangesJstreeArray);
            try(PrintWriter out = response.getWriter()) {
                out.write(openChangesJstree);
            }
        } catch (PreviousCommitsLimitExceededException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (RepositoryNotFoundException e) {
            e.printStackTrace();
        }
    }
}
