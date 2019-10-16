# File Sharing and Managment System
Distributed Systems 
Assignment 2
Kanav Gupta 20161151

## Running Client and Server

### Server

Compile FileServer.java in the required directory
```javac FileServer.java``` 
running server
```java FileServer```

### Client
Compile Client.java in the required directory
```javac Client.java``` 
start Client :
```java Client```

## Commands
```create_user <username>```
Creates User and log it in. (Note: this should be the first command after running client)

```upload <filename/filepath>``` 
Upload the file present on provided path to the server using TCP

```upload_udp <filename/filepath>```
Upload the file present on provided path to the server using TCP

```create_folder <foldername>```
Upload the file present on provided path to the server using UDP

```move_file <source_path> <dest_path>```
Move file from source path to destination path

```create_group <groupname>```
Creates new group with given group name

```list_groups```
List all present groups

```join_group <groupname>```
User joins the group

```leave_group <groupname>```
User leaves the group

```list_detail <groupname>```
Gives details of all users who are member of group and the files they uploaded

```share_msg <groupname> <text>```
Shares text with all members of the group

```share_msg_to_all <text>```
Shares text with all the groups user is part of

```get_file <groupname/username/file_path>```
Download files of other user if they share any of the group

Note: All major error cases have been handled carefully.