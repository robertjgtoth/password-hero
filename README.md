# password-hero [![Build Status](https://travis-ci.org/robertjgtoth/password-hero.svg?branch=master)](https://travis-ci.org/robertjgtoth/password-hero)
A very simple password management utility written in java.

## Subprojects
- core
    - Core password management functionality like encryption and storage/retrieval.
- standalone
    - Simple standalone desktop UI written in JavaFX.
    - To run using gradle: <code>gradlew run</code>
    
## Building
- This application builds from the top level using gradle: <code>gradlew clean assemble</code>
- Build products:
    - Jar containing core functionality: <code>core/build/libs/password-hero-core-\<version\>.jar</code>
    - Distribution containing the standalone desktop UI: <code>standalone/build/distributions/password-hero-standalone-\<version\>.[tar|zip]</code> 

