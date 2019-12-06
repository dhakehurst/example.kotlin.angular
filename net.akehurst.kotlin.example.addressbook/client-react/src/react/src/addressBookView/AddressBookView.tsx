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

import React from 'react';
import {Link, navigate} from "@reach/router";
import queryString, {ParsedQuery} from 'query-string';
import './AddressBookView.scss';
import {AppProps} from "../App";
import userApi from '../services/userApi.service'

import info_js from 'net.akehurst.kotlin.example.addressbook-information';
import info = info_js.net.akehurst.kotlin.example.addressbook.information;


class AddressBookViewState {

    constructor(
        public query: ParsedQuery<string>
    ) {
    }

    addressBookList: string[] = [];
    selectedAddressBookTitle: string;

    contactList: string[] = [];
    selectedContactAlias: string; //keep this also, so that the value can be used for updates
    selectedContact: info.Contact;

}


export default class AddressBookView extends React.Component<AppProps, AddressBookViewState> {

    constructor(props: AppProps) {
        super(props);

        const query = queryString.parse(this.props.location.search);
        this.state = new AddressBookViewState(query);
        this.register();
        this.update(query);
        userApi.userRequest.requestReadAllAddressBookTitles(userApi.session);

    }

    private register() {
        userApi.userNotification.notifyCreatedAddressBookSubject.subscribe(title => {
            this.setState(state => ({
                addressBookList: state.addressBookList.concat(title)
            }));
        });
        userApi.userNotification.notifyReadAllAddressBookSubject.subscribe(titles => {
            this.setState(state => ({
                addressBookList: titles.toArray()
            }));
        });
        userApi.userNotification.notifyUpdatedAddressBookSubject.subscribe(args => {
            let i = this.state.addressBookList.indexOf(args.oldTitle);
            let abl = this.state.addressBookList.map(it => it);
            abl.splice(i, 1, args.newTitle);
            this.setState(state => ({
                addressBookList: abl,
                selectedAddressBookTitle: args.newTitle
            }));
        });
        userApi.userNotification.notifyDeletedAddressBookSubject.subscribe(title => {
            let i = this.state.addressBookList.indexOf(title);
            let abl = this.state.addressBookList.map(it => it);
            abl.splice(i, 1);
            this.setState(state => ({}));
        });

        userApi.userNotification.notifyCreatedContactSubject.subscribe(alias => {
            this.setState(state => ({
                contactList: state.contactList.concat(alias)
            }));
        });
        userApi.userNotification.notifyReadAllContactSubject.subscribe(all => {
            this.setState(state => ({
                contactList: all.toArray()
            }));
        });
        userApi.userNotification.notifyReadContactSubject.subscribe(contact => {
            this.setState(state => ({
                selectedContactAlias: contact.alias,
                selectedContact: contact
            }));
        });
        userApi.userNotification.notifyUpdatedContactSubject.subscribe(args => {
            let i = this.state.contactList.indexOf(args.oldAlias);
            let cl = this.state.contactList.map(it => it);
            cl.splice(i, 1, args.updatedContact.alias);
            this.setState(state => ({
                contactList: cl,
                selectedContactAlias: args.updatedContact.alias
            }));
        });
        userApi.userNotification.notifyDeletedContactSubject.subscribe(alias => {
            let i = this.state.contactList.indexOf(alias);
            let cl = this.state.contactList.map(it => it);
            cl.splice(i, 1);
            this.setState(state => ({
                contactList: cl
            }));
        });
    }

