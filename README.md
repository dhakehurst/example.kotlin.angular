[![Build Status](https://travis-ci.com/dhakehurst/example.kotlin.angular.svg?branch=master)](https://travis-ci.com/dhakehurst/example.kotlin.angular)

# example.kotlin.angular
An example to illustrate the use of kotlin modules in an angular application

## Build
```
> cd net.akehurst.kotlin.example.addressbook
> ./gradlew build
```
A zip containing the application will be in the `.gradle-build/server/distributions`

## Or just run it

With an Angular UI

```
> cd net.akehurst.kotlin.example.addressbook
> ./gradlew run -Pclient=angular
```

With a React UI

```
> cd net.akehurst.kotlin.example.addressbook
> ./gradlew run run -Pclient=react
```
