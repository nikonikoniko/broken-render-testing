(ns sfm.server
  (:require ["express" :as express]
            [clojure.string :as string]
            ["passport" :as passport]
            ["passport-magic-link" :as passport-magic-link]
            ["cookie-parser" :as cookie-parser]
            ["body-parser" :as body-parser]
            ["express-session" :as express-session]))

 ;; currently broken in shadow-cljs
 (set! *warn-on-infer* true)

 ;; This should become a mail service
(def sendToken
  (fn [user token]
    (let [email (. user -email)]
      (println (str "login link: /auth/magiclink/callback?token="
                    token)))))

 (def verifyUser (fn [user] (js/Promise.resolve user)))

 (.serializeUser passport (fn [user done] (done nil (. user -email))))
 (.deserializeUser passport (fn [id done] (done nil (clj->js {:email id}))))

 (defonce server (atom nil))

 (def loginPage "
  <script>
   let e = prompt('enter email');
   fetch('/auth/magiclink', { headers: {'Accept': 'application/json',
                                        'Content-Type': 'application/json'},
                              method: 'POST',
                              body: JSON.stringify({email: e})})
        .then(() => window.location.replace('/check-your-inbox'))
 </script>
")

 (defn deliver [^js/express.Response res return-map]
   (.json res (clj->js return-map)))

(defn start-server []
  (println "Starting server")

  (let [app               (express)
        MagicLinkStrategy (. passport-magic-link -Strategy)
        strategy          (MagicLinkStrategy. (-> {:secret     "asdhasdkhsdakjhdsakjh"
                                                   :userFields ["email"]
                                                   :tokenField "token"}
                                                  clj->js)
                                              sendToken
                                              verifyUser)
        ensure-logged-in  (fn [^js/express.Request req res next]
                            (let [logged-in? (.isAuthenticated req)]
                              (when (not logged-in?)
                                (.send res "Not authorized. <a href='/login'>login</a>"))
                              (next))
                            )

        ]
    ;; app initializations
    (.use app (cookie-parser))
    (.use app (body-parser))
    (.use app (express-session (clj->js {:secret            "asdjhasdkjashdkjashdkasjhdkasjdh"
                                         :resave            false
                                         :saveUninitialized false
                                         :cookie            { "secure" false }})))
    (.use passport strategy)
    (.use app (.initialize passport))
    (.use app (.session passport))
    ;;(.use app (.authenticate passport "session"))

    ;; auth routes

    (.post app "/auth/magiclink"
           (.authenticate passport "magiclink" (clj->js {:action "requestToken"}))
           (fn [req res] (.redirect res "/check-your-inbox")))
    (.get app "/check-your-inbox"
          (fn [req res] (.send res "Check your inbox for a login link")))
    (.get app "/auth/magiclink/callback"
          (.authenticate passport "magiclink" (clj->js {:action     "acceptToken"
                                                        :allowReuse "true"}))
          (fn [req res] (.redirect res "/")))

    ;; login routes

    (.get app "/login" (fn [req res] (.send res loginPage)))
    (.get app "/logout" (fn [^js/express.Requqst req res]
                          (.destroy (. req -session))
                          (.redirect res "/login")))

    ;; api routes

    (.get app "/api"
          ensure-logged-in
          (fn [req res] (.send res "Helloooo, world")))

    ;; frontend behind a password:

    (.use app "/" ensure-logged-in (.static express "./dist/public"))

    ;; set the app to listening:
    (.listen app 3000 "0.0.0.0" (fn [] (println "Example app listening on port 3000!")))))

 ;; called by main and after reloading code
 (defn start! [] (reset! server (start-server)))

 ;; called before reloading code
 (defn stop! [] (.close @server) (reset! server nil))

 ;; executed once, on startup, can do one time setup here
(defn main [] (println "main") (start!))
