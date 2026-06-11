package com.smi.mstr.transfer.dto.normalized;

public record AccountDto(
        String iban,
        String otherAccountId,
        String accountScheme,
        String accountCurrency,
        String accountName,
        String coreAccountId,
        String ribLocal
) {}
