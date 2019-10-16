import java.io.* ;
import java.net.* ;
import java.util.* ;
import java.awt.* ;

public class Client {
    public static String ip="127.0.0.1";
    public static int port = 5551;
    public static DatagramSocket clientSocUDP;
    public static void main(String args[])   {
        try
        {
            Socket clientSoc;
            String LoginName;
            clientSocUDP = new DatagramSocket();
            clientSoc = new Socket(ip,5555) ;
            System.out.println("Connected to Server at localhost Port-5555(TCP)");
            DataInputStream din = new DataInputStream(clientSoc.getInputStream());
            DataOutputStream dout = new DataOutputStream(clientSoc.getOutputStream());
            String a="initial_string";
            byte[] file_contents = new byte[1000]; 
            file_contents = a.getBytes();
            DatagramPacket initial = new DatagramPacket(file_contents,file_contents.length,InetAddress.getByName(ip),port);
            clientSocUDP.send(initial);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String inputLine=null;
            while(true)
            {
                try
                {
                    inputLine=bufferedReader.readLine();
                    dout.writeUTF(inputLine);
                    if(inputLine.equals("LOGOUT"))
                    {
                        clientSoc.close(); 
                        din.close(); dout.close();
                        System.out.println("Logged Out");
                        System.exit(0);
                    }
                    StringTokenizer tokenedcommand = new StringTokenizer(inputLine);
                    String comm,fl,typ;
                    comm = tokenedcommand.nextToken();
                    if(comm.equals("create_user")){
                        if(tokenedcommand.hasMoreTokens()){
                            fl=tokenedcommand.nextToken();
                            LoginName=fl;
                            // dout.writeUTF(fl);
                            new Thread(new MessageReciever (fl,din)).start();

                        }
                        else{
                            System.out.println("Error! Username not given");
                        }
                    }
                    else if(comm.equals("upload")){
                        fl = tokenedcommand.nextToken();
                        System.out.println(fl);
                        File file = new File(fl);
                        long fileLength =  file.length(), current=0;
                        String togo = "LENGTH "+fileLength;
                        dout.writeUTF(togo);
                        FileInputStream fpin = new FileInputStream(file);
                        BufferedInputStream bpin = new BufferedInputStream(fpin);
                        System.out.println("Sending file...");
                        while(true)
                        {
                            if(current==fileLength) break;
                            int size=1000;
                            if(size <= fileLength - current){
                                current = current+size;
                            }
                            else {
                                size = (int)(fileLength-current);
                                current = fileLength;
                            }
                            file_contents = new byte[size];
                            bpin.read(file_contents,0,size); dout.write(file_contents);
                        }
                        System.out.println("File Sent Successfully");                       
                    }
                    else if(comm.equals("upload_udp")){
                        fl = tokenedcommand.nextToken();
                        int size=1024;
                        File file = new File(fl);
                        long fileLength = file.length(), current =0;
                        String togo = "LENGTH "+fileLength;
                        dout.writeUTF(togo);
                        FileInputStream fpin = new FileInputStream(file);
                        BufferedInputStream bpin = new BufferedInputStream(fpin);
                        System.out.println("Sending file...");
                        while(true)
                        {
                            if(current == fileLength) break;
                            if(size <= fileLength - current){
                                current = current + size;
                            }
                            else {
                                size = (int)(fileLength-current);
                                current=fileLength;
                            }
                            file_contents = new byte[size];
                            bpin.read(file_contents,0,size);
                            DatagramPacket sendPacket = new DatagramPacket(file_contents,size,InetAddress.getByName(ip),port);
                            clientSocUDP.send(sendPacket);
                        }
                        System.out.println("File Sent Successfully");
                    }
                } catch(Exception e){System.out.println(e);break;}
            }
        }
        catch(Exception e) {System.out.println(e);System.exit(0);}
    }
}

class MessageReciever implements Runnable {
    private String LoginName;
    private DataInputStream server; 
    public MessageReciever(String LoginName,DataInputStream server) {
        this.LoginName = LoginName;
        this.server = server; 
    }
    @Override
    public void run() {
        String inputLine=null;
        while(true)
        {
            try {
                inputLine=server.readUTF();
                StringTokenizer st = new StringTokenizer(inputLine);
                if(!st.nextToken().equals("GETFILE")){
                    System.out.println(inputLine);
                }
                else
                {
                    System.out.println(inputLine);
                    String fileName=st.nextToken();
                    st.nextToken(); int fileLength = Integer.parseInt(st.nextToken());
                    System.out.println("Recieving file...");
                    byte[] file_contents = new byte[1000];
                    FileOutputStream fpout = new FileOutputStream(fileName);
                    BufferedOutputStream bpout = new BufferedOutputStream(fpout);
                    DatagramPacket receivePacket;
                    int bytesRead=0,size=1000;
                    if(size>fileLength){
                        size=fileLength;
                    }
                    while((bytesRead=server.read(file_contents,0,size))!=-1 && fileLength>0)
                    {
                        bpout.write(file_contents,0,size);
                        fileLength = fileLength - size; 
                        if(fileLength<size){
                            size=fileLength;
                        }
                    }
                    bpout.flush();
                    System.out.println("File Recieved");
                }
            }
            catch(Exception e) {e.printStackTrace(System.out); break;}
        }
    }
}
