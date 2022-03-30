# More Fair


## Steps to run locally:
- Download postgress, launch the pgAdmin tool that comes bundled with it(First time setup will require making a password 
that you will need to use the admin tool and to connect with moreFair) and create a database named "moreFair"
- Set spring.datasource.password property in application-dev.properties or in your command line arguments to your postgres user password.  If you use a different user then change spring.datasource.username as well.
- To launch the server, either run the two commands below under commands, or run the first command and then launch the server via your editor of choice.  If successful you should be able to go to localhost:8080 and start playing the game.


##Steps to test locally:
- Meaningful progress when running locally requires changing many of the settings in FairController.  Changing the following will let you progress with two players by opening two different browsers. What you intend to test will dictate what theese setting should be changed to, but here are some examples for testing to AH ladder quickly.
1. MINIMUM_PEOPLE_FOR_PROMOTE = 2
2. POINTS_FOR_PROMOTE = 300
3. BASE_ASSHOLE_LADDER = 2
4. ASSHOLES_FOR_RESET = 2


##Reset Test Servers:
- In order to reset the server completely, simple spin the spring boot server down then right click on the DB in pgAdmin and choose delete.  Then right-click on the postgresSQL server and create the DB again with the same name of 'moreFair'.  Spring boot data will recreate the tables and populate the initial ladder.


## Needed:

- PostgreSQL DB

### Commands:

mvn clean package -Dmaven.test.skip=true

java -jar -Dspring.profiles.active=prod -Dspring.datasource.password=pw morefair-1.0.jar

### Mechanics

1. Start-Ladder
    1. Points += Power
    2. Power += (rank-1 + bias)*multi
    3. Buy Multi for ladder^multi+1 Power (Reset Power, Points & Bias)
    4. Buy Bias for ladder^bias+1 Points (Reset Points)
    5. Promote at Rank 1 with at least 250M Points & 10 or more Players
    6. Name-Change for an alias
2. Unassigned Mechanics
    1. Grapes to Auto-Promote someone else
    2. Buy Bulk Bias