import { Component, OnInit, Input } from '@angular/core';

import {UserApiService} from "../services/userApi.service";


import * as info_js from 'example.kotlin.angular-information';
import info = info_js.net.akehurst.kotlin.example.addressbook.information;

@Component({
  selector: 'app-addressBook-view',
  templateUrl: './addressBookView.component.html',
  styleUrls: ['./addressBookView.component.scss']
})
export class AddressBookViewComponent implements OnInit {

  addressBookList : string[] = [];
  selectedAddressBookTitle : string;

  contactList : string[] = [];
  selectedContactAlias: string;
  selectedContact : info.Contact;

  constructor(
    private userApi:UserApiService
  ) { }

  ngOnInit() {
    this.userApi.userNotification.notifyCreatedAddressBookSubject.subscribe(title => {
      this.addressBookList.push(title);
    });
    this.userApi.userNotification.notifyReadAllAddressBookSubject.subscribe( titles=>{
      this.addressBookList  = titles.toTypedArray();
    });
    this.userApi.userNotification.notifyUpdatedAddressBookSubject.subscribe(args => {
      let i = this.addressBookList.indexOf(args.oldTitle);
      this.addressBookList.splice(i,1, args.newTitle);
    });
    this.userApi.userNotification.notifyDeletedAddressBookSubject.subscribe(title => {
      let i = this.addressBookList.indexOf(title);
      this.addressBookList.splice(i,1);
    });
    this.userApi.userRequest.requestReadAllAddressBookTitles(this.userApi.session);

    this.userApi.userNotification.notifyCreatedContactSubject.subscribe(alias =>{
      this.contactList.push(alias);
    });
    this.userApi.userNotification.notifyReadAllContactSubject.subscribe(all =>{
      this.contactList = all.toTypedArray();
    });
  }

  requestCreateAddressBook() {
    let newTitle = 'new Address Book'; //TODO: use a dialog to get the value
    this.userApi.userRequest.requestCreateAddressBook(this.userApi.session,newTitle);
  }

  requestDeleteAddressBook() {
    this.userApi.userRequest.requestDeleteAddressBook(this.userApi.session,this.selectedAddressBookTitle);
  }

  selectAddressBook($event) {
    let title = $event.value;
    this.selectedAddressBookTitle = title;
  }

  requestCreateContact() {
    let newAlias = 'newContact'; //TODO: use a dialog to get the value
    this.userApi.userRequest.requestCreateContact(this.userApi.session,this.selectedAddressBookTitle, newAlias);
  }

  readContact(alias:string) {
    this.userApi.userRequest.requestReadContact(this.userApi.session,this.selectedAddressBookTitle, alias);
  }

  requestUpdateContact() {
    this.userApi.userRequest.requestUpdateContact(this.userApi.session,this.selectedAddressBookTitle, this.selectedContactAlias, this.selectedContact);
  }

  requestDeleteContact(alias:string) {
    this.userApi.userRequest.requestDeleteContact(this.userApi.session, this.selectedAddressBookTitle, alias);
  }


}
