Done
--	File WebServer only.
--	CRUD operations for ascii files.
--	Run as non-root only
--	Bound to localhost loop-back only
--	Config.xml - few entries used for demonstration.
--  Basic infra is up now change gears to HTTP version 2.0.
Not done
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
--      Config file existence not checked.
--      Directory listing not implemented.
	
******************************************
MUST READ SECTION STARTS HERE. PLEASE READ
******************************************

BUILD:
-----
1) Go to build dir and run ant.
2) Build docker image "sudo docker build -f Dockerfile -t exploreavi/webserver:v1 ."

RUN:
----
1) Update config file to set IP and PORT on which server should listen.
   Note: config is a xml file outside of jar.
2) Execute "java -jar ws.jar <config_file_path>"

HOW I HAVE TESTED SO FAR:
--------------------------
-- I have used telnet client to test the keep-alive feature. 
   Because I can create raw request.
-- Basic load test worked fine.
-- curl, rest client or browser worked for simple CRUD operations.
-- Non trivial operations like multipart upload etc., might not work as I have not handled all headers and their values.
-- The connection gets closed if either server or client terminates.
-- Default timeout is 10 sec.

MORE:
----
--	HttpParse.java taken from internet and modified.
    http://www.java2s.com/Code/Java/Network-Protocol/HttpParser.htm
