# React Client Notes

## Typscript Compiler

Unfortunately the default app created with create-react-app uses babel to transpile the
typescript code. This transpiler has limitations, namely only ES6 imports are supported
and namespaces are not properly supported.

Hence I have ejected the configuration, and replaced babel-loader with awesome-typescript-loader.
Which uses the normal typescript compiler. Build takes a little longer but you get
full typescript support.

## CLI args
also the default react app does not support any command line args for modifying the 
default conventions of react. In particular, I need to change the output directory.
Not a problem, add 'yargs' to the path.js script that react ejected, and 
modify the script to use a (y)arg if it is passed.
