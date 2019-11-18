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
