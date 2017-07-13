<%@ page import="web.CountryListGetter" %>
<%@ page import="java.util.ArrayList" %><%--
  Created by IntelliJ IDEA.
  User: Admin
  Date: 30.06.2017
  Time: 14:05
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="utf-8" %>
<%
  CountryListGetter getterList = new CountryListGetter(request.getServletContext().getRealPath(""));
  ArrayList<String> list = getterList.getList();
%>
<html>
  <head>
    <title>MyTitle</title>
    <meta charset="utf-8">
    <script type="text/javascript" src="${pageContext.request.contextPath}/jquery-3.2.1.js"></script>
  </head>
  <body>
    <form method="GET" action="/main">
      <select id="country" name="country">
        <option value="0">Выберите страну</option>
        <%
          for (String str : list) {
            out.print("<option id=\"" + str + "\">" + str + "</option>");
          }
        %>
      </select>

      <div id="querydiv"> </div>
      <div>
        <input type="checkbox" name="isNew">Is new search?
      </div>
      <div>
        <input type="submit" name="Start" value="Start">
        <input type="submit" name="Stop" value="Stop">
        <input type="submit" name="Delete" value="Delete">
      </div>
    </form>

    <div id="mydiv"> </div>
    <form action="/main" method="POST">
        <div id="results"> </div>
    </form>

  <script type='text/javascript'>
      $('#country').change(function() {
          var id = $(this).val(); //get the current value's option
          $.ajax({
              type:'POST',
              url:'/main',
              data:{'country':id},
              success:function(response){
                  $('#querydiv').html(response + "<br><input id=\"query\" name=\"query\" placeholder=\"Query\">");

                  $('#queryselect').change(function() {
                      var id = $(this).val();
                      $.ajax({
                          type:'POST',
                          url:'/main',
                          data:{'query':id},
                          success:function(response){
                              if (response != "Select query")
                                $('#query').val(response);
                              else $('#query').val("");
                          }
                      });
                  });
              }
          });
      });
      $('#country').change(function() {
          var id = $(this).val(); //get the current value's option
          $.ajax({
              type:'POST',
              url:'/main',
              data:{'country':id, 'results': 1},
              success:function(response){
                  $('#results').html(response);
              }
          });
      });

        function show()
        {
            $.ajax({
                url: "/main",
                type:'POST',
                cache: false,
                data: {'progress': 'true'},
                success: function(html){
                    $("#mydiv").html(html);
                }
            });
        }

        $(document).ready(function(){
            show();
            setInterval('show()',1000);
        });
    </script>
  </body>
</html>
