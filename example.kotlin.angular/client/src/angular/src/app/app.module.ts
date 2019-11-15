import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {FormsModule} from "@angular/forms";

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {ContactViewComponent} from "./contactView/contactView.component";
import {AddressBookViewComponent} from "./addressBookView/addressBookView.component";

@NgModule({
  declarations: [
    AppComponent,
    AddressBookViewComponent,
    ContactViewComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
