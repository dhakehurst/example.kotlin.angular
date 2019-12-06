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
import {Router, RouteComponentProps} from "@reach/router";
import userApi from './services/userApi.service'
import AddressBookList from './addressBookList/AddressBookList'
import AddressBookView from './addressBookView/AddressBookView'
import logo from './logo.svg';
import './App.css';

class AppState {
    ready = false;
}

export interface AppProps extends RouteComponentProps {

}

export default class App extends React.Component<AppProps, AppState> {

    constructor(props: AppProps) {
        super(props);
        this.state = new AppState();
    }

    render() {
        console.log("start");

        return this.state.ready ? (
            <article>
                <header className='App-header'>
                    <img src={logo} className="App-logo" alt="logo"/>
                </header>
                <section>
                    <Router>
                        <AddressBookList path='/'/>
                        <AddressBookView path='/addressBook'/>
                    </Router>
                </section>
            </article>
        ) : (<article>
                <header>
                    <img src={logo} className="App-logo" alt="logo"/>
                </header>
                <section>
                    <span>Waiting for websocket to connect...</span>
                </section>
            </article>
        );
    }

    componentDidMount(): void {
        new Promise(resolve => {
            userApi.init(resolve);
        }).then(() => {
            this.setState({ready: true});
        });

        console.log("initialised");
    }

};

