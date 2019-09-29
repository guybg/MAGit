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

public class CommitsInfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount user = userManager.getUsers().get(usernameFromSession);
        String repositoryId = request.getParameter("id");

        Gson gson = new Gson();
        String responeContent = null;
        try {
            responeContent = gson.toJson(user.getCommitsInfo(repositoryId));
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } catch (IOException | ParseException | PreviousCommitsLimitExceededException e) {
            responeContent = e.getMessage();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }finally {
            try(PrintWriter out = response.getWriter()) {
                out.write(responeContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
