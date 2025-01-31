package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;


public interface AccountService {

    void createAccount(Account account) throws DuplicateAccountIdException;

    Account getAccount(String accountId);

   void transfer(String accountFromId, String accountToId, BigDecimal amount);

}
