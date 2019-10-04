package servlets;

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

public class CommitServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount user = userManager.getUsers().get(usernameFromSession);
        String id = request.getParameter("id");
        String inputFromUser = request.getParameter("inputFromUser");
        String responseContent = null;
        try {
            user.commit(id, inputFromUser);
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            responseContent = "Files Committed Successfully";
        } catch (ParseException | RepositoryNotFoundException | PreviousCommitsLimitExceededException | FastForwardException | UnhandledConflictsException | WorkingCopyStatusNotChangedComparedToLastCommitException | WorkingCopyIsEmptyException | IOException e) {
            responseContent = e.getMessage();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } finally {
            try(PrintWriter out = response.getWriter()) {
                out.write(responseContent);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
