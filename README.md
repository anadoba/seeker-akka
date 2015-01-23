seeker-akka
===========

Akka-based project for Scala Programming classes' completion.

User inputs `depth` value and `starting_page`. Seeker goes to the `starting_page`, seeks for HTML links, saves them, and launches another seekers for each page. We have now 1 iteration completed. Seekers continue with gathering links, assign them to another group of actors, until we hit specified `depth`.