package servlets.serverListerner;

import com.magit.webLogic.users.UserAccount;
import com.magit.webLogic.users.UserManager;
import constants.Constants;
import utils.ServletUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

public class HttpSessionListener implements javax.servlet.http.HttpSessionListener {
    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        if(session==null) return;
        Object sessionAttribute = session.getAttribute(Constants.USERNAME);
        String userName = sessionAttribute != null ? sessionAttribute.toString() : null;
        UserManager userManager = ServletUtils.getUserManager(session.getServletContext());
        if (userName != null && userManager.getUsers().get(userName).isOnline()) {
            System.out.println("Clearing session for on session destroyed " + userName);
            UserAccount account = userManager.getUsers().get(userName);
            account.onLogout();
            userManager.getUsers().get(userName).setOnlineStatus(false);
        }
    }
}
