**Compsci331 2021 S1. Concert Booking Service Project.**

### Team members:
- Dane Lyttinen
- Rory Mclean
- Nathan Longhurst

### Summary of what each team member did:
- We all agree that each member played an equally valuable role in the team.
- Rory and Dane worked on the project 1-2 weeks before Nathan, hence why there's no commit messages on Master from Nathan until the end. However, Nathan ended up playing an en equally valuable role in the team, by doing the entire project on his own branch (up until publish / subscribe) tests, so that he could bring insights over to the team on the Master, e.g., pointing out forgotten JPA annotations, or a better way to do implement token based authentication.
- Throughout the project, all team members had many discussions with the team.

- Although we all touched on similiar parts of the code, there were some parts down individually:
  - Dane's work notably included: figuring out how to minimise concurrency errors
  - Rory's work notably included: doing publish / subscribe request handlers.
  - Nathan's work notably included: making a pass over all of the code on master to clean up the code, add documentation to request handlers, and making logs consistent.


### Strategy used to minimise the chance of concurrency errors:
- Login - You don't want a User to change username or etc when trying to Login, check version with OPTIMISTIC_FORCE_INCREMENT and OPTIMISTIC.
- Booking - You don't want a booking to be changed while requesting it. You don't want the requested Seats to get booked while viewing hence pessimistic_read
- ConcertService - you don't want to change concerts while users are reading them hence pessimistic read lock

### How domain model is organised:
- We made Concert, Performer, and User domain model classes so that the code would work with db_init.sql. We also made a Seat domain model class, as the project files show a Seat table in the database.
- We also decided to make a Booking domain model class, as a means to store booking information (this can be extended in the future to include payment information), as opposed to e.g. storing the seats the user has booked with a User, which isn't as extendable.

- Concert has FetchMode subselect for its Performer collection field. As there will be multiple, we reduce the pre load by only fetching the performers for concerts when needed.
- For Booking and Concert we want the collections of objects they store to be persisted when these are persisted, so these classes use FetchType.CASCADE. We don't use FetchType.REMOVE because want the colletions of objects they store (e.g. Performers) to live on even after a Booking or Concert is removed (which might be a feature that happens in the future). 
- Booking uses eager fetching for its Seat collection as we deemed that most of the time, we'd be accessing the entirety of this collection.

- We wanted a way to persist a user’s token value in the database, so that we didn’t have to store this in memory (i.e. so that we could have “stateless services”).
We decided to make a ‘token’ field in the User class to accomplish this. 

Note: we made multiple Resource classes for good practice and ease.
