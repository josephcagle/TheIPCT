# The IPCT
Connect your Raspberry Pis with the Inter-Pi Connection Thingamajigger today!

This is a simple system that lets you run a chat server and clients over a network (or the Internet).

## How to use
First, pick a server computer and find its IP address. This last part can be tricky, but on a Raspberry Pi just run this command in a terminal emulator:
```
hostname -I
```

Now download the IPCT Server and run it with:
```
java -jar IPCTServer
```

Next, you will want to connect. Run the client:
```
java -jar IPCTClient
```
Type in your name and the server computer's IP address, wait for others to do likewise, and you are good to go!
