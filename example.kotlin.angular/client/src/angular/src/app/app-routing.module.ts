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
