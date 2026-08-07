package com.crossmind.ai.agent;

import com.crossmind.ai.model.AgentType;

public interface AnalysisAgent {

    AgentType type();

    Object execute(AgentContext context);
}

