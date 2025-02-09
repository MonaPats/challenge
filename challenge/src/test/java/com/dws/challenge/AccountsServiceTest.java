package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsServiceImpl;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsServiceImpl accountsService;

  @Mock
  private AccountsRepository accountsRepository;


  // Newly Added

  @Autowired
  private NotificationService notificationService;


  private String accountFromId = "Id-123";
  private String accountToId = "Id-456";

  @BeforeEach
  void setUp() throws DuplicateAccountIdException {
    // Reset repository before each test.
    accountsService.getAccountsRepository().clearAccounts();

    // Create two initial accounts
    Account accountFrom = new Account(accountFromId);
    accountFrom.setBalance(new BigDecimal(1000));
    accountsService.createAccount(accountFrom);

    Account accountTo = new Account(accountToId);
    accountTo.setBalance(new BigDecimal(500));
    accountsService.createAccount(accountTo);
  }

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

//Newly updated Code

  @Test
  void transferMoney_Successful() {
    // Arrange
    BigDecimal transferAmount = new BigDecimal(200);

    // Act
    accountsService.transfer(accountFromId, accountToId, transferAmount);

    // Assert
    Account accountFrom = accountsService.getAccount(accountFromId);
    Account accountTo = accountsService.getAccount(accountToId);

    assertThat(accountFrom.getBalance()).isEqualByComparingTo("800");
    assertThat(accountTo.getBalance()).isEqualByComparingTo("700");
  }

  @Test
  void transferMoney_InsufficientBalance() {
    // Arrange
    BigDecimal transferAmount = new BigDecimal(2000);  // More than available in accountFrom

    // Act & Assert
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      accountsService.transfer(accountFromId, accountToId, transferAmount);
    });

    assertThat(exception.getMessage()).isEqualTo("Insufficient balance");
  }

  @Test
  void transferMoney_AccountFromNotFound() {
    // Arrange
    String nonExistentAccountId = "nonexistent-account";
    BigDecimal transferAmount = new BigDecimal(100);

    // Act & Assert
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      accountsService.transfer(nonExistentAccountId, accountToId, transferAmount);
    });

    assertThat(exception.getMessage()).isEqualTo("Account not found: " + nonExistentAccountId);
  }

  @Test
  void transferMoney_AccountToNotFound() {
    // Arrange
    String nonExistentAccountId = "nonexistent-account";
    BigDecimal transferAmount = new BigDecimal(100);

    // Act & Assert
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      accountsService.transfer(accountFromId, nonExistentAccountId, transferAmount);
    });

    assertThat(exception.getMessage()).isEqualTo("Account not found: " + nonExistentAccountId);
  }

  @Test
  void transferMoney_NegativeAmount() {
    // Arrange
    BigDecimal transferAmount = new BigDecimal(-100);  // Invalid negative amount

    // Act & Assert
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      accountsService.transfer(accountFromId, accountToId, transferAmount);
    });

    assertThat(exception.getMessage()).isEqualTo("Amount must be positive");
  }



@Test
void testTransferMoney() {
  Account account1 = new Account("Id-123", new BigDecimal("1000"));
  Account account2 = new Account("Id-124", new BigDecimal("500"));
  accountsService.createAccount(account1);
  accountsService.createAccount(account2);

  // Mock NotificationService
  doNothing().when(notificationService).sendNotification(anyString(), anyString());

  // Perform transfer
  accountsService.transfer("Id-123", "Id-124", new BigDecimal("100"));

  // Verify the balances after the transfer
  Account accountFrom = accountsService.getAccount("Id-123");
  Account accountTo = accountsService.getAccount("Id-124");

  assertThat(accountFrom.getBalance()).isEqualByComparingTo("900");
  assertThat(accountTo.getBalance()).isEqualByComparingTo("600");

  // Verify that notifications were sent
  verify(notificationService, times(2)).sendNotification(anyString(), anyString());
}
}

