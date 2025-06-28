package com.boycottpro.userboycotts;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.boycottpro.models.ResponseMessage;
import com.boycottpro.userboycotts.models.CompanySummary;
import com.boycottpro.userboycotts.models.ResponsePojo;
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
                ResponseMessage message = new ResponseMessage(400,
                        "sorry, there was an error processing your request",
                        "user_id not present");
                String responseBody = objectMapper.writeValueAsString(message);
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withHeaders(Map.of("Content-Type", "application/json"))
                        .withBody(responseBody);
            }
            String causeId = (pathParams != null) ? pathParams.get("cause_id") : null;
            if (causeId == null || causeId.isEmpty()) {
                ResponseMessage message = new ResponseMessage(400,
                        "sorry, there was an error processing your request",
                        "cause_id not present");
                String responseBody = objectMapper.writeValueAsString(message);
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withHeaders(Map.of("Content-Type", "application/json"))
                        .withBody(responseBody);
            }
            ResponsePojo userBoycotts = getUserBoycottsByCause(userId, causeId);
            String responseBody = objectMapper.writeValueAsString(userBoycotts);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseMessage message = new ResponseMessage(500,
                    "sorry, there was an error processing your request",
                    "Unexpected server error: " + e.getMessage());
            String responseBody = null;
            try {
                responseBody = objectMapper.writeValueAsString(message);
            } catch (JsonProcessingException ex) {
                System.out.println("json processing exception");
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(responseBody);
        }
    }

    private ResponsePojo getUserBoycottsByCause(String userId, String causeId) {
        QueryRequest request = QueryRequest.builder()
                .tableName("user_boycotts")
                .keyConditionExpression("user_id = :uid")
                .expressionAttributeValues(Map.of(":uid", AttributeValue.fromS(userId)))
                .build();

        QueryResponse response = dynamoDb.query(request);

        List<Map<String, AttributeValue>> matchingRecords = response.items().stream()
                .filter(item -> item.containsKey("cause_id") &&
                        item.get("cause_id").s().equals(causeId))
                .collect(Collectors.toList());
        if (matchingRecords.isEmpty()) {
            // No boycott found for this user+company
            ResponsePojo result = new ResponsePojo();
            result.setFollowing(false);
            return result;
        }

        // Filter by cause_id and map to POJOs
        List<CompanySummary> companies = matchingRecords.stream()
                .filter(item -> item.containsKey("cause_id") &&
                        causeId.equals(item.get("cause_id").s()))
                .map(item -> {
                    CompanySummary company = new CompanySummary();
                    company.setCompany_id(item.getOrDefault("company_id", AttributeValue.fromS("")).s());
                    company.setCompany_name(item.getOrDefault("company_name", AttributeValue.fromS("")).s());
                    return company;
                })
                .collect(Collectors.toList());
        // Find the record with the earliest timestamp
        Map<String, AttributeValue> earliest = matchingRecords.stream()
                .filter(item -> item.containsKey("timestamp") && item.get("timestamp").s() != null && !item.get("timestamp").s().isEmpty())
                .min(Comparator.comparing(item -> item.get("timestamp").s()))
                .orElse(null);
        // Populate final response
        ResponsePojo result = new ResponsePojo();
        result.setFollowing(true);
        result.setCompanies(companies);
        result.setCause_id(causeId);
        result.setCause_desc(earliest.getOrDefault("cause_desc", AttributeValue.fromS("")).s());
        result.setFollowingSince(earliest.getOrDefault("timestamp", AttributeValue.fromS("0")).s());
        return result;
    }

}