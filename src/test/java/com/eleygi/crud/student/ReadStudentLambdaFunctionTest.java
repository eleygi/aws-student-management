package com.eleygi.crud.student;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import com.amazonaws.services.lambda.runtime.tests.annotations.HandlerParams;
import com.amazonaws.services.lambda.runtime.tests.annotations.Response;
import com.eleygi.crud.student.dao.IDao;
import com.eleygi.crud.student.model.Student;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONCompareMode;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@ExtendWith(MockitoExtension.class)
class ReadStudentLambdaFunctionTest {

    private static final String ID = "0f2431a2-6d2f-11e7-b799-5152aa497861";
    @Mock
    private Context context;
    @Mock
    private IDao<Student> dao;

    @ParameterizedTest
    @HandlerParams(
            event = @Event(value = "read_student/requests/ok.json", type = APIGatewayProxyRequestEvent.class),
            response = @Response(value = "read_student/responses/ok.json", type = APIGatewayProxyResponseEvent.class)
    )
    void handleRequest(APIGatewayProxyRequestEvent request, APIGatewayProxyResponseEvent response) throws JsonProcessingException, JSONException {
        Student student = new Student(ID, "Jane", "Doe", "PSMS1");
        when(dao.get(ID)).thenReturn(student);

        APIGatewayProxyResponseEvent result = new ReadStudentLambdaFunction(dao).handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(response.getStatusCode());
        assertEquals(new ObjectMapper().writeValueAsString(student), response.getBody(), JSONCompareMode.STRICT);

        verify(dao).get(ID);
        verifyNoMoreInteractions(dao);
    }

    @ParameterizedTest
    @Event(value = "read_student/requests/ok.json", type = APIGatewayProxyRequestEvent.class)
    void handleRequest_notFound(APIGatewayProxyRequestEvent request) {
        when(dao.get(ID)).thenReturn(null);

        APIGatewayProxyResponseEvent result = new ReadStudentLambdaFunction(dao).handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo(String.format("{\"error\":\"Not found\", \"message\":\"student %s not found\"}", ID));

        verify(dao).get(ID);
        verifyNoMoreInteractions(dao);
    }

    @ParameterizedTest
    @Event(value = "read_student/requests/ok.json", type = APIGatewayProxyRequestEvent.class)
    void handleRequest_internalServerError(APIGatewayProxyRequestEvent request) {
        doThrow(DynamoDbException.class).when(dao).get(ID);

        APIGatewayProxyResponseEvent result = new ReadStudentLambdaFunction(dao).handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isEqualTo("{\"error\":\"Internal Server Error\", \"message\":\"An unexpected error occurred\"}");

        verify(dao).get(ID);
        verifyNoMoreInteractions(dao);
    }

    @ParameterizedTest
    @HandlerParams(
            event = @Event(value = "read_student/requests/bad_request.json", type = APIGatewayProxyRequestEvent.class),
            response = @Response(value = "read_student/responses/bad_request.json", type = APIGatewayProxyResponseEvent.class)
    )
    void handleRequest_badRequest(APIGatewayProxyRequestEvent request, APIGatewayProxyResponseEvent response) {
        APIGatewayProxyResponseEvent result = new ReadStudentLambdaFunction(dao).handleRequest(request, context);

        assertThat(result.getStatusCode()).isEqualTo(response.getStatusCode());
        assertThat(result.getBody()).isEqualTo(response.getBody());

        verifyNoInteractions(dao);
    }

}