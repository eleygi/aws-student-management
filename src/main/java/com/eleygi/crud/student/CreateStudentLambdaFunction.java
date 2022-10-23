package com.eleygi.crud.student;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.eleygi.crud.student.dao.IDao;
import com.eleygi.crud.student.model.Student;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateStudentLambdaFunction extends StudentRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateStudentLambdaFunction.class);

    public CreateStudentLambdaFunction() {
        super();
    }

    CreateStudentLambdaFunction(IDao<Student> dao) {
        super(dao);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        try {
            Student student = mapper.readValue(apiGatewayProxyRequestEvent.getBody(), Student.class);
            validateStudent(student);

            student.setId(apiGatewayProxyRequestEvent.getRequestContext().getRequestId());

            dao.create(student);

            return created(String.format("student %s created", student.getId()));
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