    render() {
        const options = this.state.addressBookList.map((ab) =>
            <option value={ab} placeholder={'please select'}>{ab}</option>
        );
        const rows = this.state.contactList.map((ct) => {
            const q = {title: this.state.query.title, contact: ct};
            return (
                <tr>
                    <td>
                        <button className="remove" onClick={this.onRequestDeleteContact.bind(this, ct)}><i
                            className="pi pi-minus"></i>
                        </button>
                    </td>
                    <td>
                        <Link to={'?' + queryString.stringify(q)}>{ct}</Link>
                    </td>
                </tr>
            );
        });
        const contactView = (() => {
            if (this.state.selectedContact) {
                const contact = this.state.selectedContact;
                const numbersView = contact.phoneNumbers.values.toArray().map(pn =>
                    <tr>
                        <td>
                            <button className="remove" onClick={this.onRequestDeletePhoneNumber.bind(this, pn)}><i
                                className="pi pi-minus"></i></button>
                        </td>
                        <td>{pn.label}</td>
                        <td>{pn.number}</td>
                    </tr>
                );
                return (
                    <section className="section2">
                        <label>alias: <input value={contact.alias} placeholder="alias"
                                             onChange={this.onRequestUpdateContact.bind(this)}/> </label>
                        <label>first name: <input value={contact.firstName} placeholder="firstName"/> </label>
                        <label>last name: <input value={contact.lastName} placeholder="lastName"/> </label>
                        <label>phone numbers:
                            <table>
                                <thead>
                                <tr>
                                    <th>
                                        <button className="add" onClick={this.onRequestCreatePhoneNumber.bind(this)}><i
                                            className="pi pi-plus"></i></button>
                                    </th>
                                    <th>Label</th>
                                    <th>Number</th>
                                </tr>
                                </thead>
                                <tbody>
                                {numbersView}
                                </tbody>
                            </table>
                        </label>
                    </section>
                );
            } else {
                return (
                    <section className="section2">
                    </section>
                );
            }
        })();
        return (
            <article>
                <header>
                    <h2>
                        <select onChange={this.onReadAddressBook.bind(this)} value={this.state.selectedAddressBookTitle}>
                            <option disabled selected> -- select an option --</option>
                            {options}
                        </select>
                    </h2>
                </header>
                <section className="section1">
                    <label>title:
                        <input onChange={this.onUpdateAddressBookTitle.bind(this)}
                               value={this.state.selectedAddressBookTitle}
                               placeholder="title"/>
                    </label>
                    <table>
                        <thead>
                        <tr>
                            <th>
                                <button className="add" onClick={this.onRequestCreateContact.bind(this)}><i
                                    className="pi pi-plus"></i></button>
                            </th>
                            <th>Contact</th>
                        </tr>
                        </thead>
                        <tbody>
                        {rows}
                        </tbody>
                    </table>
                </section>
                <section className="section2">
                    {contactView}
                </section>
            </article>
        );
    }

    componentDidUpdate(prevProps: AppProps) {
        // because the Links to contacts change the query string, we need to test if it has changed and update accordingly
        if (this.props.location.search !== prevProps.location.search) {
            const query = queryString.parse(this.props.location.search);
            this.setState({
                query: query
            });
            this.update(query);
        }
    }

    private update(query) {
        if (query.title) {
            this._selectAddressBook(query.title as string)
        }
        if (query.contact) {
            const alias = query.contact as string;
            userApi.userRequest.requestReadContact(userApi.session, query.title as string, alias);
        }
    }

    private clear() {
        this.setState({
            selectedAddressBookTitle: null,
            contactList: [],
            selectedContactAlias: null,
            selectedContact: null
        });
    }

    private _selectAddressBook(title: string) {
        this.setState({
            selectedAddressBookTitle: title
        });

        userApi.userRequest.requestReadAllContact(userApi.session, title);
    }

    onUpdateAddressBookTitle($event) {
        let newTitle = $event.target.value;
        let oldTitle = this.state.selectedAddressBookTitle;
        userApi.userRequest.requestUpdateAddressBook(userApi.session, oldTitle, newTitle);
    }

    onReadAddressBook(e) {
        this.clear();
        let title = e.target.value;
        this.props.navigate('?title=' + title);
    }

    onRequestCreateContact(e) {
        let newAlias = 'newContact'; //TODO: use a dialog to get the value
        userApi.userRequest.requestCreateContact(userApi.session, this.state.selectedAddressBookTitle, newAlias);
    }

    readContact(alias: string) {
        userApi.userRequest.requestReadContact(userApi.session, this.state.selectedAddressBookTitle, alias);
    }

    onRequestUpdateContact() {
        userApi.userRequest.requestUpdateContact(userApi.session, this.state.selectedAddressBookTitle, this.state.selectedContactAlias, this.state.selectedContact);
    }

    onRequestDeleteContact(alias: string) {
        userApi.userRequest.requestDeleteContact(userApi.session, this.state.selectedAddressBookTitle, alias);
    }

    onRequestCreatePhoneNumber() {
    }

    onRequestDeletePhoneNumber(phoneNumber: info.PhoneNumber) {
    }
}
