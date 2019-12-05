import React from 'react';

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
            //this.setState({addressBookList.push(title)});
        });
        userApi.userNotification.notifyReadAllAddressBookSubject.subscribe( titles=>{
           // this.addressBookList  = titles.toArray();
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