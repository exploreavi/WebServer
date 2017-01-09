Please forgive my brevity of features. I could do only following in this limited time.

Done
--	File WebServer only.
--	CRUD text files.
--	Run as non-root only
--	Bound to localhost loop-back only
--	Config.xml - few entries used for demonstration.

Not done
--	Synchronize threads for unsafe request(POST, PUT, DELETE). 
--	Binary files not tested.
--	Multiple-part form not implemented.
--	Did not use query params though they have been parsed and are available in a hash.
--	Clients like curl, wget, siege not working for all http methods.
--	Files with white spaces and special characters etc. not tested.
--	Header Content-Type: not used.
--	Should modify file if and only if system user owns it.
--	Tested only on Ubuntu.
--	Not tested with IPv6.
--	What happens if a request results in an exception. Simple, that thread is gone!!
-- 	Could not write Unit Test cases.
-- 	Could do Performance test.
--  Config file existence not checked
	
Compilation:
    // user java 1.7 because of getLoopbackAddress
		javac -cp . webserver/*.java


MUST READ: How I have tested so far: 

I have used telnet client to check keep-alive feature.
Until HTTP-Request does not contain "Connection: close" header
the connection is not closed.
The connection get closed if either server or client terminates.

SERVER NOT STARTED:
-------------------
adiwakar@adiwakar-ThinkPad-T420:~/toy_app$ telnet localhost 12345
Trying 127.0.0.1...
telnet: Unable to connect to remote host: Connection refused

SERVER STARTED:
---------------
adiwakar@adiwakar-ThinkPad-T420:~/toy_app$ telnet localhost 12345
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.

TEST INCORRECT HTTP VERSION:
----------------------------
GET /test.txt HTTP/2.0
Host:

HTTP/1.1 505 HTTP Version Not Supported
Content-Length: 0
Connection: Keep-Alive
Date: Mon Jan 09 09:12:21 IST 2017

TEST UNSUPPORTED METHOD:
-----------------------
TRACE /test.txt HTTP/1.1
HTTP/1.1 501 Not Implemented
Content-Length: 0
Connection: Keep-Alive
Date: Mon Jan 09 09:12:43 IST 2017

GET NON-EXISTING FILE:
----------------------
GET /test.txt HTTP/1.1
Host: localhost

HTTP/1.1 404 File Not Found
Content-Length: 0
Connection: Keep-Alive
Date: Mon Jan 09 09:13:15 IST 2017

CREATED THE FILE:
-----------------
POST /test.txt HTTP/1.1
Host: localhost
Content-Length: 13

hello-world
HTTP/1.1 201 Created
Content-Length: 0
Connection: Keep-Alive
Date: Mon Jan 09 09:14:13 IST 2017

TRY UPDATING FILE USING POST(USE PUT INSTEAD):
----------------------------
POST /somefile.txt HTTP/1.1
Host: localhost
Content-Length: 13

hello-worldHTTP/1.1 409 Conflict
Connection: Keep-Alive

USE PUT TO UPDATE THE FILE:
--------------------------
PUT /test.txt HTTP/1.1
Host: localhost
Content-Length: 13

ooooo-oorrr
HTTP/1.1 200 OK
Content-Length: 0
Connection: Keep-Alive
Date: Mon Jan 09 09:15:04 IST 2017

TRUNCATE THE FILE:
-----------------
PUT /test.txt HTTP/1.1
Host:localhost
Content-Length: 0

HTTP/1.1 200 OK
Content-Length: 0
Connection: Keep-Alive
Date: Mon Jan 09 09:21:38 IST 2017

BRING BACK NEW CONTENT:
----------------------
PUT /test.txt HTTP/1.1
Host:localhost
Content-Length: 13

hello-world
HTTP/1.1 200 OK
Content-Length: 0
Connection: Keep-Alive
Date: Mon Jan 09 09:22:09 IST 2017

DELETE THE FILE:
---------------
DELETE /test.txt HTTP/1.1
Host:

HTTP/1.1 200 OK 
Content-Length: 0
Connection: Keep-Alive
Date: Mon Jan 09 09:33:06 IST 2017

TRY DELETING AGAIN:
------------------
DELETE /test.txt HTTP/1.1
Host:

HTTP/1.1 404 File Not Found 
Content-Length: 0
Connection: Keep-Alive
Date: Mon Jan 09 09:33:17 IST 2017

FORBID PUT TO UPDATE A DIRECTORY:
--------------------------------
PUT / HTTP/1.1
Host: localhost
Content-Length: 13

HTTP/1.1 403 Forbidden 
Content-Length: 0
Connection: Keep-Alive
Date: Mon Jan 09 09:37:10 IST 2017

404 WHEN PUT TRY TO UPDATE NON-EXISTING FILE:
--------------------------------------------
PUT /test.txt HTTP/1.1
Host:
Content-Length: 13

HTTP/1.1 404 File Not Found 
Content-Length: 0
Connection: Keep-Alive
Date: Mon Jan 09 09:38:53 IST 2017

MORE:
----
--	HttpParse.java taken from internet and modified.
    http://www.java2s.com/Code/Java/Network-Protocol/HttpParser.htm
