package com.eleygi.crud.student;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import com.amazonaws.services.lambda.runtime.tests.annotations.HandlerParams;
import com.amazonaws.services.lambda.runtime.tests.annotations.Response;
import com.eleygi.crud.student.dao.IDao;
import com.eleygi.crud.student.model.Student;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteStudentLambdaFunctionTest {

    private static final String ID = "0f2431a2-6d2f-11e7-b799-5152aa497861";
    @Mock
    private Context context;
    @Mock
    private IDao<Student> dao;

    @ParameterizedTest
    @HandlerParams(
            event = @Event(value = "delete_student/requests/ok.json", type = APIGatewayProxyRequestEvent.class),
            response = @Response(value = "delete_student/responses/ok.json", type = APIGatewayProxyResponseEvent.class)
    )
    void handleRequest(APIGatewayProxyRequestEvent request, APIGatewayProxyResponseEvent response) {
        APIGatewayProxyResponseEvent result = new DeleteStudentLambdaFunction(dao).handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(response.getStatusCode());
        assertThat(result.getBody()).isEqualTo(response.getBody());

        verify(dao).delete(ID);
        verifyNoMoreInteractions(dao);
    }

    @ParameterizedTest
    @Event(value = "delete_student/requests/ok.json", type = APIGatewayProxyRequestEvent.class)
    void handleRequest_internalServerError(APIGatewayProxyRequestEvent request) {
        doThrow(DynamoDbException.class).when(dao).delete(ID);

        APIGatewayProxyResponseEvent result = new DeleteStudentLambdaFunction(dao).handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isEqualTo("{\"error\":\"Internal Server Error\", \"message\":\"An unexpected error occurred\"}");

        verify(dao).delete(ID);
        verifyNoMoreInteractions(dao);
    }

    @ParameterizedTest
    @HandlerParams(
            event = @Event(value = "delete_student/requests/bad_request.json", type = APIGatewayProxyRequestEvent.class),
            response = @Response(value = "delete_student/responses/bad_request.json", type = APIGatewayProxyResponseEvent.class)
    )
    void handleRequest_badRequest(APIGatewayProxyRequestEvent request, APIGatewayProxyResponseEvent response) {
        APIGatewayProxyResponseEvent result = new DeleteStudentLambdaFunction(dao).handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(response.getStatusCode());
        assertThat(result.getBody()).isEqualTo(response.getBody());

        verifyNoInteractions(dao);
    }
}