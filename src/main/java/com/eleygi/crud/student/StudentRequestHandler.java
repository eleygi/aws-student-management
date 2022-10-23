package com.eleygi.crud.student;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.eleygi.crud.student.dao.DaoFactory;
import com.eleygi.crud.student.dao.IDao;
import com.eleygi.crud.student.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class StudentRequestHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    protected final ObjectMapper mapper = new ObjectMapper();
    protected final IDao<Student> dao;

    protected StudentRequestHandler(){
        this(DaoFactory.create().createStudentDaoInstance());
    }

    StudentRequestHandler(IDao<Student> studentIDao){
        this.dao = studentIDao;
    }

    private APIGatewayProxyResponseEvent response() {
        return new APIGatewayProxyResponseEvent()
                .withHeaders(Collections.singletonMap("Content-Type", "application/json"));
    }

    protected APIGatewayProxyResponseEvent ok(String body) {
        return response().withStatusCode(HttpStatusCode.OK).withBody(body);
    }

    protected APIGatewayProxyResponseEvent created(String message) {
        return response().withStatusCode(HttpStatusCode.CREATED).withBody(String.format("{\"message\":\"%s\"}", message));
    }

    protected APIGatewayProxyResponseEvent badRequest(int httpStatusCode, String message) {
        return response().withStatusCode(httpStatusCode).withBody(String.format("{\"error\":\"Bad request\", \"message\":\"%s\"}", message));
    }

    protected APIGatewayProxyResponseEvent badRequest(String message) {
        return badRequest(HttpStatusCode.BAD_REQUEST, message);
    }

    protected APIGatewayProxyResponseEvent internalServerError() {
        return response().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR).withBody("{\"error\":\"Internal Server Error\", \"message\":\"An unexpected error occurred\"}");
    }

    protected APIGatewayProxyResponseEvent notFound(String message) {
        return response().withStatusCode(HttpStatusCode.NOT_FOUND).withBody(String.format("{\"error\":\"Not found\", \"message\":\"%s\"}", message));
    }

    protected void validateStudent(Student student) throws FunctionalException {
        Set<String> violations = new HashSet<>();
        if (StringUtils.isBlank(student.getFirstName())) {
            violations.add("first name must not be null");
        }
        if (StringUtils.isBlank(student.getLastName())) {
            violations.add("last name must not be null");
        }
        if (!violations.isEmpty()) {
            throw new FunctionalException(HttpStatusCode.BAD_REQUEST, "Invalid Student: " + String.join(", ", violations));
        }
    }
}
