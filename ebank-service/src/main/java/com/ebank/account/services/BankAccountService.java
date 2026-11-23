package com.ebank.account.services;

import com.ebank.account.entities.AccountOperation;
import com.ebank.account.entities.BankAccount;
import com.ebank.account.enums.OperationType;
import com.ebank.account.dto.AccountOperationDto;
import com.ebank.account.dto.BankAccountDto;
import com.ebank.account.dto.BankAccountRequest;
import com.ebank.account.repositories.AccountOperationRepository;
import com.ebank.account.repositories.BankAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository,
                              AccountOperationRepository accountOperationRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.accountOperationRepository = accountOperationRepository;
    }

    @Transactional(readOnly = true)
    public List<BankAccount> getAllAccounts() {
        return bankAccountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BankAccount getAccount(String id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account " + id + " not found"));
    }

    public BankAccount createAccount(BankAccountRequest request) {
        if (request.getCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }
        if (request.getType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type is required");
        }
        BankAccount account = new BankAccount();
        account.setId(UUID.randomUUID().toString());
        account.setCreatedAt(new Date());
        account.setBalance(request.getBalance());
        account.setCurrency(request.getCurrency() != null ? request.getCurrency() : "MAD");
        account.setType(request.getType());
        account.setCustomerId(request.getCustomerId());
        return bankAccountRepository.save(account);
    }

    public BankAccount debit(String accountId, double amount, String description) {
        checkAmount(amount);
        BankAccount account = getAccount(accountId);
        if (account.getBalance() < amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Balance not sufficient");
        }
        accountOperationRepository.save(new AccountOperation(null, new Date(), amount,
                OperationType.DEBIT, description, account));
        account.setBalance(account.getBalance() - amount);
        return bankAccountRepository.save(account);
    }

    public BankAccount credit(String accountId, double amount, String description) {
        checkAmount(amount);
        BankAccount account = getAccount(accountId);
        accountOperationRepository.save(new AccountOperation(null, new Date(), amount,
                OperationType.CREDIT, description, account));
        account.setBalance(account.getBalance() + amount);
        return bankAccountRepository.save(account);
    }

    public void transfer(String accountSource, String accountDestination, double amount) {
        if (accountSource == null || accountDestination == null || accountSource.equals(accountDestination)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Source and destination accounts must be provided and different");
        }
        debit(accountSource, amount, "Transfer to " + accountDestination);
        credit(accountDestination, amount, "Transfer from " + accountSource);
    }

    // ------------------------------------------------------------------
    // Vues DTO (utilisees par les outils MCP, sans dependance a la session JPA)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BankAccountDto> getAllAccountDtos() {
        return bankAccountRepository.findAll().stream()
                .map(a -> toDto(a, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BankAccountDto getAccountDto(String id) {
        return toDto(getAccount(id), true);
    }

    @Transactional(readOnly = true)
    public List<BankAccountDto> getAccountDtosByCustomerId(Long customerId) {
        return bankAccountRepository.findByCustomerId(customerId).stream()
                .map(a -> toDto(a, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountOperationDto> getOperations(String accountId) {
        getAccount(accountId);
        return accountOperationRepository.findByBankAccountIdOrderByIdDesc(accountId).stream()
                .map(this::toOperationDto)
                .collect(Collectors.toList());
    }

    private BankAccountDto toDto(BankAccount account, boolean withOperations) {
        BankAccountDto dto = new BankAccountDto(
                account.getId(),
                account.getCreatedAt() != null ? account.getCreatedAt().toInstant().toString() : null,
                account.getBalance(),
                account.getCurrency(),
                account.getType(),
                account.getCustomerId());
        if (withOperations) {
            dto.setOperations(accountOperationRepository.findByBankAccountIdOrderByIdDesc(account.getId())
                    .stream()
                    .map(this::toOperationDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private AccountOperationDto toOperationDto(AccountOperation operation) {
        return new AccountOperationDto(
                operation.getId(),
                operation.getOperationDate() != null ? operation.getOperationDate().toInstant().toString() : null,
                operation.getAmount(),
                operation.getType(),
                operation.getDescription());
    }

    private void checkAmount(double amount) {
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than 0");
        }
    }
}
