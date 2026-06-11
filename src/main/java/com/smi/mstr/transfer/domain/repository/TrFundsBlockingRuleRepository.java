package com.smi.mstr.transfer.domain.repository;

import com.smi.mstr.transfer.domain.entity.TrFundsBlockingRule;
import com.smi.mstr.transfer.domain.enums.YesNoFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrFundsBlockingRuleRepository extends JpaRepository<TrFundsBlockingRule, Long> {

    List<TrFundsBlockingRule> findByEnabledOrderByPriorityNoAsc(YesNoFlag enabled);
}