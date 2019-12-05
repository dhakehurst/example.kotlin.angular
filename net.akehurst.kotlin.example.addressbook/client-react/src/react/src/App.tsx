import React from 'react';
import { BrowserRouter as Router, Route } from "react-router-dom"
import userApi from './services/userApi.service'
import AddressBookList from './addressBookList/AddressBookList'
import AddressBookView from './addressBookView/AddressBookView'
import './App.css';

class AppState {
    ready = false;
}

export default class App extends React.Component<{}, AppState> {

    constructor(props: Readonly<{}>) {
        super(props);
        this.state = new AppState();
    }

    render() {
        console.log("start");

        if (!this.state.ready) {
            new Promise(resolve => {
                userApi.init(resolve);
            }).then(() => {
                this.setState({ready: true});
            });

            console.log("initialised");
        }
        return this.state.ready ? (
            <Router>
                <Route path='/' component={AddressBookList} />
                <Route path='/addressBook' component={AddressBookView} />
            </Router>
        ) : (<span>Waiting for connection to server...</span>) ;
    }

};

