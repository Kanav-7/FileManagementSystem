import java.io.* ;
import java.net.* ;
import java.util.* ;
import java.awt.* ;
import java.nio.file.Files; 
import java.nio.file.*; 

public class FileServer {
    public static Vector<Socket> ClientSockets;
    public static Vector<String> LoginNames;
    public static Vector<Group> Groups;
    public static Vector<Integer> Ports;
    public static DatagramSocket SocUDP;
    FileServer() {
        try {
            System.out.println("Server running on localhost Port-5555(TCP), 5551(UDP)");
            ServerSocket Soc = new ServerSocket(5555) ;
            DatagramSocket SocUDP = new DatagramSocket(5551);
            ClientSockets = new Vector<Socket>() ;
            LoginNames = new Vector<String>() ;
            Groups = new Vector<Group>() ;
            Ports = new Vector<Integer>();
            while(true)
            {
                Socket CSoc = Soc.accept();
                AcceptClient client_ = new AcceptClient(CSoc,SocUDP) ;
            }
        }
        catch(Exception e) {e.printStackTrace(System.out);System.exit(0);}
    }
    public static void main(String args[]) throws Exception {
        FileServer server = new FileServer() ;
    }
}

class AcceptClient extends Thread {
    Socket ClientSocket;DatagramPacket recieve_inital; DataInputStream din ; DataOutputStream dout ; String LoginName; DatagramSocket SocUDP;
    AcceptClient (Socket CSoc, DatagramSocket SocUDP_) throws Exception {
        ClientSocket = CSoc ; din = new DataInputStream(ClientSocket.getInputStream()) ; dout = new DataOutputStream(ClientSocket.getOutputStream()) ;
        SocUDP=SocUDP_;
        byte[] intial = new byte[1000];
        recieve_inital = new DatagramPacket(intial, intial.length);
        SocUDP.receive(recieve_inital);
        start() ;
    }
    public void run() {
        while(true)
        {
            try
            {
                String commandfromClient = new String() ;
                commandfromClient = din.readUTF() ;
                StringTokenizer tokenedcommand = new StringTokenizer(commandfromClient);
                String command=tokenedcommand.nextToken();
                if(command.equals("create_user")){
                    LoginName = tokenedcommand.nextToken() ;
                    File theDir = new File(LoginName);
                    if(!theDir.exists())
                        theDir.mkdir();

                    System.out.println("User "+LoginName+" logged in");
                    int port = recieve_inital.getPort();
                    FileServer.Ports.add(port);
                    FileServer.LoginNames.add(LoginName) ; 
                    FileServer.ClientSockets.add(ClientSocket);

                }
                else if(command.equals("upload")){
                    StringTokenizer st = new StringTokenizer(commandfromClient);
                    String cmd=st.nextToken(),fl=st.nextToken();
                    String st_ = din.readUTF(); StringTokenizer stt = new StringTokenizer(st_); stt.nextToken() ; int fileLength = Integer.parseInt(stt.nextToken());
                    StringTokenizer fileName = new StringTokenizer(fl,"/"); while(fileName.hasMoreTokens())fl=fileName.nextToken();
                    // C.Notify("FILE "+fl+" TCP  LENGTH "+fileLength,LoginName);
                    byte[] file_contents = new byte[1000];
                    FileOutputStream fpout = new FileOutputStream(LoginName + "/" + fl);
                    BufferedOutputStream bpout = new BufferedOutputStream(fpout);
                    int bytesRead=0,size=1000;
                    if(size>fileLength)size=fileLength;
                    while((bytesRead=din.read(file_contents,0,size))!=-1 && fileLength>0)
                    {
                        bpout.write(file_contents,0,size);
                        fileLength-=size; if(size>fileLength) size=fileLength;
                    }
                    bpout.flush();
                    System.out.println("File Recieved");
                }
                else if(command.equals("upload_udp")){
                    StringTokenizer st = new StringTokenizer(commandfromClient);
                    String cmd=st.nextToken(),fl=st.nextToken();
                    String st_ = din.readUTF(); StringTokenizer stt = new StringTokenizer(st_); stt.nextToken() ; int fileLength = Integer.parseInt(stt.nextToken());
                    StringTokenizer fileName = new StringTokenizer(fl,"/"); while(fileName.hasMoreTokens())fl=fileName.nextToken();
                    // C.Notify("FILE "+fl+" UDP LENGTH "+fileLength,LoginName);
                    byte[] file_contents = new byte[1000];
                    FileOutputStream fpout = new FileOutputStream(LoginName + "/" + fl);
                    BufferedOutputStream bpout = new BufferedOutputStream(fpout); 
                    DatagramPacket receivePacket;                   
                    int size=1024;
                    file_contents = new byte[size];
                    if(size>fileLength) size=fileLength;
                    System.out.println(fileLength);
                    while(fileLength>0)
                    {
                        receivePacket  = new DatagramPacket(file_contents, size);
                        SocUDP.receive(receivePacket);
                        bpout.write(file_contents,0,size);
                        fileLength-=size; if(size>fileLength)size=fileLength;
                    }
                    bpout.flush();
                    System.out.println("File Recieved");    
                }
                else if(command.equals("create_folder")){
                    String folder = tokenedcommand.nextToken();
                    File theDir = new File(LoginName+"/"+folder);
                    if(!theDir.exists()){
                        theDir.mkdir();
                        dout.writeUTF("Folder Created");
                    }
                    else
                        dout.writeUTF("Folder Already Exists");

                }
                else if(command.equals("move_file")){
                    String pathsrc = LoginName + "/" + tokenedcommand.nextToken();
                    String pathdst = LoginName + "/" + tokenedcommand.nextToken();
                    Path temp = Files.move(Paths.get(pathsrc), Paths.get(pathdst));
                    if(temp != null) 
                    { 
                        System.out.println("File moved successfully"); 
                        dout.writeUTF("File moved successfully"); 
                    } 
                    else
                    { 
                        System.out.println("Failed to move the file");
                        dout.writeUTF("Failed to move the file"); 
                    } 
                }
                else if(command.equals("LOGOUT"))
                {
                    FileServer.LoginNames.remove(LoginName); 
                    FileServer.ClientSockets.remove(ClientSocket) ;
                }
                else if(command.equals("create_group"))
                {
                    String groupName=tokenedcommand.nextToken();
                    if(FileServer.Groups.indexOf(groupName)==-1){
                        Group chatR = new Group(groupName, LoginName);
                        FileServer.Groups.add(chatR);
                        dout.writeUTF("Group "+groupName+" created");
                    }
                }
                else if(command.equals("list_groups")){
                    String outp="";
                    if(FileServer.Groups.size()==0) dout.writeUTF("No Groups exist");
                    else
                    {
                        for(int i=0;i<FileServer.Groups.size();i++) outp=outp+FileServer.Groups.elementAt(i).name+"\n";
                        dout.writeUTF(outp);
                    }                   
                }
                else if(command.equals("join_group"))
                {
                    String groupName=tokenedcommand.nextToken();
                    int i=0;
                    for(i=0;i<FileServer.Groups.size();i++) 
                        if(FileServer.Groups.elementAt(i).name.equals(groupName))
                        {
                            String outp=FileServer.Groups.elementAt(i).Join(LoginName);
                            dout.writeUTF(outp); 
                            FileServer.Groups.elementAt(i).Notify(LoginName+" joined the group",LoginName); 
                            break;
                        }
                    if(i==FileServer.Groups.size()) dout.writeUTF(groupName+" doesn't exist");
                }
                else if(command.equals("leave_group")){
                    String groupName=tokenedcommand.nextToken();
                    int i=0;
                    for(i=0;i<FileServer.Groups.size();i++) 
                        if(FileServer.Groups.elementAt(i).name.equals(groupName))
                        {
                            String outp=FileServer.Groups.elementAt(i).Leave(LoginName);
                            FileServer.Groups.elementAt(i).Notify(LoginName+" left the group",LoginName); 
                            if(outp.equals("DEL"))
                            {
                                FileServer.Groups.remove(FileServer.Groups.elementAt(i));
                                dout.writeUTF("You left Group"+'\n'+groupName+" deleted");
                            }
                            else dout.writeUTF(outp);
                            break;
                        }
                    if(i==FileServer.Groups.size()) dout.writeUTF("Group " + groupName+" doesn't exist");         
                }
                else if(command.equals("share_msg")){
                    String groupName=tokenedcommand.nextToken();
                    String msgfromClient=LoginName+"@"+groupName+":";
                    int i=0;
                    for(i=0;i<FileServer.Groups.size();i++){
                        if(FileServer.Groups.elementAt(i).name.equals(groupName)){
                            Group C = FileServer.Groups.elementAt(i);
                            if(C.ListUsers().indexOf(LoginName)==-1){
                                dout.writeUTF("You are not part of this group");
                                break;
                            }
                            while(tokenedcommand.hasMoreTokens()) 
                                msgfromClient=msgfromClient+" "+tokenedcommand.nextToken();
                            C.Notify(msgfromClient,LoginName);
                            break;
                        }
                    }
                    if(i==FileServer.Groups.size()) dout.writeUTF("Group" + groupName+" doesn't exist");
                }
                else if(command.equals("share_msg_to_all")){
                    String msg="";
                    while(tokenedcommand.hasMoreTokens()) 
                        msg=msg+" "+tokenedcommand.nextToken();
                    int i=0;
                    for(i=0;i<FileServer.Groups.size();i++){
                        Group C = FileServer.Groups.elementAt(i);
                        if(C.ListUsers().indexOf(LoginName)!=-1){
                            String msgfromClient=LoginName+"@"+C.name+":";
                            msgfromClient+=msg;
                            C.Notify(msgfromClient,LoginName);
                        }
                    }
                    // if(i==FileServer.Groups.size()) dout.writeUTF(groupName+" doesn't exist");                    
                }
                else if(command.equals("list_detail")){
                    String groupName=tokenedcommand.nextToken();
                    String outp="";
                    int i=0;
                    for(i=0;i<FileServer.Groups.size();i++){
                        if(FileServer.Groups.elementAt(i).name.equals(groupName)){
                            Group C = FileServer.Groups.elementAt(i);
                            Vector<String> outpl=C.ListUsers();
                            String out="";
                            for(int j=0;j<outpl.size();j++){
                                String name=outpl.elementAt(j);
                                outp+= "User: "+name+"\n";
                                File folder = new File("./"+name);
                                File[] listOfFiles = folder.listFiles();
                                if(listOfFiles.length!=0)
                                    outp+="Files:\n";
                                for (int k = 0; k < listOfFiles.length; k++) {
                                    if (listOfFiles[k].isFile()) 
                                    {
                                        outp+=listOfFiles[k].getName() + "\n";
                                    }
                                    else if (listOfFiles[k].isDirectory())
                                    {
                                        String subdir = listOfFiles[k].getName();
                                        File sbfolder = new File(folder+"/"+subdir);
                                        File[] sblistOfFiles = sbfolder.listFiles();
                                        for(int p=0;p<sblistOfFiles.length;p++)
                                            outp+=subdir+"/"+sblistOfFiles[p].getName()+"\n";
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if(i==FileServer.Groups.size()) outp = "Group " + groupName+" doesn't exist";
                    dout.writeUTF(outp);
                }
                else if(command.equals("get_file")){
                    String fl1 = tokenedcommand.nextToken();
                    String[] flSplit = fl1.split("/",3);
                    String groupname = flSplit[0],fileUser=flSplit[1],flpath=flSplit[2];
                    // System.out.println(flSplit[0]);
                    // System.out.println(flSplit[1]);
                    int i=0;
                    for(i=0;i<FileServer.Groups.size();i++){
                        Group C = FileServer.Groups.elementAt(i);
                        if(C.name.equals(groupname)){
                            if(C.ListUsers().indexOf(fileUser)==-1 || C.ListUsers().indexOf(LoginName)==-1){
                                dout.writeUTF("Either you or "+fileUser+" doesn't belong to group " + groupname);
                                break;
                            }
                            String fl = flSplit[1]+"/"+flSplit[2];
                            String[] justnamear = fl.split("/",0);
                            String justname = justnamear[justnamear.length-1];
                            File file = new File(fl);
                            FileInputStream fpin = new FileInputStream(file);
                            BufferedInputStream bpin = new BufferedInputStream(fpin);
                            long fileLength =  file.length(), current=0, start = System.nanoTime();
                            dout.writeUTF("GETFILE "+justname+" LENGTH " + fileLength);
                            // System.out.println("FILE "+justname+" LENGTH " + fileLength);
                            // System.out.println(fileLength);
                            while(current!=fileLength)
                            {
                                int size=1000;
                                if(fileLength - current >= size) current+=size;
                                else {
                                    size = (int)(fileLength-current);
                                    current=fileLength;
                                }
                                byte[] file_contents = new byte[size];
                                bpin.read(file_contents,0,size); dout.write(file_contents);
                                System.out.println("Sending file ..."+(current*100/fileLength)+"% complete");
                            }
                            System.out.println("File Sent");                       
                            break;
                        }
                    }
                    System.out.println("Check");
                    if(i==FileServer.Groups.size()) dout.writeUTF(groupname+" doesn't exist");

                }
                else
                {
                    dout.writeUTF("Unrecognised command");
                }
            }
            catch(Exception e) {
                e.printStackTrace(System.out) ; break;
            }
        }
    }
}

class Group {
    Vector<String> Members = new Vector<String>();
    String name;
    Group (String name,String member) {
        this.name = name;
        this.Members.add(member);
    }
    public Vector<String> ListUsers() {
        return this.Members;
    }
    public String Add(String memberAdd) {
        if(this.Members.contains(memberAdd)) return(memberAdd+" is already a part of "+this.name);
        if(!FileServer.LoginNames.contains(memberAdd)) return("The username "+memberAdd+" doesn't exist");
        this.Members.add(memberAdd);
        return(memberAdd+" added to group "+this.name);
    }
    public void Notify(String msg,String no_notif) {
        for(int i=0;i<this.Members.size();i++)
        {
            if(!this.Members.elementAt(i).equals(no_notif))
            {
                try {
                    Socket sendSoc = FileServer.ClientSockets.elementAt(FileServer.LoginNames.indexOf(this.Members.elementAt(i)));
                    DataOutputStream senddout = new DataOutputStream(sendSoc.getOutputStream());
                    senddout.writeUTF(msg);
                }
                catch(Exception e){ int ii=0;  }
            }
        }
    }
    public String Join (String member) {
        this.Members.add(member);
        return ("Joined Group "+this.name);
    }
    public String Leave (String member) {
        this.Members.remove(member);
        if(this.Members.isEmpty()) return ("DEL");
        else return("You left group "+this.name);
    }
}