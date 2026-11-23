package com.ebank.account.web;

import com.ebank.account.clients.CustomerRestClient;
import com.ebank.account.dto.BankAccountRequest;
import com.ebank.account.dto.CreditRequest;
import com.ebank.account.dto.DebitRequest;
import com.ebank.account.dto.TransferRequest;
import com.ebank.account.entities.BankAccount;
import com.ebank.account.model.Customer;
import com.ebank.account.services.BankAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountRestController {

    private final BankAccountService bankAccountService;
    private final CustomerRestClient customerRestClient;

    public BankAccountRestController(BankAccountService bankAccountService,
                                     CustomerRestClient customerRestClient) {
        this.bankAccountService = bankAccountService;
        this.customerRestClient = customerRestClient;
    }

    @GetMapping
    public List<BankAccount> getAllAccounts() {
        List<BankAccount> accounts = bankAccountService.getAllAccounts();
        Map<Long, Customer> customersCache = new HashMap<>();
        for (BankAccount account : accounts) {
            Customer customer = customersCache.computeIfAbsent(account.getCustomerId(),
                    customerRestClient::findCustomerById);
            account.setCustomer(customer);
        }
        return accounts;
    }

    @GetMapping("/{id}")
    public BankAccount getAccount(@PathVariable String id) {
        BankAccount account = bankAccountService.getAccount(id);
        account.setCustomer(customerRestClient.findCustomerById(account.getCustomerId()));
        return account;
    }

    @PostMapping
    public ResponseEntity<BankAccount> createAccount(@RequestBody BankAccountRequest request) {
        BankAccount account = bankAccountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/debit")
    public BankAccount debit(@RequestBody DebitRequest request) {
        BankAccount account = bankAccountService.debit(request.getAccountId(), request.getAmount(),
                request.getDescription());
        account.setCustomer(customerRestClient.findCustomerById(account.getCustomerId()));
        return account;
    }

    @PostMapping("/credit")
    public BankAccount credit(@RequestBody CreditRequest request) {
        BankAccount account = bankAccountService.credit(request.getAccountId(), request.getAmount(),
                request.getDescription());
        account.setCustomer(customerRestClient.findCustomerById(account.getCustomerId()));
        return account;
    }

    @PostMapping("/transfer")
    public Map<String, Object> transfer(@RequestBody TransferRequest request) {
        bankAccountService.transfer(request.getAccountSource(), request.getAccountDestination(),
                request.getAmount());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("accountSource", request.getAccountSource());
        response.put("accountDestination", request.getAccountDestination());
        response.put("amount", request.getAmount());
        return response;
    }
}
