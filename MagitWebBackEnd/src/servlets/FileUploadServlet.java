package servlets;
import static constants.Constants.USER_UPLOADED_XML;
//taken from: http://www.servletworld.com/servlet-tutorials/servlet3/multipartconfig-file-upload-example.html
// and http://docs.oracle.com/javaee/6/tutorial/doc/glraq.html

import com.google.gson.Gson;
import com.magit.logic.exceptions.InvalidNameException;
import com.magit.webLogic.users.UserAccount;
import com.magit.webLogic.users.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Scanner;
import java.util.function.Consumer;

@WebServlet("/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class FileUploadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("fileupload/form.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        //PrintWriter out = response.getWriter();

        Collection<Part> parts = request.getParts();


        StringBuilder fileContent = new StringBuilder();

        for (Part part : parts) {
            //to write the content of the file to a string
            fileContent.append(readFromInputStream(part.getInputStream()));
        }
        InputStream inputStream = new ByteArrayInputStream(fileContent.toString().getBytes(Charset.forName("UTF-8")));
       // out.close();
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UserAccount account = userManager.getUsers().get(usernameFromSession);
        try {
            account.addRepository(inputStream, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    String exceptionMessage = s;
                    try (PrintWriter out = response.getWriter()) {
                        out.println(exceptionMessage);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        //out.println(fileContent.toString());
    }

    private String readFromInputStream(InputStream inputStream) {
        return new Scanner(inputStream).useDelimiter("\\Z").next();
    }
}