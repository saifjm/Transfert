package com.smi.mstr.transfer.config;

import com.smi.mstr.transfer.domain.enums.WorkflowNodeCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.EnumMap;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "ms-tr.operation-codes")
public class MsTrOperationCodeProperties {

    @NotNull
    private Long defaultCommercial;

    @NotNull
    private Long defaultFinancial;

    @Valid
    private Map<WorkflowNodeCode, NodeOperationCodes> byNode = new EnumMap<>(WorkflowNodeCode.class);

    public Long getDefaultCommercial() {
        return defaultCommercial;
    }

    public void setDefaultCommercial(Long defaultCommercial) {
        this.defaultCommercial = defaultCommercial;
    }

    public Long getDefaultFinancial() {
        return defaultFinancial;
    }

    public void setDefaultFinancial(Long defaultFinancial) {
        this.defaultFinancial = defaultFinancial;
    }

    public Map<WorkflowNodeCode, NodeOperationCodes> getByNode() {
        return byNode;
    }

    public void setByNode(Map<WorkflowNodeCode, NodeOperationCodes> byNode) {
        this.byNode = byNode;
    }

    public static class NodeOperationCodes {

        private Long commercial;

        private Long financial;

        public Long getCommercial() {
            return commercial;
        }

        public void setCommercial(Long commercial) {
            this.commercial = commercial;
        }

        public Long getFinancial() {
            return financial;
        }

        public void setFinancial(Long financial) {
            this.financial = financial;
        }
    }
}
