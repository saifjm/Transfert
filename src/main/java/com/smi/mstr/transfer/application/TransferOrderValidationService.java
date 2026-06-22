package com.smi.mstr.transfer.application;

import com.smi.mstr.transfer.domain.entity.MvtTrOperation;
import com.smi.mstr.transfer.domain.entity.TrAccount;
import com.smi.mstr.transfer.domain.entity.TrParty;
import com.smi.mstr.transfer.domain.enums.AccountRole;
import com.smi.mstr.transfer.domain.enums.PartyRole;
import com.smi.mstr.transfer.domain.enums.ValidationSection;
import com.smi.mstr.transfer.domain.enums.ValidationSeverity;
import com.smi.mstr.transfer.dto.TransferValidationReport;
import com.smi.mstr.transfer.dto.ValidationErrorDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.ArrayList;

@Service
public class TransferOrderValidationService {

    public TransferValidationReport validateForInputControl(MvtTrOperation operation) {
        List<ValidationErrorDto> errors = new ArrayList<>();

        validateOperationHeader(operation, errors);

        /*
         * Important:
         * If operation is null, validateOperationHeader already added the blocking error.
         * Do not continue to debtor/creditor/amount validation.
         */
        if (operation != null) {
            validateDebtor(operation, errors);
            validateCreditor(operation, errors);
            validateAmountAndPurpose(operation, errors);
        }

        boolean hasBlockingError = errors.stream()
                .anyMatch(error -> error.severity() == ValidationSeverity.BLOCKING);

        return new TransferValidationReport(
                resolveOperationReference(operation),
                !hasBlockingError,
                LocalDateTime.now(),
                errors
        );
    }

