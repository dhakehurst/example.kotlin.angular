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

import { BrowserModule } from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule} from '@angular/core';
import {FormsModule} from "@angular/forms";

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {AddressBookListComponent} from "./addressBookList/addressBookList.component";
import {AddressBookViewComponent} from "./addressBookView/addressBookView.component";
import {ContactViewComponent} from "./contactView/contactView.component";
import {UserApiService} from "./services/userApi.service";

@NgModule({
  declarations: [
    AppComponent,
    AddressBookListComponent,
    AddressBookViewComponent,
    ContactViewComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule
  ],
  providers: [
    UserApiService,
    {
      provide: APP_INITIALIZER,
      useFactory: initApplication,
      deps: [UserApiService],
      multi: true
    },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

export function initApplication(
  userApi: UserApiService
): () => Promise<any> {
  return () => new Promise(resolve => {
    userApi.init(resolve);
  });
}
