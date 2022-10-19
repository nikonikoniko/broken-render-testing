# Shadow Clojurescript Boilerplate

install the app:

```
npm install
```

this will install all of the necessary js libraries as well as shadow-cljs

then, you can start the app for development:

```
npx shadow-cljs watch :frontend :backend
```

this will make your app available at `localhost:3000`

you can connect to the running app with the following command:

```
npx shadow-cljs cljs-repl :frontend
```

once there, you can see what is being done in the browser, as well as influencing it. 

try running `(js/alert "hello from the repl")` from this repl!

in emacs, you can connect with `cider-connect-cljs`, which connects to an nrepl, and run it directly in your editor.
