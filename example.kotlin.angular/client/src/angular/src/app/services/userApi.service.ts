import {Injectable} from '@angular/core';
import {Observable, Subject, throwError} from 'rxjs';
import {filter} from 'rxjs/operators';

import * as $kotlin from 'kotlin';

import * as info_js from 'example.kotlin.angular-information';
import info = info_js.net.akehurst.kotlin.example.addressbook.information;

import * as ui_js from 'example.kotlin.angular-user-api';
import UI = ui_js.net.akehurst.kotlin.example.addressbook.user.api;

import * as gui2core_js from 'example.kotlin.angular-gui2core';
import gui2core = gui2core_js.net.akehurst.kotlin.example.addressbook.gui2core;

import * as wsc_js from 'example.kotlin.angular-websocketClient'
import wsc = wsc_js.net.akehurst.kotlin.example.addressbook.websocket.client.ktor;

@Injectable({
  providedIn: 'root',
})
export class UserApiService {

  private websocket: wsc.WebsocketClientKtor<string>;
  private gui2Core = new gui2core.Gui2Core();

  session: info.UserSession;
  userRequest = new UserRequestService(this.gui2Core);
  userNotification = new UserNotificationService();

  private waitForWebsocket(ws, resolve): void {
    if (ws.websocket) {
      resolve(true);
    } else {
      setTimeout(this.waitForWebsocket, 500, ws, resolve);
    }
  }

  init(resolve) {
    // create
    this.websocket = new wsc.WebsocketClientKtor<string>();
    this.websocket.endPointId = "";
    this.websocket.host = window.location.hostname;
    this.websocket.port = parseInt(window.location.port, 10);

    // connect
    this.gui2Core.outgoingMessage = this.websocket.incomingMessage;
    this.websocket.incomingMessage = this.gui2Core.outgoingMessage;

    // activate
    this.gui2Core.start();
    this.websocket.start();

    // wait until websocket is established
    this.waitForWebsocket(this.websocket, resolve);
  }
}

class UserRequestService implements UI.UserRequest {

  constructor(
    private gui2Core: gui2core.Gui2Core
  ) {

  }

  requestCreateAddressBook(session: info.UserSession, title: string): void {
    this.gui2Core.requestCreateAddressBook(session, title);
  }

  requestReadAllAddressBookTitles(session: info.UserSession): void {
    this.gui2Core.requestReadAllAddressBookTitles(session);
  }

  requestUpdateAddressBook(session: info.UserSession, oldTitle: string, newTitle: string): void {
    this.gui2Core.requestUpdateAddressBook(session, oldTitle, newTitle);
  }

  requestDeleteAddressBook(session: info.UserSession, title: string): void {
    this.gui2Core.requestDeleteAddressBook(session, title);
  }

  requestCreateContact(session: info.UserSession, addressBookTitle: string, alias: string): void {
    this.gui2Core.requestCreateContact(session, addressBookTitle, alias);
  }

  requestReadAllContact(session: info.UserSession, addressBookTitle: string): void {
    this.gui2Core.requestReadAllContact(session, addressBookTitle);
  }

  requestReadContact(session: info.UserSession, addressBookTitle: string, alias: string): void {
    this.gui2Core.requestReadContact(session, addressBookTitle, alias);
  }

  requestUpdateContact(session: info.UserSession, addressBookTitle: string, oldAlias: string, contact: info.Contact): void {
    this.gui2Core.requestUpdateContact(session, addressBookTitle, oldAlias, contact);
  }

  requestDeleteContact(session: info.UserSession, addressBookTitle: string, alias: string): void {
    this.gui2Core.requestDeleteContact(session, addressBookTitle, alias);
  }
}

class UserNotificationService implements UI.UserNotification {

  notifyCreatedAddressBookSubject = new Subject<string>();
  notifyReadAllAddressBookSubject = new Subject<$kotlin.kotlin.collections.List<string>>();
  notifyReadAddressBookSubject = new Subject<info.AddressBook>();
  notifyUpdatedAddressBookSubject = new Subject<{ oldTitle: string, newTitle: string }>();
  notifyDeletedAddressBookSubject = new Subject<string>();

  notifyCreatedContactSubject = new Subject<string>();
  notifyReadAllContactSubject = new Subject<$kotlin.kotlin.collections.List<string>>();
  notifyReadContactSubject = new Subject<info.Contact>();
  notifyUpdatedContactSubject = new Subject<info.Contact>();
  notifyDeletedContactSubject = new Subject<string>();

  notifyCreatedAddressBook(session: info.UserSession, title: string): void {
    this.notifyCreatedAddressBookSubject.next(title);
  }

  notifyReadAllAddressBookTitles(session: info.UserSession, all: $kotlin.kotlin.collections.List<string>): void {
    this.notifyReadAllAddressBookSubject.next(all);
  }

  notifyReadAddressBook(session: info.UserSession, addressBook: info.AddressBook): void {
    this.notifyReadAddressBookSubject.next(addressBook);
  }

  notifyUpdatedAddressBook(session: info.UserSession, oldTitle: string, newTitle: string): void {
    this.notifyUpdatedAddressBookSubject.next({oldTitle, newTitle});
  }

  notifyDeletedAddressBook(session: info.UserSession, title: string): void {
    this.notifyDeletedAddressBookSubject.next(title);
  }

  notifyCreatedContact(session: info.UserSession, alias: string): void {
    this.notifyCreatedContactSubject.next(alias);
  }

  notifyReadAllContact(session: info.UserSession, all: $kotlin.kotlin.collections.List<string>): void {
    this.notifyReadAllContactSubject.next(all);
  }

  notifyReadContact(session: info.UserSession, contact: info.Contact): void {
    this.notifyReadContactSubject.next(contact);
  }

  notifyUpdatedContact(session: info.UserSession, oldAlias: string, updatedContact: info.Contact): void {
    this.notifyUpdatedContactSubject.next(updatedContact);
  }

  notifyDeletedContact(session: info.UserSession, alias: string): void {
    this.notifyDeletedContactSubject.next(alias);
  }
}
