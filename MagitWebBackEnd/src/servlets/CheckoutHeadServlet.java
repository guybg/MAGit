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
import java.util.ArrayList;
import java.util.HashMap;

public class CheckoutHeadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount user = userManager.getUsers().get(usernameFromSession);
        String branchName = request.getParameter("name");
        String id = request.getParameter("id");
        String requestType = request.getParameter("requestType");
        response.setContentType("application/json");
        HashMap<String,String> responseMessage= new HashMap<>();
        Gson gson = new Gson();
        switch (requestType){
            case "switch-branch":
                try {
                    user.pickHeadBranch(branchName, id);
                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
                    successMessage(response,responseMessage,"Branch changed successfully!");
                } catch (ParseException | IOException | InvalidNameException | BranchNotFoundException | PreviousCommitsLimitExceededException | RepositoryNotFoundException e) {
                    returnGenericMessage(response,responseMessage,e.getMessage());
                }catch (UncommitedChangesException e){
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    responseMessage.put("requestType", "open-changes");
                    responseMessage.put("msg", e.getMessage());
                    String resp =gson.toJson(responseMessage);
                    try (PrintWriter out = response.getWriter()) {
                        out.write(resp);
                        out.flush();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }catch (RemoteBranchException e){
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    responseMessage.put("requestType", "remote-branch");
                    responseMessage.put("msg", e.getMessage());
                    String resp =gson.toJson(responseMessage);
                    try (PrintWriter out = response.getWriter()) {
                        out.write(resp);
                        out.flush();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            case "create-rtb":
                try {
                    user.createRemoteTrackingBranch(id,branchName);
                    successMessage(response,responseMessage,"Remote tracking branch created successfully!");
                } catch (RepositoryNotFoundException | BranchAlreadyExistsException | BranchNotFoundException | InvalidNameException | RemoteReferenceException | IOException e) {
                    returnGenericMessage(response,responseMessage,e.getMessage());
                }
                break;
            case "force-checkout":
                try {
                    user.forcedChangeBranch(id,branchName);
                    successMessage(response,responseMessage,"switched branch successfully!");
                } catch (ParseException | PreviousCommitsLimitExceededException | IOException e) {
                    returnGenericMessage(response,responseMessage,e.getMessage());
                } catch (RemoteBranchException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    responseMessage.put("requestType", "generic");
                    responseMessage.put("msg", e.getMessage());
                    String resp = gson.toJson(responseMessage);
                    try (PrintWriter out = response.getWriter()) {
                        out.write(resp);
                        out.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
        }

    }

    private void returnGenericMessage(HttpServletResponse response, HashMap<String,String> responseMessage, String exMsg){
        Gson gson = new Gson();
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        responseMessage.put("requestType", "generic");
        responseMessage.put("msg", exMsg);
        String resp =gson.toJson(responseMessage);
        try (PrintWriter out = response.getWriter()) {
            out.write(resp);
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void successMessage(HttpServletResponse response, HashMap<String,String> responseMessage, String msg){
        Gson gson = new Gson();
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        responseMessage.put("requestType", "success");
        responseMessage.put("msg", msg);
        String resp =gson.toJson(responseMessage);
        try (PrintWriter out = response.getWriter()) {
            out.write(resp);
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
