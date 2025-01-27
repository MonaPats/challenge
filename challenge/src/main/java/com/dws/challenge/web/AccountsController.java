package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);
//added log
    try {
    this.accountsService.createAccount(account);
      log.info("Account created successfully with id {}", account.getAccountId());
      //this
      return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (DuplicateAccountIdException daie) {
      log.error("Duplicate account id attempt: {}", account.getAccountId(), daie);

      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {

    log.info("Retrieving account for id {}", accountId);
    //added below
    Account account = accountsService.getAccount(accountId);

    if (account == null) {
      log.warn("Account not found for id {}", accountId);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // HTTP 404 - Not Found
    }
    return new ResponseEntity<>(account, HttpStatus.OK);  // HTTP 200 - OK
    return this.accountsService.getAccount(accountId);
  }

}

//new
// Transfer money between two accounts
@PostMapping("/transfer")
public void transferMoney(@RequestParam String fromAccountId,
                          @RequestParam String toAccountId,
                          @RequestParam BigDecimal amount) {
  moneyTransferService.transfer(fromAccountId, toAccountId, amount);
}
