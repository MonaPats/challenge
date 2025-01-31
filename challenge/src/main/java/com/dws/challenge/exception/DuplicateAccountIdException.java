package com.dws.challenge.exception;

public class DuplicateAccountIdException extends RuntimeException {

  public DuplicateAccountIdException(String message) {

    super(message);
  }


  //Added new
  public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
      super(message);
    }
  }

  //Added new
  public static class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
      super(message);
    }
  }
}
