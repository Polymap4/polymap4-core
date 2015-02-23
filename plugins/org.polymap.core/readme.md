# Polymap4 Runtime

## Cache

Cache implementations based on javax.cache API.

## Event System - [core.runtime.event](src/org/polymap/core/runtime/event)

Global event based communication system. Software components can register for events and other components can send event notifications via this event bus.
 
  - central event hub, subscriber does not need to know the publisher
  - asynchronous event synchronous event propagation
  - declare event handler methods via [EventHandler](src/org/polymap/core/runtime/event/EventHandler.java) annotation
  - annotate event handling: run in Display thread, collect events, event scope
  
## Session Context - [core.runtime.session](src/org/polymap/core/runtime/session)

Provides a unique API to access the context of the session connected to the current thread. This is especially useful if sessions contexts are not just provided by RAP. For example if servlets or other services (GeoServer/OGC, WebDAV, etc.) running outside RAP.

  - [SessionContext](src/org/polymap/core/runtime/session/SessionContext.java): API
  - [SessionSingleton](src/org/polymap/core/runtime/session/SessionSingleton.java)

## I18N - [core.runtime.i18n](src/org/polymap/core/runtime/i18n)

Simple API for internationalization. 

  - translate and format messages
  - handle common key prefix
  - get messages from resource bundle (ClassLoader) or workspace file 

## Security

...

## State Tracker

...

## Logging

...

## Operations

...