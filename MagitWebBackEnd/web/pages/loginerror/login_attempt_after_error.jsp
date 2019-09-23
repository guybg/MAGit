<%--
    Document   : index
    Created on : Jan 24, 2012, 6:01:31 AM
    Author     : blecherl
    This is the login JSP for the online chat application
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<%@page import="utils.*" %>
<%@ page import="constants.Constants" %>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <!-- Custom styles for this template-->
    <link rel="stylesheet" href="../css/sb-admin.css">
    <!-- Custom fonts for this template-->
    <link href="../fontawesome/css/all.min.css" rel="stylesheet" type="text/css">

    <script src="signup.js"></script>
</head>
<body class="bg-dark">
<div class="container">
    <% String usernameFromSession = SessionUtils.getUsername(request);%>
    <% String usernameFromParameter = request.getParameter(Constants.USERNAME) != null ? request.getParameter(Constants.USERNAME) : "";%>
    <% if (usernameFromSession == null) {%>
    <div class="card card-login mx-auto mt-5">
        <div class="card-header">Login</div>
        <div class="card-body">
            <form method="GET" action="login">
                <div class="form-group">
                    <div class="form-label-group">
                        <input type="text" id="login" class="form-control" placeholder="login" name="<%=Constants.USERNAME%>"
                               value="<%=usernameFromParameter%>">
                        <label for="login">User name</label>
                    </div>
                </div>
                <input type="submit" class="btn btn-primary btn-block" value="login">
            </form>
            <% Object errorMessage = request.getAttribute(Constants.USER_NAME_ERROR);%>
            <% if (errorMessage != null) {%>
            <span class="bg-danger"><%=errorMessage%></span>
            <% } %>
            <% } else {%>
            <h1>Welcome back, <%=usernameFromSession%>
            </h1>
            <a href="../chatroom/chatroom.html">Click here to enter the chat room</a>
            <br/>
            <a href="login?logout=true" id="logout">logout</a>
            <% }%>
        </div>
    </div>
</div>
</body>
</html>