<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"%>
<%@ page import="com.google.webauthn.gaedemo.storage.Credential"%>
<%@ page import="java.util.List"%>
<%@ page import="com.google.common.io.BaseEncoding"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>WebAuthn</title>
<script src="js/jquery-3.2.1.min.js"></script>
<script src="js/webauthn.js"></script>
</head>
<body onload="fetchAllCredentials();">
<div id="addCredential" onclick="addCredential();">
  <span>Add Credential</span>
</div>
<div id="getAssertion" onclick="getAssertion();">
  <span>Try Assertion</span>
</div>
<p id="credentials"></p>
</body>
</html>