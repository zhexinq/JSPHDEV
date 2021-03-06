<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@ page import="java.util.*" %>
    <%@ page import="model.Automobile" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<% 
Automobile auto = (Automobile)request.getAttribute("auto");
String[] setNames = auto.getOptionSetNames();
String makeAndModel = auto.getMake() + " " + auto.getModel();
%>

<h1>Basic Car Choice</h1>
<form action="dispatcher" method="get">
<table style="width:100%" action="">
<tr bgcolor="#FDF5E6"><th>Make/Model<th><%=makeAndModel%>
<% for (String setName:setNames) { %>
  <tr>
    <td><%= setName %></td>
    <td>    	<select name=<%= setName %> >

    	<% for (String optName:auto.getOptionNames(setName)) 
    		out.println("<option value=\"" + optName + "\">" + optName + "/$" + auto.getOptionPrice(setName, optName));
    		%>
    	</select>
    </td>		
  </tr>
  <%} %>
</table>
<input type="hidden" name="requestType" value="choiceResult"/>
<input type="submit" value="Done"/>
</form>

</body>
</html>