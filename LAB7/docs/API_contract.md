URL:    /api/v1
Формат: JSON


Эндпоинты:

Users:
GET	    /api/v1/users	
GET	    /api/v1/users/{id}	
POST	/api/v1/users	
PUT  	/api/v1/users/{id}	
DELETE	/api/v1/users/{id}


Functions:
GET	    /api/v1/functions
GET	    /api/v1/functions/{id}
GET	    /api/v1/users/{userId}/functions
POST	/api/v1/functions
PUT	    /api/v1/functions/{id}
DELETE	/api/v1/functions/{id}


Points:
GET	    /api/v1/points
GET	    /api/v1/points/{id}
GET	    /api/v1/functions/{functionId}/points
POST	/api/v1/points
PUT	    /api/v1/points/{id}
DELETE	/api/v1/points/{id}


DTO:


User DTOs:
// CreateUserRequest (для POST /api/v1/users)
{
"username": "string",
"password": "string"
}

// UserResponse (для всех GET запросов)
{
"id": "long",
"username": "string",
"email": "string",
"createdAt": "timestamp",
"updatedAt": "timestamp"
}

// UpdateUserRequest (для PUT /api/v1/users/{id})
{
"username": "string",
"password": "string"
}


Function DTOs:
// CreateFunctionRequest (для POST /api/v1/functions)
{
"userId": "long",
"name": "string",
"signature": "string"
}

// FunctionResponse (для всех GET запросов)
{
"id": "long",
"userId": "long",
"name": "string",
"signature": "string",
"createdAt": "timestamp"
}

// UpdateFunctionRequest (для PUT /api/v1/functions/{id})
{
"name": "string",
"userId": "long",
"signature": "string"
}


Point DTOs:
// CreatePointRequest (для POST /api/v1/points)
{
"functionId": "long",
"xValue": "number",
"yValue": "number"
}

// PointResponse (для всех GET запросов)
{
"id": "long",
"functionId": "long",
"xValue": "number",
"yValue": "number",
"createdAt": "timestamp"
}

// UpdatePointRequest (для PUT /api/v1/points/{id})
{
"xValue": "number",
"yValue": "number"
}