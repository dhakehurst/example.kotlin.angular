import { Component, OnInit, Input } from '@angular/core';

import * as info_js from 'example.kotlin.angular-information';
import info = info_js.net.akehurst.kotlin.example.addressbook.information;

@Component({
  selector: 'app-contact-view',
  templateUrl: './contactView.component.html',
  styleUrls: ['./contactView.component.scss']
})
export class ContactViewComponent implements OnInit {

  @Input() contact : info.Contact;

  constructor() { }

  ngOnInit() {
  }

}
