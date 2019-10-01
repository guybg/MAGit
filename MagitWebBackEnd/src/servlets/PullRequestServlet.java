package servlets;

import com.google.gson.Gson;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
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

public class PullRequestServlet extends HttpServlet {
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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String action = request.getParameter("pr-action");
        String targetBranch = request.getParameter("target-branch");
        String baseBranch = request.getParameter("base-branch");
        String repositoryId = request.getParameter("repository-id");
        String requestId = request.getParameter("request-id");
        String message = request.getParameter("message");
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount account;
        String remoteId;
        String remoteUserName;
        response.setContentType("application/json");
        synchronized (this) {
            account = userManager.getUsers().get(usernameFromSession);
            remoteId = account.getRepositories().get(repositoryId).get("remote-id");
            remoteUserName = account.getRepositories().get(repositoryId).get("remote-user");
            if (action.equals("pr-create")) {
                UserAccount receiverUserAccount = userManager.getUsers().get(remoteUserName);
                try {
                    account.createPullRequest(receiverUserAccount, remoteId, account.getRepositories().get(repositoryId).get("activeBranch"),baseBranch, message, repositoryId);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RepositoryNotFoundException e) {
                    e.printStackTrace();
                } catch (RemoteReferenceException e) {
                    e.printStackTrace();
                } catch (PushException e) {
                    e.printStackTrace();
                } catch (UnhandledMergeException e) {
                    e.printStackTrace();
                } catch (CommitNotFoundException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (UncommitedChangesException e) {
                    e.printStackTrace();
                } catch (RemoteBranchException e) {
                    e.printStackTrace();
                } catch (PreviousCommitsLimitExceededException e) {
                    e.printStackTrace();
                }
            } else if (action.equals("pr-reject")) {
                try {
                    account.rejectPullRequest(Integer.parseInt(requestId));
                } catch (UnhandledMergeException e) {
                    e.printStackTrace();
                } catch (MergeNotNeededException e) {
                    e.printStackTrace();
                } catch (RepositoryNotFoundException e) {
                    e.printStackTrace();
                } catch (MergeException e) {
                    e.printStackTrace();
                } catch (UncommitedChangesException e) {
                    e.printStackTrace();
                } catch (FastForwardException e) {
                    e.printStackTrace();
                }
            } else if (action.equals("pr-accept")) {
                try {
                    account.acceptPullRequest(repositoryId, Integer.parseInt(requestId));
                } catch (UnhandledMergeException e) {
                    e.printStackTrace();
                } catch (MergeNotNeededException e) {
                    e.printStackTrace();
                } catch (RepositoryNotFoundException e) {
                    e.printStackTrace();
                } catch (MergeException e) {
                    e.printStackTrace();
                } catch (UncommitedChangesException e) {
                    e.printStackTrace();
                } catch (FastForwardException e) {
                    e.printStackTrace();
                }
            } else if (action.equals("pr-show")) {//show prs
                Gson gson = new Gson();
                String prs = gson.toJson(account.getPullRequests(repositoryId));
                try (PrintWriter out = response.getWriter()) {
                    out.println(prs);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

