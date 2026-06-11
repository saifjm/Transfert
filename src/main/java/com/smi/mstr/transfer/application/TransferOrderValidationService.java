package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrAccount;
import com.smi.mstr.transfer.domain.entity.TrFinancialAgent;
import com.smi.mstr.transfer.domain.entity.TrParty;
import com.smi.mstr.transfer.domain.enums.*;
import com.smi.mstr.transfer.dto.TransferValidationReport;
import com.smi.mstr.transfer.dto.ValidationErrorDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Service
public class TransferOrderValidationService {

    public TransferValidationReport validateForInputControl(MvtTrOperation operation) {
        List<ValidationErrorDto> errors = new ArrayList<>();

        validateDebtor(operation, errors);
        validateCreditor(operation, errors);
        validateAmountAndPurpose(operation, errors);

        boolean hasBlockingError = errors.stream()
                .anyMatch(error -> error.severity() == ValidationSeverity.BLOCKING);

        return new TransferValidationReport(
                operation.getOperationRef(),
                !hasBlockingError,
                LocalDateTime.now(),
                errors
        );
    }

    private void validateDebtor(
            MvtTrOperation operation,
            List<ValidationErrorDto> errors
    ) {
        TrParty debtor = findParty(operation, PartyRole.DBTR);

        if (debtor == null) {
            add(errors, ValidationSection.DEBTOR, "debtor", "DEBTOR_REQUIRED",
                    "Debtor / ordering customer is required.", ValidationSeverity.BLOCKING);
            return;
        }

        if (blank(debtor.getName())) {
            add(errors, ValidationSection.DEBTOR, "debtor.name", "DEBTOR_NAME_REQUIRED",
                    "Debtor name is required.", ValidationSeverity.BLOCKING);
        }

        if (blank(debtor.getCountryOfResidence())) {
            add(errors, ValidationSection.DEBTOR, "debtor.countryOfResidence", "DEBTOR_COUNTRY_REQUIRED",
                    "Debtor country of residence is required.", ValidationSeverity.BLOCKING);
        }

        if (blank(debtor.getCustomerCode()) && blank(debtor.getLocalPartyId())) {
            add(errors, ValidationSection.DEBTOR, "debtor.customerCode", "DEBTOR_IDENTIFIER_REQUIRED",
                    "Debtor customer code or local identifier is required.", ValidationSeverity.BLOCKING);
        }

        TrAccount debtorAccount = findAccount(operation, AccountRole.DBTR_ACCT);

        if (debtorAccount == null) {
            add(errors, ValidationSection.DEBTOR_ACCOUNT, "debtorAccount", "DEBTOR_ACCOUNT_REQUIRED",
                    "Debtor account is required.", ValidationSeverity.BLOCKING);
            return;
        }

        if (!hasAnyAccountIdentifier(debtorAccount)) {
            add(errors, ValidationSection.DEBTOR_ACCOUNT, "debtorAccount", "DEBTOR_ACCOUNT_IDENTIFIER_REQUIRED",
                    "Debtor account must contain IBAN, core account ID, RIB or another account identifier.",
                    ValidationSeverity.BLOCKING);
        }
    }

    private void validateCreditor(
            MvtTrOperation operation,
            List<ValidationErrorDto> errors
    ) {
        TrParty creditor = findParty(operation, PartyRole.CDTR);

        if (creditor == null) {
            add(errors, ValidationSection.CREDITOR, "creditor", "CREDITOR_REQUIRED",
                    "Creditor / beneficiary is required.", ValidationSeverity.BLOCKING);
            return;
        }

        if (blank(creditor.getName())) {
            add(errors, ValidationSection.CREDITOR, "creditor.name", "CREDITOR_NAME_REQUIRED",
                    "Creditor name is required.", ValidationSeverity.BLOCKING);
        }

        if (blank(creditor.getCountryOfResidence())) {
            add(errors, ValidationSection.CREDITOR, "creditor.countryOfResidence", "CREDITOR_COUNTRY_REQUIRED",
                    "Creditor country is required.", ValidationSeverity.BLOCKING);
        }

        boolean hasAddress = creditor.getPostalAddresses() != null
                && creditor.getPostalAddresses().stream().anyMatch(address ->
                notBlank(address.getCountry())
                        || notBlank(address.getTownName())
                        || notBlank(address.getAddressLine1())
        );

        if (!hasAddress) {
            add(errors, ValidationSection.CREDITOR, "creditor.postalAddresses",
                    "CREDITOR_ADDRESS_REQUIRED",
                    "Creditor address is required.", ValidationSeverity.BLOCKING);
        }

        TrAccount creditorAccount = findAccount(operation, AccountRole.CDTR_ACCT);

        if (creditorAccount == null) {
            add(errors, ValidationSection.CREDITOR_ACCOUNT, "creditorAccount",
                    "CREDITOR_ACCOUNT_REQUIRED",
                    "Creditor account is required.", ValidationSeverity.BLOCKING);
        } else if (!hasAnyAccountIdentifier(creditorAccount)) {
            add(errors, ValidationSection.CREDITOR_ACCOUNT, "creditorAccount",
                    "CREDITOR_ACCOUNT_IDENTIFIER_REQUIRED",
                    "Creditor account must contain IBAN or another account identifier.",
                    ValidationSeverity.BLOCKING);
        }

        TrFinancialAgent creditorAgent = findFinancialAgent(operation, FinancialAgentRole.CDTR_AGT);

        if (creditorAgent == null) {
            add(errors, ValidationSection.CREDITOR_AGENT, "creditorAgent",
                    "CREDITOR_AGENT_REQUIRED",
                    "Beneficiary bank / creditor agent is required.", ValidationSeverity.BLOCKING);
            return;
        }

        boolean hasBankIdentifier =
                notBlank(creditorAgent.getBicfi())
                        || notBlank(creditorAgent.getClearingMemberId())
                        || notBlank(creditorAgent.getAgentName());

        if (!hasBankIdentifier) {
            add(errors, ValidationSection.CREDITOR_AGENT, "creditorAgent",
                    "CREDITOR_AGENT_IDENTIFIER_REQUIRED",
                    "Beneficiary bank must contain BICFI, clearing member ID or bank name.",
                    ValidationSeverity.BLOCKING);
        }

        if (notBlank(creditorAgent.getBicfi()) && !isValidBic(creditorAgent.getBicfi())) {
            add(errors, ValidationSection.CREDITOR_AGENT, "creditorAgent.bicfi",
                    "INVALID_BICFI_FORMAT",
                    "BICFI must contain 8 or 11 valid characters.",
                    ValidationSeverity.BLOCKING);
        }
    }

