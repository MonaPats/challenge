package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;

@Service
public class AccountsServiceImpl implements AccountService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  public AccountsServiceImpl(AccountsRepository accountsRepository, NotificationService notificationService) {

      this.accountsRepository = accountsRepository;
      this.notificationService = notificationService;

  }

    //***Added NEW code


  @Override
    // Transfer money between accounts
 public void transfer(String accountFromId, String accountToId, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }

    // Synchronize on the account IDs to avoid race conditions
    synchronized (this) {
      Account accountFrom = accountsRepository.getAccount(accountFromId);
      if (accountFrom == null) {
      //  throw new AccountNotFoundException("Account not found: " + accountFromId);
      }

      Account accountTo = accountsRepository.getAccount(accountToId);
      if (accountTo == null) {
       // throw new AccountNotFoundException("Account not found: " + accountToId);
      }

      // Ensure the sender has enough balance
      if (accountFrom.getBalance().compareTo(amount) < 0) {
        throw new DuplicateAccountIdException.InsufficientBalanceException("Insufficient balance");
      }

      // Perform the transfer
      accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
      accountTo.setBalance(accountTo.getBalance().add(amount));

      // Save updated accounts
      try {
        accountsRepository.createAccount(accountFrom);  // Update the sender account
        accountsRepository.createAccount(accountTo);    // Update the receiver account
      } catch (DuplicateAccountIdException e) {
        // In the case of an error with saving accounts, handle appropriately
        throw new RuntimeException("Error updating accounts during transfer", e);
      }

      // Send notifications to both account holders
      notificationService.sendNotification(accountFromId, "Transferred " + amount + " to account " + accountToId);
      notificationService.sendNotification(accountToId, "Received " + amount + " from account " + accountFromId);
    }
  }


  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
   accountsRepository.createAccount(account);
  }

  @Override
  public Account getAccount(String accountId) {
      return accountsRepository.getAccount(accountId);

  }


}
