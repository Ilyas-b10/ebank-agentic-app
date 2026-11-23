package com.ebank.account;

import com.ebank.account.entities.AccountOperation;
import com.ebank.account.entities.BankAccount;
import com.ebank.account.enums.AccountType;
import com.ebank.account.enums.OperationType;
import com.ebank.account.repositories.AccountOperationRepository;
import com.ebank.account.repositories.BankAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.UUID;

@SpringBootApplication
@EnableFeignClients
public class EbankServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbankServiceApplication.class, args);
    }

    /**
     * Donnees de test : les customerId 1 a 4 correspondent aux clients inseres
     * par le CommandLineRunner de customer-service (IDs auto-incrementes 1..4).
     */
    @Bean
    CommandLineRunner start(BankAccountRepository bankAccountRepository,
                            AccountOperationRepository accountOperationRepository) {
        return args -> {
            for (long customerId = 1; customerId <= 4; customerId++) {
                createAccountWithOperations(bankAccountRepository, accountOperationRepository,
                        customerId, AccountType.CURRENT_ACCOUNT, 10000 * customerId);
                createAccountWithOperations(bankAccountRepository, accountOperationRepository,
                        customerId, AccountType.SAVING_ACCOUNT, 20000 * customerId);
            }
            bankAccountRepository.findAll().forEach(a ->
                    System.out.println("Account inserted : " + a.getId() + " - customerId=" + a.getCustomerId()
                            + " - " + a.getType() + " - balance=" + a.getBalance()));
        };
    }

    private void createAccountWithOperations(BankAccountRepository bankAccountRepository,
                                             AccountOperationRepository accountOperationRepository,
                                             Long customerId, AccountType type, double initialBalance) {
        BankAccount account = new BankAccount();
        account.setId(UUID.randomUUID().toString());
        account.setCreatedAt(new Date());
        account.setCurrency("MAD");
        account.setType(type);
        account.setCustomerId(customerId);
        account.setBalance(initialBalance);
        bankAccountRepository.save(account);

        double balance = initialBalance;
        for (int i = 1; i <= 3; i++) {
            double creditAmount = 1000 * i;
            balance += creditAmount;
            accountOperationRepository.save(new AccountOperation(null, new Date(), creditAmount,
                    OperationType.CREDIT, "Initial credit #" + i, account));

            double debitAmount = 300 * i;
            balance -= debitAmount;
            accountOperationRepository.save(new AccountOperation(null, new Date(), debitAmount,
                    OperationType.DEBIT, "Initial debit #" + i, account));
        }
        account.setBalance(balance);
        bankAccountRepository.save(account);
    }
}