    private void validateAmountAndPurpose(
            MvtTrOperation operation,
            List<ValidationErrorDto> errors
    ) {
        if (operation.getTransferAmount() == null) {
            add(errors, ValidationSection.AMOUNT_PURPOSE, "transferAmount",
                    "TRANSFER_AMOUNT_REQUIRED",
                    "Transfer amount is required.", ValidationSeverity.BLOCKING);
        } else if (operation.getTransferAmount().compareTo(BigDecimal.ZERO) <= 0) {
            add(errors, ValidationSection.AMOUNT_PURPOSE, "transferAmount",
                    "TRANSFER_AMOUNT_POSITIVE_REQUIRED",
                    "Transfer amount must be strictly positive.", ValidationSeverity.BLOCKING);
        }

        if (blank(operation.getTransferCurrency())) {
            add(errors, ValidationSection.AMOUNT_PURPOSE, "transferCurrency",
                    "TRANSFER_CURRENCY_REQUIRED",
                    "Transfer currency is required.", ValidationSeverity.BLOCKING);
        } else if (!isValidCurrency(operation.getTransferCurrency())) {
            add(errors, ValidationSection.AMOUNT_PURPOSE, "transferCurrency",
                    "INVALID_TRANSFER_CURRENCY",
                    "Transfer currency must be a valid ISO 4217 currency code.",
                    ValidationSeverity.BLOCKING);
        }

        boolean hasPurpose =
                notBlank(operation.getPurposeCode())
                        || notBlank(operation.getPurposeProprietary())
                        || notBlank(operation.getRemittanceUnstructured());

        if (!hasPurpose) {
            add(errors, ValidationSection.AMOUNT_PURPOSE, "purpose",
                    "PURPOSE_REQUIRED",
                    "Economic purpose / transfer reason is required.",
                    ValidationSeverity.BLOCKING);
        }

        if (notBlank(operation.getChargeBearer())
                && !List.of("SHAR", "OUR", "BEN", "DEBT", "CRED", "SLEV").contains(operation.getChargeBearer())) {
            add(errors, ValidationSection.AMOUNT_PURPOSE, "chargeBearer",
                    "INVALID_CHARGE_BEARER",
                    "Charge bearer must be SHAR, OUR, BEN, DEBT, CRED or SLEV.",
                    ValidationSeverity.BLOCKING);
        }
    }

    private void add(
            List<ValidationErrorDto> errors,
            ValidationSection section,
            String fieldPath,
            String errorCode,
            String message,
            ValidationSeverity severity
    ) {
        errors.add(new ValidationErrorDto(
                section,
                fieldPath,
                errorCode,
                message,
                severity
        ));
    }

    private TrParty findParty(MvtTrOperation operation, PartyRole role) {
        if (operation.getParties() == null) {
            return null;
        }

        return operation.getParties()
                .stream()
                .filter(party -> party.getPartyRole() == role)
                .findFirst()
                .orElse(null);
    }

    private TrAccount findAccount(MvtTrOperation operation, AccountRole role) {
        if (operation.getAccounts() == null) {
            return null;
        }

        return operation.getAccounts()
                .stream()
                .filter(account -> account.getAccountRole() == role)
                .findFirst()
                .orElse(null);
    }

    private TrFinancialAgent findFinancialAgent(MvtTrOperation operation, FinancialAgentRole role) {
        if (operation.getFinancialAgents() == null) {
            return null;
        }

        return operation.getFinancialAgents()
                .stream()
                .filter(agent -> agent.getAgentRole() == role)
                .findFirst()
                .orElse(null);
    }

    private boolean hasAnyAccountIdentifier(TrAccount account) {
        return notBlank(account.getIban())
                || notBlank(account.getOtherAccountId())
                || notBlank(account.getCoreAccountId())
                || notBlank(account.getRibLocal());
    }

    private boolean isValidBic(String bicfi) {
        return bicfi != null && bicfi.matches("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    }

    private boolean isValidCurrency(String currency) {
        try {
            Currency.getInstance(currency);
            return currency.length() == 3;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}