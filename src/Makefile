JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $*.java

CLASSES = \
	server/ServerStarter.java \
	client/ClientStarter.java

default: classes

classes:
	$(JC) server/ServerStarter.java
	$(JC) server/ServerImpl.java
	$(JC) server/ServerGui.java
	$(JC) client/ClientStarter.java
	$(JC) client/ClientImpl.java
	$(JC) client/ClientGui.java
start:
	rmiregistry &
	sleep 2
	xterm -e java server/ServerStarter Razorback1 &
	
	xterm -e java server/ServerStarter Razorback2 &

	sleep 2
	xterm -e java client/ClientStarter client1 Razorback1 3 A 4 B 5 &
	xterm -e java client/ClientStarter client2 Razorback1 5 J 2 &
	xterm -e java client/ClientStarter C1 Razorback2 3 E 2 &
stop:
	killall rmiregistry & 
	sleep 1
	killall xterm &
clean:
	$(RM) server/*.class
	$(RM) client/*.class
