# The event-registry
Simple registry that uses Zookeeper to keep track of event types, topics etc. It is implemented as a simple wrapper around the apache curator zookeeper library that adds a simple model and zookeeper manipulation operations.

Each update to the registry can generate events that different listeners can receive. So, for exmaple, when a new EventType is registered a router component could receive this update and create any necessary topics. Likewise when a component expresses an interest in a type of event (via an ```@Consumes``` annotation, connections to the correct topic can be made.


# Connecting to the Registry

To use the registry, make a connection to a running zookeeper server and start the connection:

```
RegistryConnetion connection = new RegistryConnection("localhost:2181");
connection.setBaseKey("/streamzi")
connection.connect();
```

The connection is created with a base key argument which attaches to the specified point in the Zookeeper data. All operations are then relative to that base key.

# Updating the Registry

When a registry connection has been made, updates are made via ```RegistryOperations```. These can either run asynchronously or synchronously. For example, to register a new EventType synchronously:

```
connection.executeSync(new CreateEventType(new EventType("MeterReading"));

```
or

```
connection.execute(new CreateEventType(new EventType("MeterReading")).thenRun(
	new Runnable(){
		public void run(){
			System.out.println("Event type added");
		}
	}

);
```
for an async operation.

# Listening to changes

Registry listeners can be created to listen for changes to specific object types. For example to listen for changes to EventTypes:

```
	RegistryKeyListener<EventType> listner = connection.addKeyListener(new RegistryKeyListener<>(){
		@Override
		public void objectAdded(EventType value){
		}
		
		@Override
		public void objectRemoved(EventType value){
		}
	
		@Override
		public void objectChanged(EventType value({
		}
	
	});

```

Listeners are attached to a specific ```key``` in the Zookeeper tree and listen for child events on that key. They also keep a cache of the current state, so can be iterrogated directly:

```
	List<EventType> eventTypes = listener.getValues();
```
will return the locally cached list of event types.