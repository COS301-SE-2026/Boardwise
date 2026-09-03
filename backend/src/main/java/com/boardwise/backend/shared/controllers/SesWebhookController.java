package com.boardwise.backend.shared.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Slf4j
@RestController
@RequestMapping("/api/webhooks/aws-ses")
public class SesWebhookController {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/bounces")
    public ResponseEntity<String> handleSesNotification(
        @RequestHeader(value = "x-amz-sns-message-type", required = false) String snsMessageType,
        @RequestBody String payload){
        try {
            JsonNode rootNode = objectMapper.readTree(payload);

            if("SubscriptionConfirmation".equals(snsMessageType)){
                String subscribeUrl = rootNode.get("SubscribeURL").asText();

                // AWS needs us to send a GET request to the subscribeURL to confirm
                restTemplate.getForObject(subscribeUrl, String.class);
                log.info("SNS Subscription confirmed");
                return ResponseEntity.ok("Confirmed");
            }

            if("Notification".equals(snsMessageType)){
                String message = rootNode.get("Message").asText();
                JsonNode messageNode = objectMapper.readTree(message);

                String notificationType = messageNode.get("notificationType").asText();

                if("Bounce".equals(notificationType)){
                    JsonNode bounce = messageNode.get("bounce");

                    JsonNode bouncedRecipients = bounce.get("bouncedRecipients");
                    for(JsonNode recipient : bouncedRecipients){
                        String email = recipient.get("emailAddress").asText();

                        // TODO: Mark the user's email as invalid in the db
                        log.info("Hard bounce detected for: " + email);
                    }
                }
            }

            return ResponseEntity.ok("Received");
        } catch (Exception e) {
            log.error("Something went wrong", e);
            return ResponseEntity.badRequest().body("Error processing webhook");
        }
    }
    
}
