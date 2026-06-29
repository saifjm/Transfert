package com.smi.mstr.transfer.application.workflow;

import com.smi.mstr.transfer.domain.enums.WorkflowNodeCode;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkflowNodeCommandRegistry {

    private final Map<WorkflowNodeCode, WorkflowNodeCommandHandler> handlers;

    public WorkflowNodeCommandRegistry(List<WorkflowNodeCommandHandler> handlerList) {
        this.handlers = new EnumMap<>(WorkflowNodeCode.class);

        for (WorkflowNodeCommandHandler handler : handlerList) {
            for (WorkflowNodeCode nodeCode : WorkflowNodeCode.values()) {
                if (handler.supports(nodeCode)) {
                    handlers.put(nodeCode, handler);
                }
            }
        }
    }

    public WorkflowNodeCommandHandler getHandler(WorkflowNodeCode nodeCode) {
        WorkflowNodeCommandHandler handler = handlers.get(nodeCode);

        if (handler == null) {
            throw new IllegalArgumentException(
                    "No workflow command handler configured for node: " + nodeCode
            );
        }

        return handler;
    }
}