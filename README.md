# The Inter-Pi Connection Thingamajigger
Connect your Raspberry Pis with the Inter-Pi Connection Thingamajigger today!

This is a simple system, designed for Raspberry Pis, that lets you connect a chat server and clients over a network.
Since the IPCT is written in Java, it can be run on any system with a JRE version 8 (1.8) or above. That is,

**The IPCT requires Java 8 or later.**
On Raspbian, you can run `sudo apt install oracle-java8-jdk` as the `pi` user to install Java 8, but if that doesn't work, PLEASE let me know.

## How to use
First, find your computer's local (private) IP address. This can be tricky, but on [Raspbian](https://en.wikipedia.org/wiki/Raspbian) just run this command:
```
hostname -I
```
If you are using something besides Raspbian, have a look at [this article](https://www.howtogeek.com/236838/how-to-find-any-devices-ip-address-mac-address-and-other-network-connection-details/) and just ignore the parts on MAC addresses.

### Raspberry Pi (Raspbian)
Now run the IPCT Server with:
```
java -jar path/to/the/file/IPCTServer8_v1.1.jar
```
Next, you will want to connect. Run the client:
```
java -jar path/to/the/file/IPCTClient8_v1.1.jar
```
Type in your name and the server computer's IP address, wait for others to do the same, and have fun chatting!

If you are having problems connecting, make sure you are on the exact same network, and then recheck the server's IP.

On Raspbian, if you want this to be double-clickable, open the file manager and right-click either the client or server JAR. Click Open With... and Custom Command Line. In the first text box, type `java -jar %f`. In the second text box, type a name for this command (I used `Java (Run Jar)`). Finally, make sure the `Set selected application as default action for this file type` box is checked. This will tell the system to run the file when it is double-clicked.

![Example Image](/doubleclickableing.png)

Click OK to run the program, and the next time you double-click on a runnable JAR file, it will be run. NOTE: The system will also try to run non-executable JARs, too, so make sure to open those differently.

### macOS
On Mac, the situation is easier. All you have to do is double-click the JAR file.

### Windows
Instructions coming soon! (or not... I typed that two years ago lol)


##### Fine Print, aka LMJ (Legal Mumbo Jumbo)
Raspberry Pi is a trademark of the Raspberry Pi Foundation.
