package com.eleygi.crud.student;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.tests.annotations.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateStudentLambdaFunctionTest {

    private static final String ID = "0f2431a2-6d2f-11e7-b799-5152aa497861";
    @Mock
    private Context context;
    @Mock
    private IDao<Student> dao;

    @ParameterizedTest
    @Event(value = "update_student/requests/ok.json", type = APIGatewayProxyRequestEvent.class)
    void handleRequest(APIGatewayProxyRequestEvent request) {
        when(dao.get(ID)).thenReturn(new Student(ID, "Jane", "Doe", "PSMS1"));
        APIGatewayProxyResponseEvent result = new UpdateStudentLambdaFunction(dao).handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.OK);
        assertThat(result.getBody()).isEqualTo("{\"message\":\"student 0f2431a2-6d2f-11e7-b799-5152aa497861 updated\"}");

        verifyDaoInteractions();
    }

    @ParameterizedTest
    @Event(value = "/update_student/requests/ok.json", type = APIGatewayProxyRequestEvent.class)
    void handleRequest_internalError(APIGatewayProxyRequestEvent request) {
        UpdateStudentLambdaFunction handler = new UpdateStudentLambdaFunction(dao);

        when(dao.get(ID)).thenReturn(new Student(ID, "Jane", "Doe", "PSMS1"));
        doThrow(DynamoDbException.class).when(dao).update(Mockito.any(Student.class));
        APIGatewayProxyResponseEvent result = handler.handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isEqualTo("{\"error\":\"Internal Server Error\", \"message\":\"An unexpected error occurred\"}");

        verifyDaoInteractions();
    }

    @ParameterizedTest
    @Event(value = "update_student/requests/ok.json", type = APIGatewayProxyRequestEvent.class)
    void handleRequest_notFound(APIGatewayProxyRequestEvent request) {
        when(dao.get(ID)).thenReturn(null);

        APIGatewayProxyResponseEvent result = new UpdateStudentLambdaFunction(dao).handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo("{\"error\":\"Not found\", \"message\":\"student 0f2431a2-6d2f-11e7-b799-5152aa497861 not found\"}");

        verify(dao).get(ID);
        verifyNoMoreInteractions(dao);
    }

    @ParameterizedTest
    @HandlerParams(
            events = @Events(
                    events = {
                            @Event(value = "update_student/requests/bad_request.json"),
                            @Event(value = "update_student/requests/invalid_student.json"),
                            @Event(value = "update_student/requests/malformed_student.json")
                    },
                    type = APIGatewayProxyRequestEvent.class

            ),
            responses = @Responses(
                    responses = {
                            @Response("update_student/responses/bad_request.json"),
                            @Response("update_student/responses/invalid_student.json"),
                            @Response("update_student/responses/malformed_student.json")
                    },
                    type = APIGatewayProxyResponseEvent.class
            )
    )
    void handleRequest_badRequest(APIGatewayProxyRequestEvent request, APIGatewayProxyResponseEvent response) {
        UpdateStudentLambdaFunction handler = new UpdateStudentLambdaFunction(dao);
        APIGatewayProxyResponseEvent result = handler.handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(response.getStatusCode());
        assertThat(result.getBody()).isEqualTo(response.getBody());

        verifyNoInteractions(dao);
    }

    private void verifyDaoInteractions() {
        verify(dao).get(ID);
        ArgumentCaptor<Student> argumentCaptor = ArgumentCaptor.forClass(Student.class);
        Mockito.verify(dao).update(argumentCaptor.capture());
        Student capturedArgument = argumentCaptor.getValue();
        assertThat(capturedArgument.getId()).isEqualTo(ID);
        assertThat(capturedArgument.getFirstName()).isEqualTo("Jane");
        assertThat(capturedArgument.getLastName()).isEqualTo("Doe");
        assertThat(capturedArgument.getClassroom()).isEqualTo("PSGS8");
        Mockito.verifyNoMoreInteractions(dao);
    }
}