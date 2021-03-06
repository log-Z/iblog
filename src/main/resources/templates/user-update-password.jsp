<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>修改用户名</title>
    <script src="${pageContext.request.contextPath}/static/js/crypto-js.js"></script>
    <script src="${pageContext.request.contextPath}/static/js/secure.js"></script>
</head>
<body>
    <h1>修改用户密码</h1>
    <p>${msg}</p>
    <form:form modelAttribute="form" method="post">
        <label>旧密码: <input name="oldPassword" type="password" minlength="8" required="required"></label>
        <form:errors path="oldPassword" />
        <br>
        <label>新密码: <input name="newPassword" type="password" minlength="8" required="required"></label>
        <form:errors path="newPassword" />
        <br>
        <label>再次输入新密码: <input name="newPasswordAgain" type="password" minlength="8" required="required"></label>
        <form:errors path="newPasswordAgain" />
        <br>
        <input type="submit" value="修改">
    </form:form>
</body>
</html>
