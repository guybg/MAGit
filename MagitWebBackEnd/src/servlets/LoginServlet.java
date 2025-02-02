package servlets;

import com.magit.logic.exceptions.InvalidNameException;
import com.magit.webLogic.users.UserAccount;
import com.magit.webLogic.users.UserManager;
import constants.Constants;
import utils.SessionUtils;
import utils.ServletUtils;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static constants.Constants.USERNAME;

public class LoginServlet extends HttpServlet {

    // urls that starts with forward slash '/' are considered absolute
    // urls that doesn't start with forward slash '/' are considered relative to the place where this servlet request comes from
    // you can use absolute paths, but then you need to build them from scratch, starting from the context path
    // ( can be fetched from request.getContextPath() ) and then the 'absolute' path from it.
    // Each method with it's pros and cons...
    private final String MAIN_SCREEN_URL = "../mainScreen/mainScreen.html";
    private final String SIGN_UP_URL = "../signup/signup.html";
    private final String LOGIN_ERROR_URL = "/pages/loginerror/login_attempt_after_error.jsp";  // must start with '/' since will be used in request dispatcher...
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */

    private void prepareRedirectAjaxResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAjax(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("Location", response.encodeRedirectURL(MAIN_SCREEN_URL));
            response.flushBuffer();
        }
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        if (usernameFromSession == null) {
            //user is not logged in yet
            String usernameFromParameter = request.getParameter(USERNAME);
            if(usernameFromParameter !=null)
                usernameFromParameter = usernameFromParameter.toLowerCase();
            if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                //no username in session and no username in parameter -
                //redirect back to the index page
                //this return an HTTP code back to the browser telling it to load
                String errorMessage = "Please enter User name";
                //if (isAjax(request)) {
                //    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                //    response.setHeader("Location", response.encodeRedirectURL(SIGN_UP_URL));
                //    response.flushBuffer();
                //}
                try (PrintWriter out = response.getWriter()) {
                    out.print(errorMessage);
                    out.flush();
                }
                //response.sendRedirect(SIGN_UP_URL);
            } else {
                //normalize the username value
                usernameFromParameter = usernameFromParameter.trim();

                /*
                One can ask why not enclose all the synchronizations inside the userManager object ?
                Well, the atomic action we need to perform here includes both the question (isUserExists) and (potentially) the insertion
                of a new user (addUser). These two actions needs to be considered atomic, and synchronizing only each one of them, solely, is not enough.
                (of course there are other more sophisticated and performable means for that (atomic objects etc) but these are not in our scope)

                The synchronized is on this instance (the servlet).
                As the servlet is singleton - it is promised that all threads will be synchronized on the very same instance (crucial here)

                A better code would be to perform only as little and as necessary things we need here inside the synchronized block and avoid
                do here other not related actions (such as request dispatcher\redirection etc. this is shown here in that manner just to stress this issue
                 */
                   synchronized (this) {
                       if (userManager.isUserExists(usernameFromParameter) && userManager.getUsers().get(usernameFromParameter).isOnline()) {
                           String errorMessage = "Username " + usernameFromParameter + " already exists. Please enter a different username.";
                           // username already exists, forward the request back to index.jsp
                           // with a parameter that indicates that an error should be displayed
                           // the request dispatcher obtained from the servlet context is one that MUST get an absolute path (starting with'/')
                           // and is relative to the web app root
                           // see this link for more details:
                           // http://timjansen.github.io/jarfiller/guide/servlet25/requestdispatcher.xhtml
                           request.setAttribute(Constants.USER_NAME_ERROR, errorMessage);

                           try (PrintWriter out = response.getWriter()) {
                               out.print(errorMessage);
                               out.flush();
                           }

                           //getServletContext().getRequestDispatcher(LOGIN_ERROR_URL).forward(request, response);

                       } else {
                           UserAccount account;
                           if(!userManager.getUsers().containsKey(usernameFromParameter)){
                               //add the new user to the users list
                               account = new UserAccount(usernameFromParameter);

                               try {
                                   userManager.addUser(usernameFromParameter, account);
                               } catch (InvalidNameException e) {
                                   String errorMessage = e.getMessage();
                                   try (PrintWriter out = response.getWriter()) {
                                       out.print(errorMessage);
                                       out.flush();
                                   }
                               }
                           }else{
                               account = userManager.getUsers().get(usernameFromParameter);
                           }
                           account.setOnlineStatus(true);
                           //set the username in a session so it will be available on each request
                           //the true parameter means that if a session object does not exists yet
                           //create a new one
                           request.getSession(true).setAttribute(Constants.USERNAME, usernameFromParameter);

                           //redirect the request to the chat room - in order to actually change the URL
                           System.out.println("On login, request URI is: " + request.getRequestURI());
                           prepareRedirectAjaxResponse(request,response);
                         //  response.sendRedirect(CHAT_ROOM_URL);
                       }
                   }
            }
        } else {
            //user is already logged in
            prepareRedirectAjaxResponse(request,response);
            //response.sendRedirect(CHAT_ROOM_URL);
        }
    }
    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
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
