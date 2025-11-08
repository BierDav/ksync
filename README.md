we need to set busy timeout so that writes during a rebase won't get errors but just wait for the rebase to finish

Rebase:

1. We start listening to Websocket and keep them in queue (if a event with the same id is already in queue we will override it)
2. We simultaneously
   2.1 push our outgoing events
   if event is successfully applied we will add it to queue

2.2 query all new events and put them to the queue

3. we wait for both to finish
4. now we can start processing the queue (we remember last event in queue)
   4.1 check if first event from queue is really the next for our truth table (maybe there is a hole in the queue)
   4.2 If there is a hole, we need to fetch it and add it to queue. Then restart the current loop
   4.3 apply the event to truth db
   4.4 If the queue is empty
   5.1 Check for unconfirmed events in local db, if there are none we will use the last event id
   5.2 We go from oldest to newest and try to confirm the events
   5.3 If all events in local are confirmed, we lock the db and check the the last event ids.
   6.1 If different, and apply the events from truth db as confirmed
   5.4 If there are still some unconfirmed events, we have to rebase
   6.1 We copy truth db and emit a new connection to the frontend
   6.2 Close the old connections and try to delete all old databases