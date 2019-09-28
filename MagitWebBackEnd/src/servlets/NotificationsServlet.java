package servlets;

import com.google.gson.Gson;
import com.magit.webLogic.users.UserAccount;
import com.magit.webLogic.users.UserManager;
import com.magit.webLogic.utils.notifications.SingleNotification;
import constants.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class NotificationsServlet extends HttpServlet {

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            response.sendRedirect(request.getContextPath() + "/index.html");
        }

        /*
        verify chat version given from the user is a valid number. if not it is considered an error and nothing is returned back
        Obviously the UI should be ready for such a case and handle it properly
         */
        boolean isGetNotifications = ServletUtils.getBooleanParameter(request, Constants.GET_UNREAD_NOTIFICATIONS_COUNT_PARAMETER);
        int version = ServletUtils.getIntParameter(request, Constants.NOTIFICATIONS_VERSION_PARAMETER);

        if (version == Constants.INT_PARAMETER_ERROR) {
            return;
        }

        /*
        Synchronizing as minimum as I can to fetch only the relevant information from the chat manager and then only processing and sending this information onward
        Note that the synchronization here is on the ServletContext, and the one that also synchronized on it is the chat servlet when adding new chat lines.
         */

        int serverVersion = 0;
        int unSeenMessages = 0;
        List<SingleNotification> notificationsEntries;
        UserAccount account;
        synchronized (getServletContext()) {
            account = userManager.getUsers().get(username);
        }

        // log and create the response json string

        if(isGetNotifications){
            synchronized (getServletContext()){
                unSeenMessages = account.getNumberOfNewNotifications();
            }
            try (PrintWriter out = response.getWriter()) {
                out.print(unSeenMessages);
                out.flush();
            }
        }else {
            synchronized (getServletContext()) {
                serverVersion = account.getNotificationsVersion();
                notificationsEntries = account.getNotifications(version);
            }
            Gson gson = new Gson();
            NotificationsAndVersion nav = new NotificationsAndVersion(notificationsEntries, serverVersion);
            String jsonResponse = gson.toJson(nav);
            logServerMessage("Server Chat version: " + serverVersion + ", User '" + username + "' Chat version: " + version + "' savedversion: " + userManager.getUsers().get(username).getLastUpdatedNotificationsVersion());
            logServerMessage(jsonResponse);

            try (PrintWriter out = response.getWriter()) {
                out.print(jsonResponse);
                out.flush();
            }
        }
    }

    private void logServerMessage(String message){
        System.out.println(message);
    }

    private static class NotificationsAndVersion {

        final private List<SingleNotification> entries;
        final private int version;

        public NotificationsAndVersion(List<SingleNotification> entries, int version) {
            this.entries = entries;
            this.version = version;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
