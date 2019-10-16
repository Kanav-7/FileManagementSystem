# File Sharing and Managment System
Distributed Systems </br>
Assignment 2 <br>
Kanav Gupta <br> 20161151 <br>

## Running Client and Server

### Server

Compile FileServer.java in the required directory <br>
```javac FileServer.java``` 
running server <br> 
```java FileServer```

### Client
Compile Client.java in the required directory <br>
```javac Client.java``` 
start Client <br>
```java Client```

## Commands
- ```create_user <username>``` <br>
Creates User and log it in. (Note: this should be the first command after running client)

- ```upload <filename/filepath>``` <br> 
Upload the file present on provided path to the server using TCP

- ```upload_udp <filename/filepath>``` <br>
Upload the file present on provided path to the server using TCP

- ```create_folder <foldername>``` <br>
Upload the file present on provided path to the server using UDP

- ```move_file <source_path> <dest_path>``` <br>
Move file from source path to destination path

- ```create_group <groupname>``` <br>
Creates new group with given group name

- ```list_groups``` <br>
List all present groups

- ```join_group <groupname>``` <br>
User joins the group

- ```leave_group <groupname>``` <br>
User leaves the group

- ```list_detail <groupname>``` <br>
Gives details of all users who are member of group and the files they uploaded

- ```share_msg <groupname> <text>``` <br>
Shares text with all members of the group

- ```share_msg_to_all <text>``` <br>
Shares text with all the groups user is part of

- ```get_file <groupname/username/file_path>``` <br>
Download files of other user if they share any of the group

Note: All major error cases have been handled carefully.
