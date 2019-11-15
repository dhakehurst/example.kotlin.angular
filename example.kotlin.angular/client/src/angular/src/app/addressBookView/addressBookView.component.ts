import { Component, OnInit, Input } from '@angular/core';

import * as info_js from 'example.kotlin.angular-information';
import info = info_js.net.akehurst.kotlin.example.addressbook.information;

@Component({
  selector: 'app-addressBookView-view',
  templateUrl: './addressBookView.component.html',
  styleUrls: ['./addressBookView.component.scss']
})
export class AddressBookViewComponent implements OnInit {

  addressBook : info.AddressBook;

  constructor() { }

  ngOnInit() {
  }

}
