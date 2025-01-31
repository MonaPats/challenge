package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountService;
import com.dws.challenge.service.AccountsServiceImpl;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountService accountsService;

  @Autowired
  private AccountsRepository accountsRepository;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsRepository.clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
            .andExpect(status().isOk())
            .andExpect(
                    content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  //Adding NEW code


  @Test
  void getAccount_AccountFound() throws Exception {
    // Arrange: Create an account for testing
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, BigDecimal.valueOf(123.45));
    this.accountsService.createAccount(account);

    // Act: Call the endpoint
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
            .andExpect(status().isOk())  // Expecting HTTP 200 OK
            .andExpect(content().json("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));  // Ensure the response body matches
  }

  @Test
  void getAccount_AccountNotFound() throws Exception {
    // Arrange: Use a non-existent account ID
    String nonExistentAccountId = "nonexistent-account";

    // Act: Call the endpoint with the non-existent account ID
    this.mockMvc.perform(get("/v1/accounts/" + nonExistentAccountId))
            .andExpect(status().isNotFound());  // Expecting HTTP 404 Not Found
  }


  private String accountFromId = "account1";
  private String accountToId = "account2";

  @BeforeEach
  void setUp() {
    // Set up accounts for testing
    Account accountFrom = new Account(accountFromId, BigDecimal.valueOf(1000));
    Account accountTo = new Account(accountToId, BigDecimal.valueOf(500));

    // Save mock accounts using accounts service (if needed for integration test)
    accountsService.createAccount(accountFrom);
    accountsService.createAccount(accountTo);
  }


  @Test
  void transferMoney_Success() throws Exception {
    // Arrange: Mock the money transfer service
    BigDecimal transferAmount = BigDecimal.valueOf(200);

    // Act: Perform transfer request
    mockMvc.perform(post("/v1/accounts/transfer")
                    .param("fromAccountId", accountFromId)
                    .param("toAccountId", accountToId)
                    .param("amount", transferAmount.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    // Verify: Check if the service method was called
    verify(accountsService).transfer(accountFromId, accountToId, transferAmount);

    // Optionally, verify account balances after the transfer
    Account fromAccount = accountsService.getAccount(accountFromId);
    Account toAccount = accountsService.getAccount(accountToId);

    assertThat(fromAccount.getBalance()).isEqualByComparingTo("800");
    assertThat(toAccount.getBalance()).isEqualByComparingTo("700");
  }

  @Test
  void transferMoney_InsufficientFunds() throws Exception {
    // Arrange: Try transferring more than available balance
    BigDecimal transferAmount = BigDecimal.valueOf(1500); // exceeds balance

    // Act: Perform transfer request
    mockMvc.perform(post("/v1/accounts/transfer")
                    .param("fromAccountId", accountFromId)
                    .param("toAccountId", accountToId)
                    .param("amount", transferAmount.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());  // Expecting a failure due to insufficient funds

    // Verify: Ensure that transfer was not attempted
    verify(accountsService, never()).transfer(anyString(), anyString(), any(BigDecimal.class));
  }

  @Test
  void transferMoney_InvalidAmount() throws Exception {
    // Arrange: Invalid negative amount for transfer
    BigDecimal transferAmount = BigDecimal.valueOf(-100);

    // Act: Perform transfer request
    mockMvc.perform(post("/v1/accounts/transfer")
                    .param("fromAccountId", accountFromId)
                    .param("toAccountId", accountToId)
                    .param("amount", transferAmount.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());  // Expecting a failure due to invalid amount

    // Verify: Ensure that transfer was not attempted
    verify(accountsService, never()).transfer(anyString(), anyString(), any(BigDecimal.class));
  }

  @Test
  void transferMoney_AccountNotFound() throws Exception {
    // Arrange: Non-existing account ID
    String nonExistentAccountId = "nonexistent-account";
    BigDecimal transferAmount = BigDecimal.valueOf(100);

    // Act: Perform transfer request
    mockMvc.perform(post("/v1/accounts/transfer")
                    .param("fromAccountId", accountFromId)
                    .param("toAccountId", nonExistentAccountId)
                    .param("amount", transferAmount.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());  // Expecting 404 due to non-existing account

    // Verify: Ensure that transfer was not attempted
    verify(accountsService, never()).transfer(anyString(), anyString(), any(BigDecimal.class));
  }

  @Test
  void transferMoney_SameAccount() throws Exception {
    // Arrange: Same account for both 'from' and 'to'
    BigDecimal transferAmount = BigDecimal.valueOf(100);

    // Act: Perform transfer request
    mockMvc.perform(post("/v1/accounts/transfer")
                    .param("fromAccountId", accountFromId)
                    .param("toAccountId", accountFromId)  // Same account
                    .param("amount", transferAmount.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());  // Expecting failure as transfer to same account is not valid

    // Verify: Ensure that transfer was not attempted
    verify(accountsService, never()).transfer(anyString(), anyString(), any(BigDecimal.class));
  }
}

