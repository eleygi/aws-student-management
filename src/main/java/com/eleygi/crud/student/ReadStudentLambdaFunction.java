package com.eleygi.crud.student;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.eleygi.crud.student.dao.IDao;
import com.eleygi.crud.student.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.utils.StringUtils;

public class ReadStudentLambdaFunction extends StudentRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadStudentLambdaFunction.class);

    public ReadStudentLambdaFunction() {
        super();
    }

    ReadStudentLambdaFunction(IDao<Student> dao) {
        super(dao);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        String id = apiGatewayProxyRequestEvent.getPathParameters().get("id");
        if (StringUtils.isBlank(id)) {
            return badRequest("missing student id");
        }

        try {
            Student student = dao.get(id);
            if(student == null){
                return notFound(String.format("student %s not found", id));
            }

            return ok(mapper.writeValueAsString(student));

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return internalServerError();
        }
    }

}
