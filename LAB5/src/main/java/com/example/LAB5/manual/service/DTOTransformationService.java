package com.example.LAB5.manual.service;

import com.example.LAB5.manual.DTO.FunctionDTO;
import com.example.LAB5.manual.DTO.PointDTO;
import com.example.LAB5.manual.DTO.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DTOTransformationService {
    private static final Logger logger = LoggerFactory.getLogger(DTOTransformationService.class);

    // === TRANSFORM MAP TO DTO ===

    public UserDTO transformToUserDTO(Map<String, Object> data) {
        logger.info("Transforming map data to UserDTO: {}", data);

        try {
            UserDTO userDTO = new UserDTO();

            if (data.containsKey("id")) {
                userDTO.setId(Long.valueOf(data.get("id").toString()));
            }
            if (data.containsKey("login")) {
                userDTO.setLogin(data.get("login").toString());
            }
            if (data.containsKey("role")) {
                userDTO.setRole(data.get("role").toString());
            }
            if (data.containsKey("password")) {
                userDTO.setPassword(data.get("password").toString());
            }

            logger.debug("Successfully transformed to UserDTO: {}", userDTO);
            return userDTO;

        } catch (Exception e) {
            logger.error("Error transforming data to UserDTO: {}", data, e);
            throw new IllegalArgumentException("Invalid user data format", e);
        }
    }

    public FunctionDTO transformToFunctionDTO(Map<String, Object> data) {
        logger.info("Transforming map data to FunctionDTO: {}", data);

        try {
            FunctionDTO functionDTO = new FunctionDTO();

            if (data.containsKey("id")) {
                functionDTO.setId(Long.valueOf(data.get("id").toString()));
            }
            if (data.containsKey("userId") || data.containsKey("u_id")) {
                Long userId = data.containsKey("userId") ?
                        Long.valueOf(data.get("userId").toString()) :
                        Long.valueOf(data.get("u_id").toString());
                functionDTO.setUserId(userId);
            }
            if (data.containsKey("name")) {
                functionDTO.setName(data.get("name").toString());
            }
            if (data.containsKey("signature")) {
                functionDTO.setSignature(data.get("signature").toString());
            }

            logger.debug("Successfully transformed to FunctionDTO: {}", functionDTO);
            return functionDTO;

        } catch (Exception e) {
            logger.error("Error transforming data to FunctionDTO: {}", data, e);
            throw new IllegalArgumentException("Invalid function data format", e);
        }
    }

    public PointDTO transformToPointDTO(Map<String, Object> data) {
        logger.info("Transforming map data to PointDTO: {}", data);

        try {
            PointDTO pointDTO = new PointDTO();

            if (data.containsKey("id")) {
                pointDTO.setId(Long.valueOf(data.get("id").toString()));
            }
            if (data.containsKey("functionId") || data.containsKey("f_id")) {
                Long functionId = data.containsKey("functionId") ?
                        Long.valueOf(data.get("functionId").toString()) :
                        Long.valueOf(data.get("f_id").toString());
                pointDTO.setFunctionId(functionId);
            }
            if (data.containsKey("xValue") || data.containsKey("x_value")) {
                Double xValue = data.containsKey("xValue") ?
                        Double.valueOf(data.get("xValue").toString()) :
                        Double.valueOf(data.get("x_value").toString());
                pointDTO.setXValue(xValue);
            }
            if (data.containsKey("yValue") || data.containsKey("y_value")) {
                Double yValue = data.containsKey("yValue") ?
                        Double.valueOf(data.get("yValue").toString()) :
                        Double.valueOf(data.get("y_value").toString());
                pointDTO.setYValue(yValue);
            }

            logger.debug("Successfully transformed to PointDTO: {}", pointDTO);
            return pointDTO;

        } catch (Exception e) {
            logger.error("Error transforming data to PointDTO: {}", data, e);
            throw new IllegalArgumentException("Invalid point data format", e);
        }
    }

    // === TRANSFORM DTO TO MAP ===

    public Map<String, Object> transformToMap(UserDTO userDTO) {
        logger.debug("Transforming UserDTO to map: {}", userDTO);

        try {
            Map<String, Object> map = new HashMap<>();
            if (userDTO.getId() != null) {
                map.put("id", userDTO.getId());
            }
            if (userDTO.getLogin() != null) {
                map.put("login", userDTO.getLogin());
            }
            if (userDTO.getRole() != null) {
                map.put("role", userDTO.getRole());
            }
            if (userDTO.getPassword() != null) {
                map.put("password", userDTO.getPassword());
            }

            logger.debug("Successfully transformed UserDTO to map: {}", map);
            return map;

        } catch (Exception e) {
            logger.error("Error transforming UserDTO to map: {}", userDTO, e);
            throw new RuntimeException("Error transforming UserDTO to map", e);
        }
    }

    public Map<String, Object> transformToMap(FunctionDTO functionDTO) {
        logger.debug("Transforming FunctionDTO to map: {}", functionDTO);

        try {
            Map<String, Object> map = new HashMap<>();
            if (functionDTO.getId() != null) {
                map.put("id", functionDTO.getId());
            }
            if (functionDTO.getUserId() != null) {
                map.put("userId", functionDTO.getUserId());
            }
            if (functionDTO.getName() != null) {
                map.put("name", functionDTO.getName());
            }
            if (functionDTO.getSignature() != null) {
                map.put("signature", functionDTO.getSignature());
            }

            logger.debug("Successfully transformed FunctionDTO to map: {}", map);
            return map;

        } catch (Exception e) {
            logger.error("Error transforming FunctionDTO to map: {}", functionDTO, e);
            throw new RuntimeException("Error transforming FunctionDTO to map", e);
        }
    }

    public Map<String, Object> transformToMap(PointDTO pointDTO) {
        logger.debug("Transforming PointDTO to map: {}", pointDTO);

        try {
            Map<String, Object> map = new HashMap<>();
            if (pointDTO.getId() != null) {
                map.put("id", pointDTO.getId());
            }
            if (pointDTO.getFunctionId() != null) {
                map.put("functionId", pointDTO.getFunctionId());
            }
            if (pointDTO.getXValue() != null) {
                map.put("xValue", pointDTO.getXValue());
            }
            if (pointDTO.getYValue() != null) {
                map.put("yValue", pointDTO.getYValue());
            }

            logger.debug("Successfully transformed PointDTO to map: {}", map);
            return map;

        } catch (Exception e) {
            logger.error("Error transforming PointDTO to map: {}", pointDTO, e);
            throw new RuntimeException("Error transforming PointDTO to map", e);
        }
    }

    // === BATCH TRANSFORMATIONS ===

    public Map<String, Object> transformToMapWithAllFields(UserDTO userDTO) {
        logger.debug("Transforming UserDTO to map with all fields: {}", userDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("id", userDTO.getId() != null ? userDTO.getId() : 0L);
        map.put("login", userDTO.getLogin() != null ? userDTO.getLogin() : "");
        map.put("role", userDTO.getRole() != null ? userDTO.getRole() : "USER");
        map.put("password", userDTO.getPassword() != null ? userDTO.getPassword() : "");

        logger.debug("UserDTO transformed to map with all fields: {}", map);
        return map;
    }

    public Map<String, Object> transformToMapWithAllFields(FunctionDTO functionDTO) {
        logger.debug("Transforming FunctionDTO to map with all fields: {}", functionDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("id", functionDTO.getId() != null ? functionDTO.getId() : 0L);
        map.put("userId", functionDTO.getUserId() != null ? functionDTO.getUserId() : 0L);
        map.put("name", functionDTO.getName() != null ? functionDTO.getName() : "");
        map.put("signature", functionDTO.getSignature() != null ? functionDTO.getSignature() : "");

        logger.debug("FunctionDTO transformed to map with all fields: {}", map);
        return map;
    }

    public Map<String, Object> transformToMapWithAllFields(PointDTO pointDTO) {
        logger.debug("Transforming PointDTO to map with all fields: {}", pointDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("id", pointDTO.getId() != null ? pointDTO.getId() : 0L);
        map.put("functionId", pointDTO.getFunctionId() != null ? pointDTO.getFunctionId() : 0L);
        map.put("xValue", pointDTO.getXValue() != null ? pointDTO.getXValue() : 0.0);
        map.put("yValue", pointDTO.getYValue() != null ? pointDTO.getYValue() : 0.0);

        logger.debug("PointDTO transformed to map with all fields: {}", map);
        return map;
    }

    // === VALIDATION METHODS ===

    public boolean isValidUserMap(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            logger.warn("User data map is null or empty");
            return false;
        }

        boolean valid = data.containsKey("login") && data.get("login") != null &&
                data.containsKey("password") && data.get("password") != null;

        if (!valid) {
            logger.warn("Invalid user data map: missing required fields (login, password)");
        }
        return valid;
    }

    public boolean isValidFunctionMap(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            logger.warn("Function data map is null or empty");
            return false;
        }

        boolean valid = data.containsKey("name") && data.get("name") != null &&
                data.containsKey("signature") && data.get("signature") != null &&
                data.containsKey("userId") && data.get("userId") != null;

        if (!valid) {
            logger.warn("Invalid function data map: missing required fields (name, signature, userId)");
        }
        return valid;
    }

    public boolean isValidPointMap(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            logger.warn("Point data map is null or empty");
            return false;
        }

        boolean valid = data.containsKey("xValue") && data.get("xValue") != null &&
                data.containsKey("yValue") && data.get("yValue") != null &&
                data.containsKey("functionId") && data.get("functionId") != null;

        if (!valid) {
            logger.warn("Invalid point data map: missing required fields (xValue, yValue, functionId)");
        }
        return valid;
    }

    // === UTILITY METHODS ===

    public Map<String, Object> createUserMap(String login, String password, String role) {
        logger.debug("Creating user map: login={}, role={}", login, role);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("login", login);
        userMap.put("password", password);
        userMap.put("role", role != null ? role : "USER");

        logger.debug("Created user map: {}", userMap);
        return userMap;
    }

    public Map<String, Object> createFunctionMap(Long userId, String name, String signature) {
        logger.debug("Creating function map: userId={}, name={}, signature={}", userId, name, signature);

        Map<String, Object> functionMap = new HashMap<>();
        functionMap.put("userId", userId);
        functionMap.put("name", name);
        functionMap.put("signature", signature);

        logger.debug("Created function map: {}", functionMap);
        return functionMap;
    }

    public Map<String, Object> createPointMap(Long functionId, Double xValue, Double yValue) {
        logger.debug("Creating point map: functionId={}, xValue={}, yValue={}", functionId, xValue, yValue);

        Map<String, Object> pointMap = new HashMap<>();
        pointMap.put("functionId", functionId);
        pointMap.put("xValue", xValue);
        pointMap.put("yValue", yValue);

        logger.debug("Created point map: {}", pointMap);
        return pointMap;
    }

    // === EXTRACT SPECIFIC FIELDS ===

    public String extractLogin(Map<String, Object> userData) {
        if (userData != null && userData.containsKey("login")) {
            return userData.get("login").toString();
        }
        logger.warn("Login field not found in user data: {}", userData);
        return null;
    }

    public String extractFunctionName(Map<String, Object> functionData) {
        if (functionData != null && functionData.containsKey("name")) {
            return functionData.get("name").toString();
        }
        logger.warn("Name field not found in function data: {}", functionData);
        return null;
    }

    public Long extractFunctionId(Map<String, Object> pointData) {
        if (pointData != null && pointData.containsKey("functionId")) {
            return Long.valueOf(pointData.get("functionId").toString());
        }
        logger.warn("FunctionId field not found in point data: {}", pointData);
        return null;
    }
}