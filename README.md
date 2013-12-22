About
=====

#### This is still a work-in-progress project

I have been tinkering around with an application that runs on top of Google App Engine for a while now.

I have decided to make my work available for others to play around with. Here you will find some rather useful pieces of code that will help you get started with designing a highly modular application using the following technology stack:

* JSF
* JPA
* CDI

Think of this project as my imaginary car engine that I keep tinkering every time I get a few hours of spare time :)

As my project matures, so will this README file.

Getting Started
==============================

1. Clone the repository to your local machine
2. Ensure that you have [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (7 or higher) & [Gradle](http://www.gradle.org) installed on your machine
3. From the repository root directory (*GIT_ROOT*) execute the following commands
    * gradle setupEnv - This will download the AppEngine SDK
    * gradle generateDocs - This will generate the Javadocs for all the modules under *GIT_ROOT/tmp/javadoc*
4. Navigate to the webapp module. Compile and run it.
    * cd modules/webapp ; gradle run

      *This will start the GAE/J server on port 8888 (debug port 9999)*

    * Access the app @ http://locahost:8888/ & http://localhost:8888/admin
    <br/><br/>


5. Till I put up some more documentation, you will just have to rummage through the code to see the goodies that I have put in there.

Things that you will find here
==============================

* Gradle + GAE integration
* GAE + JSF + CDI => Tweaks
* DataNucleus Customizations - One persistence-unit and distributed entity classes
* Designing a modular JSF application
* Using the Scripting support provided in JDK to execute Java code on the server using the browser
* Probably a bit of unusual coding styles :)

Remember, all of this is still WIP. I have been working on this project for a rather long period of time, so it is likely to have junk that needs to be cleaned out as well.