    private void validateOperationHeader(
            MvtTrOperation operation,
            List<ValidationErrorDto> errors
    ) {
        if (operation == null) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "operation",
                    "OPERATION_REQUIRED",
                    "Transfer operation is required.",
                    ValidationSeverity.BLOCKING);
            return;
        }

        if (operation.getCodeOperation() == null) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "codeOperation",
                    "CODE_OPERATION_REQUIRED",
                    "Operation code is required.",
                    ValidationSeverity.BLOCKING);
        }

        if (operation.getTypeTransfert() == null) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "typeTransfert",
                    "TRANSFER_TYPE_REQUIRED",
                    "Transfer type is required.",
                    ValidationSeverity.BLOCKING);
        }

        if (blank(operation.getCodeAgence())) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "codeAgence",
                    "AGENCY_CODE_REQUIRED",
                    "Agency code is required.",
                    ValidationSeverity.BLOCKING);
        }

        if (blank(operation.getNumDossier())) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "numDossier",
                    "DOSSIER_NUMBER_REQUIRED",
                    "Dossier number is required.",
                    ValidationSeverity.BLOCKING);
        }

        if (operation.getDateDossier() == null) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "dateDossier",
                    "DOSSIER_DATE_REQUIRED",
                    "Dossier date is required.",
                    ValidationSeverity.BLOCKING);
        }
    }

    private void validateDebtor(
            MvtTrOperation operation,
            List<ValidationErrorDto> errors
    ) {
        TrParty debtor = findParty(operation, PartyRole.DBTR);

        if (debtor == null) {
            add(errors,
                    ValidationSection.DEBTOR,
                    "debtor",
                    "DEBTOR_REQUIRED",
                    "Debtor / ordering customer is required.",
                    ValidationSeverity.BLOCKING);
            return;
        }

        if (blank(debtor.getName())) {
            add(errors,
                    ValidationSection.DEBTOR,
                    "debtor.name",
                    "DEBTOR_NAME_REQUIRED",
                    "Debtor name is required.",
                    ValidationSeverity.BLOCKING);
        }

        if (blank(debtor.getCountryOfResidence())) {
            add(errors,
                    ValidationSection.DEBTOR,
                    "debtor.countryOfResidence",
                    "DEBTOR_COUNTRY_REQUIRED",
                    "Debtor country of residence is required.",
                    ValidationSeverity.BLOCKING);
        }

        /*
         * Debit account is intentionally not validated here.
         *
         * In our design, the debit/funding resource is controlled by EPIC 3:
         * payment modalities, availability check, and security/blocking.
         */
    }

    private void validateCreditor(
            MvtTrOperation operation,
            List<ValidationErrorDto> errors
    ) {
        TrParty creditor = findParty(operation, PartyRole.CDTR);
        TrAccount creditorAccount = findAccount(operation, AccountRole.CDTR_ACCT);

        if (creditor == null) {
            add(errors,
                    ValidationSection.CREDITOR,
                    "creditor",
                    "CREDITOR_REQUIRED",
                    "Creditor / beneficiary is required.",
                    ValidationSeverity.BLOCKING);
            return;
        }

        if (blank(creditor.getName())) {
            add(errors,
                    ValidationSection.CREDITOR,
                    "creditor.name",
                    "CREDITOR_NAME_REQUIRED",
                    "Creditor name is required.",
                    ValidationSeverity.BLOCKING);
        }

        if (blank(creditor.getCountryOfResidence())) {
            add(errors,
                    ValidationSection.CREDITOR,
                    "creditor.countryOfResidence",
                    "CREDITOR_COUNTRY_REQUIRED",
                    "Creditor country of residence is required.",
                    ValidationSeverity.BLOCKING);
        }

        if (creditorAccount == null || !hasAnyAccountIdentifier(creditorAccount)) {
            add(errors,
                    ValidationSection.CREDITOR_ACCOUNT,
                    "creditorAccount",
                    "CREDITOR_ACCOUNT_REQUIRED",
                    "Creditor account is required.",
                    ValidationSeverity.BLOCKING);
        }
    }

    private void validateAmountAndPurpose(
            MvtTrOperation operation,
            List<ValidationErrorDto> errors
    ) {
        validateOrderAmount(operation, errors);
        validateTransferAmount(operation, errors);
        validatePurpose(operation, errors);
        validateChargeBearer(operation, errors);
    }

    private void validateOrderAmount(
            MvtTrOperation operation,
            List<ValidationErrorDto> errors
    ) {
        if (operation.getMntOrdre() != null
                && operation.getMntOrdre().compareTo(BigDecimal.ZERO) <= 0) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "mntOrdre",
                    "ORDER_AMOUNT_POSITIVE_REQUIRED",
                    "Order amount must be strictly positive when provided.",
                    ValidationSeverity.BLOCKING);
        }

        if (notBlank(operation.getCodeDeviseOrdre())
                && !isValidCurrency(operation.getCodeDeviseOrdre())) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "codeDeviseOrdre",
                    "INVALID_ORDER_CURRENCY",
                    "Order currency must be a valid ISO 4217 currency code.",
                    ValidationSeverity.BLOCKING);
        }
    }

    private void validateTransferAmount(
            MvtTrOperation operation,
            List<ValidationErrorDto> errors
    ) {
        if (operation.getMntDevise() == null) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "mntDevise",
                    "TRANSFER_AMOUNT_REQUIRED",
                    "Transfer amount is required.",
                    ValidationSeverity.BLOCKING);
        } else if (operation.getMntDevise().compareTo(BigDecimal.ZERO) <= 0) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "mntDevise",
                    "TRANSFER_AMOUNT_POSITIVE_REQUIRED",
                    "Transfer amount must be strictly positive.",
                    ValidationSeverity.BLOCKING);
        }

        if (blank(operation.getCodeDevise())) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "codeDevise",
                    "TRANSFER_CURRENCY_REQUIRED",
                    "Transfer currency is required.",
                    ValidationSeverity.BLOCKING);
        } else if (!isValidCurrency(operation.getCodeDevise())) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "codeDevise",
                    "INVALID_TRANSFER_CURRENCY",
                    "Transfer currency must be a valid ISO 4217 currency code.",
                    ValidationSeverity.BLOCKING);
        }
    }

    private void validatePurpose(
            MvtTrOperation operation,
            List<ValidationErrorDto> errors
    ) {
        boolean hasPurpose =
                notBlank(operation.getPurposeCode())
                        || notBlank(operation.getPurposeProprietary())
                        || notBlank(operation.getRemittanceUnstructured());

        if (!hasPurpose) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "purpose",
                    "PURPOSE_REQUIRED",
                    "Economic purpose / transfer reason is required.",
                    ValidationSeverity.BLOCKING);
        }
    }

    private void validateChargeBearer(
            MvtTrOperation operation,
            List<ValidationErrorDto> errors
    ) {
        if (blank(operation.getChargeBearer())) {
            return;
        }

        if (!List.of("SHAR", "OUR", "BEN", "DEBT", "CRED", "SLEV")
                .contains(operation.getChargeBearer())) {
            add(errors,
                    ValidationSection.AMOUNT_PURPOSE,
                    "chargeBearer",
                    "INVALID_CHARGE_BEARER",
                    "Charge bearer must be SHAR, OUR, BEN, DEBT, CRED or SLEV.",
                    ValidationSeverity.BLOCKING);
        }
    }

    private TrParty findParty(MvtTrOperation operation, PartyRole role) {
        if (operation == null || operation.getParties() == null) {
            return null;
        }

        return operation.getParties()
                .stream()
                .filter(party -> party.getPartyRole() == role)
                .findFirst()
                .orElse(null);
    }

    private TrAccount findAccount(MvtTrOperation operation, AccountRole role) {
        if (operation == null || operation.getAccounts() == null) {
            return null;
        }

        return operation.getAccounts()
                .stream()
                .filter(account -> account.getAccountRole() == role)
                .findFirst()
                .orElse(null);
    }

    private boolean hasAnyAccountIdentifier(TrAccount account) {
        if (account == null) {
            return false;
        }

        return notBlank(account.getIban())
                || notBlank(account.getOtherAccountId())
                || notBlank(account.getCoreAccountId())
                || notBlank(account.getRibLocal());
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

    private boolean isValidCurrency(String currencyCode) {
        if (blank(currencyCode) || currencyCode.length() != 3) {
            return false;
        }

        try {
            Currency.getInstance(currencyCode);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String resolveOperationReference(MvtTrOperation operation) {
        if (operation == null) {
            return null;
        }

        if (notBlank(operation.getRefOrdre())) {
            return operation.getRefOrdre();
        }

        return operation.getRefOperation() == null
                ? null
                : String.valueOf(operation.getRefOperation());
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}