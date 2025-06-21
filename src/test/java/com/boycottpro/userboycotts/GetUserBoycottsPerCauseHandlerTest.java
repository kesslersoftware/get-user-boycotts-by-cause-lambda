package com.boycottpro.userboycotts;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetUserBoycottsPerCauseHandlerTest {

    @Mock
    private DynamoDbClient dynamoDb;

    @InjectMocks
    private GetUserBoycottsPerCauseHandler handler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testHandleRequest_validRequest_returnsBoycotts() throws Exception {
        String userId = "user123";
        String causeId = "cause456";

        Map<String, AttributeValue> item = Map.of(
                "user_id", AttributeValue.fromS(userId),
                "company_id", AttributeValue.fromS("comp123"),
                "company_name", AttributeValue.fromS("TestCorp"),
                "cause_id", AttributeValue.fromS(causeId),
                "cause_desc", AttributeValue.fromS("Environmental harm"),
                "company_cause_id", AttributeValue.fromS("comp123#cause456"),
                "timestamp", AttributeValue.fromS("2025-06-20T10:00:00Z")
        );

        when(dynamoDb.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder().items(List.of(item)).build());

        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("user_id", userId, "cause_id", causeId));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, mock(Context.class));

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("TestCorp"));
        assertTrue(response.getBody().contains("Environmental harm"));
    }

    @Test
    public void testHandleRequest_missingUserId_returns400() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("cause_id", "cause456"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, mock(Context.class));

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing user_id"));
    }

    @Test
    public void testHandleRequest_missingCauseId_returns400() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("user_id", "user123"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, mock(Context.class));

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Missing cause_id"));
    }

    @Test
    public void testHandleRequest_dynamoThrowsException_returns500() {
        when(dynamoDb.query(any(QueryRequest.class)))
                .thenThrow(RuntimeException.class);

        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withPathParameters(Map.of("user_id", "user123", "cause_id", "cause456"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, mock(Context.class));

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Unexpected server error"));
    }
}
