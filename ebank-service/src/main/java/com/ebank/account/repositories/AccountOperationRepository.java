package com.ebank.account.repositories;

import com.ebank.account.entities.AccountOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {

    List<AccountOperation> findByBankAccountIdOrderByIdDesc(String bankAccountId);
}
