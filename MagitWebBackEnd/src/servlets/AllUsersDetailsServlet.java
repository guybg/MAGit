package servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.magit.webLogic.users.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class AllUsersDetailsServlet extends HttpServlet {

    private void prepareRedirectAjaxResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAjax(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("Location", response.encodeRedirectURL("../signup/signup.html"));
            response.flushBuffer();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        if (usernameFromSession != null) {
            try (PrintWriter out = response.getWriter()) {
                response.setContentType("application/json");
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String allUsers = gson.toJson(userManager.getUsers());
                out.print(allUsers);
                out.flush();
            }
        }
    }

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
    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}
