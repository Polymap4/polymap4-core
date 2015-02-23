# Polymap4 Runtime

## Cache

...

## Event System

...

## Session Context ([core.runtime.session](src/org/polymap/core/runtime/session))

Provides a unique API to access the context of the session connected to the current thread. This is especially useful if sessions contexts are not just provided by RAP. For example if servlets or other services (GeoServer/OGC, WebDAV, etc.) running outside RAP.

  - [SessionContext](src/org/polymap/core/runtime/session/SessionContext.java): API
  - [SessionSingleton](src/org/polymap/core/runtime/session/SessionSingleton.java)

## I18N ([core.runtime.i18n](src/org/polymap/core/runtime/i18n))

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