# More Fair

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