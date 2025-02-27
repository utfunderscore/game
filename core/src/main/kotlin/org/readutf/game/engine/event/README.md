# Event System

## Summary
The goal of the game event system is to filter events and map them to their relevant game. For example,
if PlayerDeathEvent is called it should only be available to listeners that are for the game that the player is in.

```Native event -> Game Event Adapter -> Event (with Game) -> Game Event Listener```

