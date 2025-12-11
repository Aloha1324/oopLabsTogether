package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DTO.FunctionDTO;
import com.example.LAB5.manual.DTO.PointDTO;
import com.example.LAB5.manual.DTO.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DTOTransformationService {

    private static final Logger log = LoggerFactory.getLogger(DTOTransformationService.class);

    public UserDTO toUserDTO(Map<String, Object> source) {
        log.info("Преобразование данных в UserDTO: {}", source);

        try {
            UserDTO dto = new UserDTO();

            Object idVal = source.get("id");
            if (idVal != null) {
                dto.setId(Long.valueOf(idVal.toString()));
            }

            Object loginVal = source.get("login");
            if (loginVal != null) {
                dto.setLogin(loginVal.toString());
            }

            Object roleVal = source.get("role");
            if (roleVal != null) {
                dto.setRole(roleVal.toString());
            }

            Object passwordVal = source.get("password");
            if (passwordVal != null) {
                dto.setPassword(passwordVal.toString());
            }

            log.debug("Результат преобразования в UserDTO: {}", dto);
            return dto;
        } catch (RuntimeException ex) {
            log.error("Ошибка преобразования карты в UserDTO: {}", source, ex);
            throw new IllegalArgumentException("Invalid user data format", ex);
        }
    }

    public FunctionDTO toFunctionDTO(Map<String, Object> source) {
        log.info("Преобразование данных в FunctionDTO: {}", source);

        try {
            FunctionDTO dto = new FunctionDTO();

            Object idVal = source.get("id");
            if (idVal != null) {
                dto.setId(Long.valueOf(idVal.toString()));
            }

            Object userIdVal = source.containsKey("userId")
                    ? source.get("userId")
                    : source.get("u_id");
            if (userIdVal != null) {
                dto.setUserId(Long.valueOf(userIdVal.toString()));
            }

            Object nameVal = source.get("name");
            if (nameVal != null) {
                dto.setName(nameVal.toString());
            }

            Object signatureVal = source.get("signature");
            if (signatureVal != null) {
                dto.setSignature(signatureVal.toString());
            }

            log.debug("Результат преобразования в FunctionDTO: {}", dto);
            return dto;
        } catch (RuntimeException ex) {
            log.error("Ошибка преобразования карты в FunctionDTO: {}", source, ex);
            throw new IllegalArgumentException("Invalid function data format", ex);
        }
    }

    public PointDTO toPointDTO(Map<String, Object> source) {
        log.info("Преобразование данных в PointDTO: {}", source);

        try {
            PointDTO dto = new PointDTO();

            Object idVal = source.get("id");
            if (idVal != null) {
                dto.setId(Long.valueOf(idVal.toString()));
            }

            Object functionIdVal = source.containsKey("functionId")
                    ? source.get("functionId")
                    : source.get("f_id");
            if (functionIdVal != null) {
                dto.setFunctionId(Long.valueOf(functionIdVal.toString()));
            }

            Object xVal = source.containsKey("xValue")
                    ? source.get("xValue")
                    : source.get("x_value");
            if (xVal != null) {
                dto.setXValue(Double.valueOf(xVal.toString()));
            }

            Object yVal = source.containsKey("yValue")
                    ? source.get("yValue")
                    : source.get("y_value");
            if (yVal != null) {
                dto.setYValue(Double.valueOf(yVal.toString()));
            }

            log.debug("Результат преобразования в PointDTO: {}", dto);
            return dto;
        } catch (RuntimeException ex) {
            log.error("Ошибка преобразования карты в PointDTO: {}", source, ex);
            throw new IllegalArgumentException("Invalid point data format", ex);
        }
    }

    public Map<String, Object> toMap(UserDTO user) {
        log.debug("Преобразование UserDTO в карту: {}", user);
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("login", user.getLogin());
        result.put("role", user.getRole());
        result.put("password", user.getPassword());
        return result;
    }

    public Map<String, Object> toMap(FunctionDTO function) {
        log.debug("Преобразование FunctionDTO в карту: {}", function);
        Map<String, Object> result = new HashMap<>();
        result.put("id", function.getId());
        result.put("userId", function.getUserId());
        result.put("name", function.getName());
        result.put("signature", function.getSignature());
        return result;
    }

    public Map<String, Object> toMap(PointDTO point) {
        log.debug("Преобразование PointDTO в карту: {}", point);
        Map<String, Object> result = new HashMap<>();
        result.put("id", point.getId());
        result.put("functionId", point.getFunctionId());
        result.put("xValue", point.getXValue());
        result.put("yValue", point.getYValue());
        return result;
    }
}
