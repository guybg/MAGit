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
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        synchronized (this) {
            account = userManager.getUsers().get(usernameFromSession);
            remoteId = account.getRepositories().get(repositoryId).get("remoteId");
            remoteUserName = account.getRepositories().get(repositoryId).get("remoteUser");
            UserAccount receiverUserAccount = userManager.getUsers().get(remoteUserName);
            if (action.equals("pr-create")) {
                try {
                    account.createPullRequest(receiverUserAccount, remoteId, targetBranch,baseBranch, message, repositoryId);
                    receiverUserAccount.addNotification(account.getUserName(),"New pull request to repository with id: " + remoteId);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("Pull request created successfully.");
                        out.flush();
                    }
                } catch (UncommitedChangesException | IOException | RepositoryNotFoundException | RemoteReferenceException | PushException | UnhandledMergeException | CommitNotFoundException | ParseException | RemoteBranchException | PreviousCommitsLimitExceededException e) {
                    try (PrintWriter out = response.getWriter()) {
                        out.println(e.getMessage());
                        out.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } catch (BranchNotFoundException | PullRequestException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.println(e.getMessage());
                        out.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (action.equals("pr-reject")) {
                try {
                    String rejectMessage = request.getParameter("reject-message");
                    account.rejectPullRequest(repositoryId,Integer.parseInt(requestId));
                    userManager.getUsers().get(applicantName).addNotification(account.getUserName(),"Pull request reject into repository with id " + repositoryId + " by " + usernameFromSession +"<br>" + "with reason: " + rejectMessage);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("Pull request rejected");
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnhandledMergeException | MergeNotNeededException | RepositoryNotFoundException | MergeException | UncommitedChangesException | FastForwardException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.println(e.getMessage());
                        out.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (action.equals("pr-accept")) {
                try {
                    response.setContentType("test/html");
                    account.acceptPullRequest(repositoryId, Integer.parseInt(requestId));
                    userManager.getUsers().get(applicantName).addNotification(account.getUserName(),"Pull request accepted into repository with id " +repositoryId + " by " + usernameFromSession);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("Pull request accepted");
                        out.flush();
                    }
                } catch (UnhandledMergeException | WorkingCopyStatusNotChangedComparedToLastCommitException | UnhandledConflictsException | WorkingCopyIsEmptyException | BranchNotFoundException | RemoteBranchException | InvalidNameException | RepositoryNotFoundException | MergeNotNeededException | MergeException | FastForwardException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.println(e.getMessage());
                        out.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } catch (PreviousCommitsLimitExceededException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (UncommitedChangesException e){
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("There are unsaved changes, in order to accept the pull request, please commit your changes.");
                        out.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (action.equals("pr-show")) {//show prs
                response.setContentType("application/json");
                    Gson gson = new Gson();
                String prs = gson.toJson(account.getPullRequests(repositoryId));
                try (PrintWriter out = response.getWriter()) {
                    out.println(prs);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(action.equals("pr-diff")){
                response.setContentType("application/json");
                Gson gson = new Gson();
                String diffTree = null;
                try {
                    diffTree = gson.toJson(account.getOverallCommitsDiff(targetBranch, baseBranch,repositoryId));
                   // diffTree = gson.toJson(account.getPullRequestDifferencesJsTreeArray(repositoryId));
                    try (PrintWriter out = response.getWriter()) {
                        out.println(diffTree);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (PreviousCommitsLimitExceededException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (RepositoryNotFoundException e) {
                    e.printStackTrace();
                } catch (CommitNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

