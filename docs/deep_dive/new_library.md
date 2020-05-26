# Supporting a new library

One benefit of Disparse is that it can be made to work with essentially any text-based message system.  That is to say, Disparse could just as easily be a command library for CLI-apps, other chats such as Slack or Messenger, or simply another Discord library that is not supported yet.

The flexibility of this modularized project means that only a single class, Dispatcher, really has to be implemented for Disparse to understand how to dispatch commands.

The class to implement would look like:

```java
public class Dispatcher extends Helpable<E, T> {}
```

There are a few methods to implement; However, in the case of Discord, they are fairly trivial.  The rest of the help commands will be implemented using these building blocks.

Looking at the implementations for JDA, D4J, or SmallD would be very useful as they are all quite similar.  If there are specific questions on how to get a library you are interested in supported, submit an issue!
