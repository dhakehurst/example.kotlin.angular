import React from 'react';
import logo from './logo.svg';
import './App.css';

import userApi from './services/userApi.service'
import AddressBookList from './addressBookList/AddressBookList'

const App: React.FC = () => {
    let p = new Promise(resolve => {
        userApi.init(resolve);
    });
//how to wait ?
    return (
        <div className="App">
            <AddressBookList />
        </div>
    );
};

export default App;
