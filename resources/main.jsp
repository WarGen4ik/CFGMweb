<%@ page import="web.CountryListGetter" %>
<%@ page import="java.util.ArrayList" %><%--
  Created by IntelliJ IDEA.
  User: Admin
  Date: 30.06.2017
  Time: 14:05
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  CountryListGetter getterList = new CountryListGetter(request.getServletContext().getRealPath(""));
  ArrayList<String> list = getterList.getList();
%>
<html>
  <head>
    <title>MyTitle</title>
    <meta charset="utf-8">
  </head>
  <body>
  <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>

    <form method="GET" action="/">
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

  <script type='text/javascript'>
      $('#country').change(function() {
          var id = $(this).val(); //get the current value's option
          $.ajax({
              type:'POST',
              url:'/',
              data:{'country':id},
              success:function(response){
                  $('#querydiv').html(response + "<br><input id=\"query\" name=\"query\" placeholder=\"Query\">");

                  $('#queryselect').change(function() {
                      var id = $(this).val();
                      $.ajax({
                          type:'POST',
                          url:'/',
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

        function show()
        {
            $.ajax({
                url: "/",
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
