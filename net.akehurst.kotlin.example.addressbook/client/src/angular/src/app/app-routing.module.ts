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

import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {AddressBookListComponent} from "./addressBookList/addressBookList.component";
import {AddressBookViewComponent} from "./addressBookView/addressBookView.component";


const routes: Routes = [
  {path: '', component: AddressBookListComponent, data: {title: 'Address Books'} },
  {path: 'list', component: AddressBookListComponent, data: {title: 'Address Books'} },
  {path: 'addressBook', component: AddressBookViewComponent, data: {title: 'Address Books'} }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
