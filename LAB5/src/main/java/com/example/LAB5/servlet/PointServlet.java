package com.example.LAB5.servlet;

import com.example.LAB5.manual.DTO.PointDTO;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/points")
public class PointServlet extends HttpServlet {
    private List<PointDTO> points = new ArrayList<>();
    private Gson gson = new Gson();
    private Long currentId = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idParam = request.getParameter("id");
        String functionIdParam = request.getParameter("functionId");

        if (idParam != null) {
            Long id = Long.parseLong(idParam);
            PointDTO point = points.stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (point != null) {
                response.getWriter().write(gson.toJson(point));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (functionIdParam != null) {
            Long functionId = Long.parseLong(functionIdParam);
            List<PointDTO> functionPoints = points.stream()
                    .filter(p -> p.getFunctionId().equals(functionId))
                    .toList();
            response.getWriter().write(gson.toJson(functionPoints));
        } else {
            response.getWriter().write(gson.toJson(points));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PointDTO point = gson.fromJson(request.getReader(), PointDTO.class);
        point.setId(currentId++);
        points.add(point);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write(gson.toJson(point));
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
        PointDTO updatedPoint = gson.fromJson(request.getReader(), PointDTO.class);

        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).getId().equals(id)) {
                updatedPoint.setId(id);
                points.set(i, updatedPoint);

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(gson.toJson(updatedPoint));
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
        boolean removed = points.removeIf(point -> point.getId().equals(id));

        if (removed) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}