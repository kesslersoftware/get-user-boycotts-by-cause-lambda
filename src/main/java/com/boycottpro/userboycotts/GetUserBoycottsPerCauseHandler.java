package com.boycottpro.userboycotts;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.boycottpro.models.UserBoycotts;
import com.boycottpro.models.UserCauses;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class GetUserBoycottsPerCauseHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String TABLE_NAME = "";
    private final DynamoDbClient dynamoDb;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GetUserBoycottsPerCauseHandler() {
        this.dynamoDb = DynamoDbClient.create();
    }

    public GetUserBoycottsPerCauseHandler(DynamoDbClient dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            Map<String, String> pathParams = event.getPathParameters();
            String userId = (pathParams != null) ? pathParams.get("user_id") : null;
            if (userId == null || userId.isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"error\":\"Missing user_id in path\"}");
            }
            String causeId = (pathParams != null) ? pathParams.get("cause_id") : null;
            if (causeId == null || causeId.isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"error\":\"Missing cause_id in path\"}");
            }
            List<UserBoycotts> userBoycotts = getUserBoycottsByCause(userId, causeId);
            String responseBody = objectMapper.writeValueAsString(userBoycotts);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(responseBody);
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\": \"Unexpected server error: " + e.getMessage() + "\"}");
        }
    }

    private List<UserBoycotts> getUserBoycottsByCause(String userId, String causeId) {
        QueryRequest request = QueryRequest.builder()
                .tableName("user_boycotts")
                .keyConditionExpression("user_id = :uid")
                .expressionAttributeValues(Map.of(":uid", AttributeValue.fromS(userId)))
                .build();

        QueryResponse response = dynamoDb.query(request);

        // Filter by cause_id and map to POJOs
        return response.items().stream()
                .filter(item -> item.containsKey("cause_id") &&
                        causeId.equals(item.get("cause_id").s()))
                .map(item -> {
                    UserBoycotts boycott = new UserBoycotts();
                    boycott.setUser_id(userId);
                    boycott.setCompany_id(item.getOrDefault("company_id", AttributeValue.fromS("")).s());
                    boycott.setCompany_name(item.getOrDefault("company_name", AttributeValue.fromS("")).s());
                    boycott.setCause_id(causeId);
                    boycott.setCause_desc(item.getOrDefault("cause_desc", AttributeValue.fromS("")).s());
                    boycott.setCompany_cause_id(item.getOrDefault("company_cause_id", AttributeValue.fromS("")).s());
                    boycott.setTimestamp(item.getOrDefault("timestamp", AttributeValue.fromS("")).s());
                    return boycott;
                })
                .collect(Collectors.toList());
    }

}