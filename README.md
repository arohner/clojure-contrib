What is this?
=============
This is Clojure Contrib, 1.2.0, patched to compile against Clojure 1.3 (currently, 1.3-RC0)

Why does it exist?
==================
Because I have projects with significant dependencies against contrib-1.2, but want to use clojure 1.3 features. The standard 1.2 can't be used because of contrib functions being promoted to core, and a few bug fixes, and some contrib libraries calling binding on non-earmuffed variables.

Does it work?
=============
It works for me. All the tests pass, except for a few minor pprint tests that I haven't spent the time looking into yet.

How do I use it?
===============
Check it out, then 
`mvn install`
Change your clojure contrib dependency to 
[org.clojure/clojure-contrib "1.3-compat"]

Note that you'll have to host the jar in a private maven repo yourself, or mvn install it on all boxes that want to use it.

Why isn't this hosted in maven repos?
=====================================
Because this isn't official, and I don't own the org.clojure namespace. I could change it to org.clojars.arohner, but that would defeat the point of replacing the current contrib :-)

