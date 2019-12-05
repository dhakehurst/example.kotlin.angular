/**
 * Copyright (C) 2019 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Injectable} from '@angular/core';
import { Subject} from 'rxjs';

import * as $kotlin from 'kotlin';

import * as info_js from 'net.akehurst.kotlin.example.addressbook-information';
import info = info_js.net.akehurst.kotlin.example.addressbook.information;

import * as ui_js from 'net.akehurst.kotlin.example.addressbook-user-api';
import UI = ui_js.net.akehurst.kotlin.example.addressbook.user.api;

import * as gui2core_js from 'net.akehurst.kotlin.example.addressbook-gui2core';
import gui2core = gui2core_js.net.akehurst.kotlin.example.addressbook.gui2core;

import * as wsc_js from 'net.akehurst.kotlin.example.addressbook-websocketClient'
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

  constructor(
  ) {
  }

  private waitForWebsocket(ws:wsc.WebsocketClientKtor<string>, resolve): void {
    if (ws.connected) {
      resolve(true);
    } else {
      setTimeout(this.waitForWebsocket, 500, ws, resolve);
    }
  }

  init(resolve) {
    //TODO: how to find out the session_id?
    this.session  = new info.UserSession("unknown");
    // create
    this.websocket = new wsc.WebsocketClientKtor<string>("unknown");
    let host = window.location.hostname;
    let port = parseInt(window.location.port, 10);
    let path = "ws";

    // connect
    this.gui2Core.outgoingMessage = this.websocket.incomingMessage;
    this.websocket.outgoingMessage = this.gui2Core.incomingMessage;
    this.gui2Core.userNotification = this.userNotification;

    // activate
    this.gui2Core.start();
    this.websocket.start(host, port, path);

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
  notifyUpdatedContactSubject = new Subject<{oldAlias:string, updatedContact:info.Contact}>();
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
    this.notifyUpdatedContactSubject.next({oldAlias, updatedContact});
  }

  notifyDeletedContact(session: info.UserSession, alias: string): void {
    this.notifyDeletedContactSubject.next(alias);
  }
}
