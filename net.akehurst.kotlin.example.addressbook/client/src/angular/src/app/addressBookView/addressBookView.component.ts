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

import {Component, OnInit, Input} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";

import {UserApiService} from "../services/userApi.service";

import * as info_js from 'net.akehurst.kotlin.example.addressbook-information';
import info = info_js.net.akehurst.kotlin.example.addressbook.information;

@Component({
  selector: 'app-addressBook-view',
  templateUrl: './addressBookView.component.html',
  styleUrls: ['./addressBookView.component.scss']
})
export class AddressBookViewComponent implements OnInit {

  addressBookList: string[] = [];
  selectedAddressBookTitle: string;

  contactList: string[] = [];
  selectedContactAlias: string; //keep this also, so that the value can be used for updates
  selectedContact: info.Contact;

  constructor(
    private userApi: UserApiService,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  ngOnInit() {
    this.route.queryParamMap.subscribe(params => {
      if (params.has('title')) {
        let title = params.get('title');
        this._selectAddressBook(title);
      } else {
        //do nothing
      }
    });

    this.userApi.userNotification.notifyCreatedAddressBookSubject.subscribe(title => {
      this.addressBookList.push(title);
    });
    this.userApi.userNotification.notifyReadAllAddressBookSubject.subscribe(titles => {
      this.addressBookList = titles.toArray();
    });
    this.userApi.userNotification.notifyUpdatedAddressBookSubject.subscribe(args => {
      let i = this.addressBookList.indexOf(args.oldTitle);
      this.addressBookList.splice(i, 1, args.newTitle);
      this.selectedAddressBookTitle = args.newTitle;
    });
    this.userApi.userNotification.notifyDeletedAddressBookSubject.subscribe(title => {
      let i = this.addressBookList.indexOf(title);
      this.addressBookList.splice(i, 1);
    });
    this.userApi.userRequest.requestReadAllAddressBookTitles(this.userApi.session);

    this.userApi.userNotification.notifyCreatedContactSubject.subscribe(alias => {
      this.contactList.push(alias);
    });
    this.userApi.userNotification.notifyReadAllContactSubject.subscribe(all => {
      this.contactList = all.toArray();
    });
    this.userApi.userNotification.notifyReadContactSubject.subscribe(contact => {
      this.selectedContactAlias = contact.alias;
      this.selectedContact = contact;
    });
    this.userApi.userNotification.notifyUpdatedContactSubject.subscribe(args => {
      let i = this.contactList.indexOf(args.oldAlias);
      this.contactList.splice(i, 1, args.updatedContact.alias);
      this.selectedContactAlias = args.updatedContact.alias;
    });
    this.userApi.userNotification.notifyDeletedContactSubject.subscribe(alias => {
      let i = this.contactList.indexOf(alias);
      this.contactList.splice(i, 1);
    });
  }

  clear() {
    this.selectedAddressBookTitle = null;
    this.contactList = [];
    this.selectedContactAlias = null;
    this.selectedContact = null;
  }

  _selectAddressBook(title: string) {
    this.selectedAddressBookTitle = title;
    this.userApi.userRequest.requestReadAllContact(this.userApi.session, this.selectedAddressBookTitle);
  }

  updateAddressBookTitle($event) {
    let newTitle = $event.target.value;
    let oldTitle = this.selectedAddressBookTitle;
    this.userApi.userRequest.requestUpdateAddressBook(this.userApi.session, oldTitle, newTitle);
  }

  readAddressBook($event) {
    this.clear();
    let title = $event.target.value;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {title: title},
      queryParamsHandling: 'merge'
    });
  }

  requestCreateContact() {
    let newAlias = 'newContact'; //TODO: use a dialog to get the value
    this.userApi.userRequest.requestCreateContact(this.userApi.session, this.selectedAddressBookTitle, newAlias);
  }

  readContact(alias: string) {
    this.userApi.userRequest.requestReadContact(this.userApi.session, this.selectedAddressBookTitle, alias);
  }

  requestUpdateContact() {
    this.userApi.userRequest.requestUpdateContact(this.userApi.session, this.selectedAddressBookTitle, this.selectedContactAlias, this.selectedContact);
  }

  requestDeleteContact(alias: string) {
    this.userApi.userRequest.requestDeleteContact(this.userApi.session, this.selectedAddressBookTitle, alias);
  }

  requestCreatePhoneNumber() {
  }
  requestDeletePhoneNumber(phoneNumber:info.PhoneNumber) {
  }
}
