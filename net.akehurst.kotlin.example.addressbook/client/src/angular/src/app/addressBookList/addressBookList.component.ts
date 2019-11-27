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

import { Component, OnInit, Input } from '@angular/core';
import {Router} from "@angular/router";

import {UserApiService} from "../services/userApi.service";

import * as info_js from 'net.akehurst.kotlin.example.addressbook-information';
import info = info_js.net.akehurst.kotlin.example.addressbook.information;

@Component({
  selector: 'app-addressBook-list',
  templateUrl: './addressBookList.component.html',
  styleUrls: ['./addressBookList.component.scss']
})
export class AddressBookListComponent implements OnInit {

  addressBookList : string[];

  constructor(
    private userApi:UserApiService,
    private router: Router
  ) { }

  ngOnInit() {
    this.userApi.userNotification.notifyCreatedAddressBookSubject.subscribe(title => {
      this.addressBookList.push(title);
    });
    this.userApi.userNotification.notifyReadAllAddressBookSubject.subscribe( titles=>{
      this.addressBookList  = titles.toArray();
    });
    this.userApi.userNotification.notifyDeletedAddressBookSubject.subscribe(title => {
      let i = this.addressBookList.indexOf(title);
      this.addressBookList.splice(i,1);
    });
    this.userApi.userRequest.requestReadAllAddressBookTitles(this.userApi.session)
  }

  createAddressBook() {
    let newTitle = 'new Address Book'; //TODO: use a dialog to get the value
    this.userApi.userRequest.requestCreateAddressBook(this.userApi.session,newTitle);
  }

  readAddressBook(title:string) {
    this.router.navigate(['/addressBook'], {queryParams: {title: title}});
  }

  deleteAddressBook(title:string) {
    this.userApi.userRequest.requestDeleteAddressBook(this.userApi.session,title);
  }


}
