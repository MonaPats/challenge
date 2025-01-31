package com.dws.challenge.service;

import com.dws.challenge.domain.Account;


public interface NotificationService {

  void notifyAboutTransfer(Account account, String transferDescription);

  void sendNotification(String accountFromId, String s);
}
