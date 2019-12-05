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
import './AddressBookList.scss';

import userApi from '../services/userApi.service'

class AddressBookListState {

    addressBookList : string[] = [];

}

export default class AddressBookList extends React.Component<{}, AddressBookListState> {

    constructor(props: Readonly<{}>) {
        super(props);
        this.state = new AddressBookListState();
    }

    render() {
        const elements = this.state.addressBookList.map((ab) =>
            <tr key={ab.toString()}>
                <td>
                    <button className="remove" onClick={this.onRemove}>
                        <i className="pi pi-minus"/>
                    </button>
                </td>
                <td className="click">{ab}</td>
            </tr>
        );
        return (
            <section>
                <header>
                    <h1>Address Books</h1>
                </header>
                <article>
                    <table>
                        <thead>
                        <tr>
                            <th>
                                <button className="add" onClick={this.onAdd}>
                                    <i className="pi pi-plus"/>
                                </button>
                            </th>
                            <th>Address Book</th>
                        </tr>
                        </thead>
                        <tbody>
                            {elements}
                        </tbody>
                    </table>

                </article>
            </section>
        );
    }

    componentDidMount() {
        userApi.userNotification.notifyCreatedAddressBookSubject.subscribe(title => {
            this.setState(state => {
               return { addressBookList: state.addressBookList.concat(title) };
            });
        });
        userApi.userNotification.notifyReadAllAddressBookSubject.subscribe( titles=>{
            this.setState(state => {
               return { addressBookList: titles.toArray() };
            });
        });
        userApi.userNotification.notifyDeletedAddressBookSubject.subscribe(title => {
           // let i = this.addressBookList.indexOf(title);
           // this.addressBookList.splice(i,1);
        });
        userApi.userRequest.requestReadAllAddressBookTitles(userApi.session)
    }

    onAdd(): void {
    }

    onRemove(): void {
    }

};