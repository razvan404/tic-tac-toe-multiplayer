# <b>Tic Tac Toe Multiplayer</b>

<h2><b>A brief view of the project</b></h2>
GUI-driven Tic Tac Toe game, the players communicate with the server through a TCP connection, when a player connects, the server waits for another one to connect as well and the game begins. Whenever 2 clients connects to the server, a new thread is created, so that the server can hold whatever number of games simultaneously.<br><br>
The application is implemented using GRASP principles, layered arhitecture, Computer Networks concepts (client-server model) and Object Oriented Programming concepts.<br><br>
The development of the application was made using C (server backend), Java (client backend), Gradle (dependencies), JavaFX (client user interface) and Cascade Style Sheets (designing the user interface).<br><br>

<h2><b>Features</b></h2>
<h3>• <b>connect page</b></h3>
<img src="https://i.imgur.com/48iAxzE.png"></a>
<h3>• <b>waiting page</b></h3>
<img src="https://i.imgur.com/TnOzmJL.png"></a>
<h3>• <b>grid page</b></h3>
<img src="https://i.imgur.com/UOMbl6a.png"></a>
<h3>• <b>verdict page</b></h3>
<img src="https://i.imgur.com/CrXgdKI.png"></a>

<h2><b>Mentions</b></h2>
The application icons an the load gif were taken from <a href="https://icons8.com/">icons8</a>. The server can run only on Linux OS because it uses libraries such as sys/socket.h, netinet/in.h and arpa/inet.h which are not compatible with the Windows OS.