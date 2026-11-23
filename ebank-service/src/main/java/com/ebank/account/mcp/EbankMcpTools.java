package com.ebank.account.mcp;

import com.ebank.account.clients.CustomerRestClient;
import com.ebank.account.dto.AccountOperationDto;
import com.ebank.account.dto.BankAccountDto;
import com.ebank.account.dto.BankAccountRequest;
import com.ebank.account.entities.BankAccount;
import com.ebank.account.enums.AccountType;
import com.ebank.account.model.Customer;
import com.ebank.account.services.BankAccountService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Outils MCP exposes par ebank-service aux agents IA.
 * Le client de chaque compte est recupere via OpenFeign (avec Circuit Breaker).
 */
@Component
public class EbankMcpTools {

    private final BankAccountService bankAccountService;
    private final CustomerRestClient customerRestClient;

    public EbankMcpTools(BankAccountService bankAccountService, CustomerRestClient customerRestClient) {
        this.bankAccountService = bankAccountService;
        this.customerRestClient = customerRestClient;
    }

    @Tool(description = "Retourne la liste de tous les comptes bancaires avec leur solde, type, devise et client")
    public List<BankAccountDto> getAllAccounts() {
        List<BankAccountDto> accounts = bankAccountService.getAllAccountDtos();
        enrichWithCustomers(accounts);
        return accounts;
    }

    @Tool(description = "Retourne un compte bancaire avec son client et la liste de ses operations, a partir de son identifiant (UUID)")
    public BankAccountDto getAccountById(
            @ToolParam(description = "Identifiant (UUID) du compte bancaire") String accountId) {
        BankAccountDto account = bankAccountService.getAccountDto(accountId);
        account.setCustomer(customerRestClient.findCustomerById(account.getCustomerId()));
        return account;
    }

    @Tool(description = "Retourne tous les comptes bancaires appartenant a un client donne")
    public List<BankAccountDto> getAccountsByCustomerId(
            @ToolParam(description = "Identifiant numerique du client") Long customerId) {
        List<BankAccountDto> accounts = bankAccountService.getAccountDtosByCustomerId(customerId);
        enrichWithCustomers(accounts);
        return accounts;
    }

    @Tool(description = "Retourne l'historique des operations (debits et credits) d'un compte bancaire, de la plus recente a la plus ancienne")
    public List<AccountOperationDto> getAccountOperations(
            @ToolParam(description = "Identifiant (UUID) du compte bancaire") String accountId) {
        return bankAccountService.getOperations(accountId);
    }

    @Tool(description = "Cree un nouveau compte bancaire pour un client et retourne le compte cree")
    public BankAccountDto createAccount(
            @ToolParam(description = "Identifiant numerique du client proprietaire du compte") Long customerId,
            @ToolParam(description = "Type de compte : CURRENT_ACCOUNT ou SAVING_ACCOUNT") AccountType type,
            @ToolParam(description = "Solde initial du compte") double initialBalance,
            @ToolParam(description = "Devise du compte, par exemple MAD (MAD par defaut)", required = false) String currency) {
        BankAccount account = bankAccountService.createAccount(
                new BankAccountRequest(initialBalance, currency, type, customerId));
        BankAccountDto dto = bankAccountService.getAccountDto(account.getId());
        dto.setCustomer(customerRestClient.findCustomerById(dto.getCustomerId()));
        return dto;
    }

    @Tool(description = "Effectue un debit (retrait) sur un compte bancaire et retourne le compte mis a jour. Echoue si le solde est insuffisant")
    public BankAccountDto debitAccount(
            @ToolParam(description = "Identifiant (UUID) du compte a debiter") String accountId,
            @ToolParam(description = "Montant a debiter, strictement positif") double amount,
            @ToolParam(description = "Description de l'operation") String description) {
        BankAccount account = bankAccountService.debit(accountId, amount, description);
        return bankAccountService.getAccountDto(account.getId());
    }

    @Tool(description = "Effectue un credit (depot) sur un compte bancaire et retourne le compte mis a jour")
    public BankAccountDto creditAccount(
            @ToolParam(description = "Identifiant (UUID) du compte a crediter") String accountId,
            @ToolParam(description = "Montant a crediter, strictement positif") double amount,
            @ToolParam(description = "Description de l'operation") String description) {
        BankAccount account = bankAccountService.credit(accountId, amount, description);
        return bankAccountService.getAccountDto(account.getId());
    }

    @Tool(description = "Effectue un virement entre deux comptes bancaires. Echoue si le solde du compte source est insuffisant")
    public String transfer(
            @ToolParam(description = "Identifiant (UUID) du compte source") String accountSource,
            @ToolParam(description = "Identifiant (UUID) du compte destinataire") String accountDestination,
            @ToolParam(description = "Montant du virement, strictement positif") double amount) {
        bankAccountService.transfer(accountSource, accountDestination, amount);
        return "Virement de " + amount + " effectue du compte " + accountSource
                + " vers le compte " + accountDestination;
    }

    private void enrichWithCustomers(List<BankAccountDto> accounts) {
        Map<Long, Customer> cache = new HashMap<>();
        for (BankAccountDto account : accounts) {
            Customer customer = cache.computeIfAbsent(account.getCustomerId(),
                    customerRestClient::findCustomerById);
            account.setCustomer(customer);
        }
    }
}
