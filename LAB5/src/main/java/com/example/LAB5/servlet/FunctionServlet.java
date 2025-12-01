package com.example.LAB5.servlet;

import com.example.LAB5.manual.DTO.FunctionDTO;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/functions")
public class FunctionServlet extends HttpServlet {
    private List<FunctionDTO> functions = new ArrayList<>();
    private Gson gson = new Gson();
    private Long currentId = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idParam = request.getParameter("id");
        String userIdParam = request.getParameter("userId");

        if (idParam != null) {
            Long id = Long.parseLong(idParam);
            FunctionDTO function = functions.stream()
                    .filter(f -> f.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (function != null) {
                response.getWriter().write(gson.toJson(function));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (userIdParam != null) {
            Long userId = Long.parseLong(userIdParam);
            List<FunctionDTO> userFunctions = functions.stream()
                    .filter(f -> f.getUserId().equals(userId))
                    .toList();
            response.getWriter().write(gson.toJson(userFunctions));
        } else {
            response.getWriter().write(gson.toJson(functions));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        FunctionDTO function = gson.fromJson(request.getReader(), FunctionDTO.class);
        function.setId(currentId++);
        functions.add(function);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write(gson.toJson(function));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long id = Long.parseLong(idParam);
        FunctionDTO updatedFunction = gson.fromJson(request.getReader(), FunctionDTO.class);

        for (int i = 0; i < functions.size(); i++) {
            if (functions.get(i).getId().equals(id)) {
                updatedFunction.setId(id);
                functions.set(i, updatedFunction);

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(gson.toJson(updatedFunction));
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long id = Long.parseLong(idParam);
        boolean removed = functions.removeIf(function -> function.getId().equals(id));

        if (removed) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}