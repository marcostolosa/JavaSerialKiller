# Java Serial Killer

Burp extension to perform Java Deserialization Attacks using the ysoserial payload generator tool.

Blog https://blog.netspi.com/java-deserialization-attacks-burp/

Chris Frohoff's ysoserial (https://github.com/frohoff/ysoserial)

## Download & Requirements

Download from the Releases tab: https://github.com/NetSPI/JavaSerialKiller/releases

Requirements: Java 8 

## Instructions

1. Right-click on a request and select Send to Java Serial Killer
2. Highlight the area or parameter you want the serialized Java object to replace
3. Select the payload that you want, type in the command, choose to base64 encode or not, and press Serialize

  *Note: You do not need to re-highlight the serialized Java object if you change the payload or command. It will automatically update the request with the correct serialization in the spot that you highlighted the first time, even if you base64 encode it.
  
4. From here you can press Go button to send the request or right-click and send it to another tool.
