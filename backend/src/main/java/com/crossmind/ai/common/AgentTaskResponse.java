package com.crossmind.ai.common;

import com.crossmind.ai.model.AgentTaskStatus;
import com.crossmind.ai.model.AgentType;

public record AgentTaskResponse(AgentType agentType, AgentTaskStatus status) {
}

