package com.eleygi.crud.student;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.eleygi.crud.student.dao.IDao;
import com.eleygi.crud.student.model.Student;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.utils.StringUtils;

public class UpdateStudentLambdaFunction extends StudentRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStudentLambdaFunction.class);

    public UpdateStudentLambdaFunction() {
        super();
    }

    UpdateStudentLambdaFunction(IDao<Student> dao) {
        super(dao);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        String id = apiGatewayProxyRequestEvent.getPathParameters().get("id");
        if (StringUtils.isBlank(id)) {
            return badRequest("missing student id");
        }

        try {
            Student student = mapper.readValue(apiGatewayProxyRequestEvent.getBody(), Student.class);
            validateStudent(student);

            Student existingStudent = dao.get(id);
            if(existingStudent == null){
                return notFound(String.format("student %s not found", id));
            }

            student.setId(existingStudent.getId());
            dao.update(student);

            return ok(String.format("{\"message\":\"student %s updated\"}", student.getId()));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            return badRequest("Malformed Student");
        } catch(FunctionalException e){
            return badRequest(e.getHttpStatusCode(), e.getMessage());
        } catch(Exception e){
            LOGGER.error(e.getMessage());
            return internalServerError();
        }
    }

}
