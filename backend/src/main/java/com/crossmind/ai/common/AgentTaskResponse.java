package com.crossmind.ai.common;

import com.crossmind.ai.model.AgentTaskStatus;
import com.crossmind.ai.model.AgentType;
import com.fasterxml.jackson.databind.JsonNode;

public record AgentTaskResponse(AgentType agentType, AgentTaskStatus status, JsonNode result) {
}
