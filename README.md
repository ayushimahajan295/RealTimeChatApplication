# RealTimeChatApplication
Developed a Real-Time Chatting Application in Java and using relevant frameworks. The project is responsive and has an efficient chat interface.
Features like user authentication, message encryption, and multimedia support are incorporated.

This project is developed in IntelliJ IDE.

The following are the dependencies to run the project:
- Download openjdk-22.0.2 from https://gluonhq.com/products/javafx/
- Go to File -> Project Structure -> Add jar files in Libraries and openjdk-22 in Modules -> Click Apply ->OK
- Go to Main -> right-click on the Main.java -> Modify Run Configuration... -> Add VM options -> <br/>
```--module-path "D:\Program Files\My project\openjfx-22.0.2_windows-x64_bin-sdk\javafx-sdk-22.0.2\lib" --add-modules javafx.controls,javafx.fxml``` <br/>
 write this in VM options not program arguments -> Click Apply -> OK
  Repeat the same for Main2.

To run the file:
Follow the below steps and run the SimpleHttpServer file to start the Server:

- Open IntelliJ IDEA and open your RealTimeChatApp project.
- Navigate to the SimpleHttpServer file in the src directory.
- Right-click on SimpleHttpServer.java and select Run 'SimpleHttpServer.main()'

This indicates the server is started and shows the message: Server started on port 8000...

Next, run the Main and Main2 classes in IntelliJ IDEA:

- Navigate to Main.java and right-click on it.
- Select Run 'Main.main()'.
- Repeat for Main2.java.

These two instances will now connect to the HTTP Server and exchange messages in real-time.
The default login credentials are user1, password and user2, password which can be modified from code if needed.

 
