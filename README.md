# The Inter-Pi Connection Thingamajigger
Connect your Raspberry Pis with the Inter-Pi Connection Thingamajigger today!

This is a simple system, designed for Raspberry Pis, that lets you connect a chat server and clients over a network.

## How to use
First, find your computer's local (private) IP address. This can be tricky, but on [Raspbian](https://en.wikipedia.org/wiki/Raspbian) just run this command:
```
hostname -I
```
If you are using something besides Raspbian, have a look at [this article](https://www.howtogeek.com/236838/how-to-find-any-devices-ip-address-mac-address-and-other-network-connection-details/) and just ignore the parts on MAC addresses.

### Raspberry Pi / Other Linux/Unix
Now run the IPCT Server with:
```
java -jar path/to/the/file/IPCTServer.jar
```
Next, you will want to connect. Run the client:
```
java -jar path/to/the/file/IPCTClient.jar
```
Type in your name and the server computer's IP address, wait for others to do the same, and have fun ~~yakking your head off~~ chatting!

On Raspbian, if you want this to be double-clickable, open the file manager and right-click either the client or server JAR. Click Open With... and Custom Command Line. In the first text box, type `java -jar %f`. In the second text box, type a name for this command (I used `Java (Run Jar)`). Finally, make sure the `Set selected application as default action for this file type` box is checked. This will tell the system to run the file when it is double-clicked.
