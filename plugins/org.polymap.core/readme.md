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

Provides a simple API for internationalization. 

  - translate and format messages
  - handle common key prefix
  - get messages from resource bundle (ClassLoader) or workspace file 

## Config - [core.runtime.config](src/org/polymap/core/runtime/config)

Provides a system for config style properties, avoiding getter/setter code. 

  - avoid a lot of declaration/getter/setter code
  - define a clear API to access a [Property](src/org/polymap/core/runtime/config/Property.java), which is separated from algorithm interface
  - define special behaviour via annotations, making it part of the declaration (instead of hiding inside the set() method)
  - concrete property instances are injected by [ConfigurationFactory](src/org/polymap/core/runtime/config/ConfigurationFactory.java) 

## Security

...

## State Tracker

...

## Logging

...

## Operations

...