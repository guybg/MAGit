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
        String applicantName = request.getParameter("applicant");
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
            UserAccount receiverUserAccount = userManager.getUsers().get(remoteUserName);
            if (action.equals("pr-create")) {
                try {
                    account.createPullRequest(receiverUserAccount, remoteId, account.getRepositories().get(repositoryId).get("activeBranch"),baseBranch, message, repositoryId);
                    receiverUserAccount.addNotification(account.getUserName(),"New pull request to repository with id: " + remoteId);
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
                    account.rejectPullRequest(repositoryId,Integer.parseInt(requestId));
                    userManager.getUsers().get(applicantName).addNotification(account.getUserName(),"Pull request reject into repository with id " +repositoryId);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("Pull request rejected");
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                    response.setContentType("test/html");
                    account.acceptPullRequest(repositoryId, Integer.parseInt(requestId));
                    userManager.getUsers().get(applicantName).addNotification(account.getUserName(),"Pull request accepted into repository with id " +repositoryId);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("Pull request accepted");
                        out.flush();
                    }
                } catch (UnhandledMergeException e) {
                    e.printStackTrace();
                } catch (MergeNotNeededException e) {
                    e.printStackTrace();
                } catch (RepositoryNotFoundException e) {
                    e.printStackTrace();
                } catch (UncommitedChangesException | MergeException | FastForwardException e) {
                    try (PrintWriter out = response.getWriter()) {
                        out.println(e.getMessage());
                        out.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } catch (PreviousCommitsLimitExceededException e) {
                    e.printStackTrace();
                } catch (InvalidNameException e) {
                    e.printStackTrace();
                } catch (RemoteBranchException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BranchNotFoundException e) {
                    e.printStackTrace();
                } catch (WorkingCopyIsEmptyException e) {
                    e.printStackTrace();
                } catch (UnhandledConflictsException e) {
                    e.printStackTrace();
                } catch (WorkingCopyStatusNotChangedComparedToLastCommitException e) {
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

