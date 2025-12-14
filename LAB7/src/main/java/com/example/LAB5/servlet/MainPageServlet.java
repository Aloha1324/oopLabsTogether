package com.example.LAB5.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/")
public class MainPageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'/>");
            out.println("<title>LAB5 Manual API</title>");
            out.println("<style>");
            out.println("body{font-family:Arial, sans-serif;margin:40px;}");
            out.println("h1{color:#333;}");
            out.println("h2{margin-top:25px;}");
            out.println(".endpoint{background:#f5f5f5;padding:8px 10px;margin:4px 0;border-radius:4px;}");
            out.println("a{color:#0069d9;text-decoration:none;}");
            out.println("a:hover{text-decoration:underline;}");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");

            out.println("<h1>LAB5 Manual API — Tomcat is running</h1>");
            out.println("<p>Below is a short overview of the main HTTP endpoints.</p>");

            // USERS
            out.println("<h2>Users endpoints</h2>");
            out.println("<div class='endpoint'>GET <a href='/LAB5/users'>/users</a> — list all users</div>");
            out.println("<div class='endpoint'>GET /users/{id} — get user by ID</div>");
            out.println("<div class='endpoint'>GET /users/login/{login} — get user by login</div>");
            out.println("<div class='endpoint'>GET /users/role/{role} — list users by role</div>");
            out.println("<div class='endpoint'>POST /users — create new user</div>");
            out.println("<div class='endpoint'>PUT /users/{id} — update existing user</div>");
            out.println("<div class='endpoint'>DELETE /users/{id} — remove user</div>");

            // FUNCTIONS
            out.println("<h2>Functions endpoints</h2>");
            out.println("<div class='endpoint'>GET <a href='/LAB5/functions'>/functions</a> — list all functions</div>");
            out.println("<div class='endpoint'>GET /functions/{id} — get function by ID</div>");
            out.println("<div class='endpoint'>GET /functions/user/{userId} — list functions by owner ID</div>");
            out.println("<div class='endpoint'>GET /functions/name/{name} — search functions by name</div>");
            out.println("<div class='endpoint'>GET /functions/stats/{functionId} — function statistics</div>");
            out.println("<div class='endpoint'>POST /functions — create function</div>");
            out.println("<div class='endpoint'>PUT /functions/{id} — update function</div>");
            out.println("<div class='endpoint'>DELETE /functions/{id} — delete function</div>");

            // POINTS
            out.println("<h2>Points endpoints</h2>");
            out.println("<div class='endpoint'>GET <a href='/LAB5/points'>/points</a> — entry for points API</div>");
            out.println("<div class='endpoint'>GET /points/{id} — get point by ID</div>");
            out.println("<div class='endpoint'>GET /points/function/{functionId} — points for function</div>");
            out.println("<div class='endpoint'>GET /points/max/{functionId} — point with maximum Y</div>");
            out.println("<div class='endpoint'>GET /points/min/{functionId} — point with minimum Y</div>");
            out.println("<div class='endpoint'>GET /points/stats/{functionId} — statistics for function points</div>");
            out.println("<div class='endpoint'>POST /points — create single point</div>");
            out.println("<div class='endpoint'>POST /points/generate/{functionId} — generate points for function</div>");
            out.println("<div class='endpoint'>PUT /points/{id} — update point</div>");
            out.println("<div class='endpoint'>DELETE /points/{id} — delete point</div>");

            out.println("<p>For POST, PUT and DELETE requests use a REST client (e.g. Postman) with Basic Auth.</p>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}
