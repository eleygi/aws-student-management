package com.eleygi.crud.student;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import com.amazonaws.services.lambda.runtime.tests.annotations.Events;
import com.amazonaws.services.lambda.runtime.tests.annotations.HandlerParams;
import com.amazonaws.services.lambda.runtime.tests.annotations.Responses;
import com.eleygi.crud.student.dao.IDao;
import com.eleygi.crud.student.model.Student;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CreateStudentLambdaFunctionTest {

    @Mock
    private Context context;
    @Mock
    private IDao<Student> dao;

    @ParameterizedTest
    @HandlerParams(
            events = @Events(folder = "create_student/requests", type = APIGatewayProxyRequestEvent.class),
            responses = @Responses(folder = "create_student/responses", type = APIGatewayProxyResponseEvent.class)
    )
    void handleRequest(APIGatewayProxyRequestEvent request, APIGatewayProxyResponseEvent response) {
        CreateStudentLambdaFunction handler = new CreateStudentLambdaFunction(dao);
        APIGatewayProxyResponseEvent result = handler.handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(response.getStatusCode());
        assertThat(result.getBody()).isEqualTo(response.getBody());

        if (response.getStatusCode() == HttpStatusCode.CREATED) {
            verifyDaoInteractions();

        } else if (response.getStatusCode() == HttpStatusCode.BAD_REQUEST) {
            Mockito.verifyNoInteractions(dao);
        }

    }

    @ParameterizedTest
    @Event(value = "/create_student/requests/ok.json", type = APIGatewayProxyRequestEvent.class)
    void handleRequest_internalError(APIGatewayProxyRequestEvent request) {
        CreateStudentLambdaFunction handler = new CreateStudentLambdaFunction(dao);

        Mockito.doThrow(DynamoDbException.class).when(dao).create(Mockito.any(Student.class));
        APIGatewayProxyResponseEvent result = handler.handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(500);
        assertThat(result.getBody()).isEqualTo("{\"error\":\"Internal Server Error\", \"message\":\"An unexpected error occurred\"}");

        verifyDaoInteractions();
    }

    private void verifyDaoInteractions() {
        ArgumentCaptor<Student> argumentCaptor = ArgumentCaptor.forClass(Student.class);
        Mockito.verify(dao).create(argumentCaptor.capture());
        Student capturedArgument = argumentCaptor.getValue();
        assertThat(capturedArgument.getId()).isEqualTo("0f2431a2-6d2f-11e7-b799-5152aa497861");
        assertThat(capturedArgument.getFirstName()).isEqualTo("Jane");
        assertThat(capturedArgument.getLastName()).isEqualTo("Doe");
        assertThat(capturedArgument.getClassroom()).isEqualTo("PSMS1");
        Mockito.verifyNoMoreInteractions(dao);
    }
}