package com.eleygi.crud.student;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.eleygi.crud.student.dao.IDao;
import com.eleygi.crud.student.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.utils.StringUtils;

public class DeleteStudentLambdaFunction extends StudentRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteStudentLambdaFunction.class);

    public DeleteStudentLambdaFunction() {
        super();
    }

    DeleteStudentLambdaFunction(IDao<Student> dao) {
        super(dao);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        String id = apiGatewayProxyRequestEvent.getPathParameters().get("id");
        if (StringUtils.isBlank(id)) {
            return badRequest("missing student id");
        }

        try {
            dao.delete(id);
            return ok(String.format("student %s deleted", id));

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return internalServerError();
        }
    }

}
