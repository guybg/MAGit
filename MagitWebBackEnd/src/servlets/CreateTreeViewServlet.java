package servlets;

import com.google.gson.Gson;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
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

public class CreateTreeViewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount user = userManager.getUsers().get(usernameFromSession);
        String repositoryId = request.getParameter("id");
        String sha1 = request.getParameter("sha1");
        String responseContent = null;
        try {
            Gson gson = new Gson();
            responseContent = gson.toJson(user.getTree(repositoryId,sha1));
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (ParseException | PreviousCommitsLimitExceededException | IOException e) {
            responseContent = e.getMessage();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } finally {
            try (PrintWriter out = response.getWriter()) {
                out.write(responseContent);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}