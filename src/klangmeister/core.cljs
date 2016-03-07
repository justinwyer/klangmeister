(ns klangmeister.core
  (:require
    [klangmeister.processing] ; Import action defs.
    [klangmeister.actions :as action]
    [klangmeister.ui.view :as view]
    [klangmeister.framework :as framework]
    [reagent.core :as reagent]
    [reagent.session :as session]
    [accountant.core :as accountant]
    [secretary.core :as secretary :include-macros true]))

(defn audio-context
  "Construct an audio context in a way that works even if it's prefixed."
  []
  (if js/window.AudioContext. ; Some browsers e.g. Safari don't use the unprefixed version yet.
    (js/window.AudioContext.)
    (js/window.webkitAudioContext.)))

(defonce state-atom (reagent/atom {:audiocontext (audio-context)}))

(secretary/defroute "/klangmeister/"            [query-params] (session/put! :gist (:gist query-params))
                                                               (session/put! :current-page view/about))

(secretary/defroute "/klangmeister/index.html"  [query-params] (session/put! :gist (:gist query-params))
                                                               (session/put! :current-page view/about))

(secretary/defroute "/klangmeister/synthesis"   [] (session/put! :current-page view/synthesis))
(secretary/defroute "/klangmeister/performance" [] (session/put! :current-page view/performance))
(secretary/defroute "/klangmeister/composition" [] (session/put! :current-page view/composition))
(secretary/defroute "/klangmeister/reference"   [] (session/put! :current-page view/reference))
(secretary/defroute "/klangmeister/about"       [] (session/put! :current-page view/about))

(def handle!
  "An handler that components can use to raise events."
  (framework/handler-for state-atom))

(defn current-page
  "Extract the current page from the session and use it to build the page."
  []
  [(session/get :current-page) handle! state-atom])

(defn mount-root []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (let [default (or (session/get :gist) "4b04fd7f2d361c6604c4")]
    (handle! (action/->Import default :main)) ; Pull in the content of the main code pane.
    (reagent/render [current-page] js/document.body)))

(mount-root)
