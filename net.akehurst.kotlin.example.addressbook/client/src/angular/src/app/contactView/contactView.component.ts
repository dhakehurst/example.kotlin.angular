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
