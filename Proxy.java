import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Proxy {


    // NOC these 3 fields
    private String requestedHost;
    private String requestedMethod;
    private String requestedPath;
    private String requestedProtocol;
    private final int serverPort;
    private InetAddress hostIP ;
    private final String INTERNAL_SERVER_ERROR="HTTP/1.1 500 Internal Server Error\r\n\r\n";
  //  private             FILE ;    /* name of the file in URL, if you like */

    public static void main(String [] a) throws Exception
    { // NOC - do not change main()

        Proxy proxy = new Proxy(Integer.parseInt(a[0]));
        proxy.run();
    }

    Proxy(int port)
    {
        serverPort=port; // other init stuff ADC here
    }

    int parseInput(String buffer) throws Exception
    {
        Pattern p = Pattern.compile("^([A-Z]+)\\s+([a-z]*://)?+([^/\\s]+)(/[^\\s]*)?\\s+([A-Z0-9/.]+)\r\n");
        Matcher m = p.matcher(buffer);
        if (m.find())
        {
            requestedMethod = m.group(1);
            requestedHost = m.group(3);
            requestedPath=m.group(4);
            requestedProtocol = m.group(2);

        }
        else
        {
            System.out.println("Request ");
        }


        return 0;
    }

    // Note: dns() must set PREFERRED
    int dns(String host) // NOC - do not change this signature; X is whatever you want
    {
        try {
            InetAddress hostAddress = InetAddress.getByName(host);
            hostIP = hostAddress;
        }
        catch(UnknownHostException uhe)
        {
            uhe.printStackTrace();
            return -1;
        }
        return 0;
    }
    String bytesToSTring(byte[] clientReq,int reqLength)
    {
        char[] cbuff=new char[clientReq.length];
        for(int i =0;i<reqLength;i++)
            {
                cbuff[i]=(char)clientReq[i];
            }
        return new String(cbuff);
    }

    int http(Socket client,String req){
        Socket peer=new Socket();
        int bytesTransferred=0;
        int bytesRead;
        byte []  buffer = new byte[4096];
        InputStream peerInputStream = null;
        OutputStream peerOutputStream = null;
        OutputStream clientOutputStream = null;

        int count=0;
        try {
            peer.connect(new InetSocketAddress(hostIP,80));
            peerInputStream= peer.getInputStream();
            peerOutputStream= peer.getOutputStream();
            clientOutputStream=client.getOutputStream();
            peerOutputStream.write(req.getBytes());
            peerOutputStream.flush();

            while ((bytesRead=peerInputStream.read(buffer))!=-1)
            {
                clientOutputStream.write(buffer,0,bytesRead);
                bytesTransferred+=bytesRead;
             //   if(bytesRead<4096){
               //     break;
              //  }
            }
            System.out.println("bytesTransferred"+bytesTransferred);
        } catch (Exception se) {
            try {

                clientOutputStream.write(INTERNAL_SERVER_ERROR.getBytes());
            }
            catch(Exception e){
                e.printStackTrace();
            }
        } finally {
            try {

                peer.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return bytesTransferred;

    }
    void run() throws Exception
    {
        byte[] clientBytes=new byte[65536];
        String clientRequest = "";
        int bytesRead;
        int parseResponse;
        int dnsResponse;
        ServerSocket serverSocket = new ServerSocket();;
        Socket clientSocket = null;
        InputStream clientInputStream=null;
        OutputStream clientOutputStream=null;
        InetAddress localAddress = InetAddress.getByName("192.168.0.14");
        SocketAddress socketAddress=new InetSocketAddress(localAddress,serverPort);
        serverSocket.bind(socketAddress);

        while ( true ) {
            System.out.println("Waiting for client connection to " + localAddress);
            clientSocket=serverSocket.accept();
            System.out.println("Client Connected");
            clientInputStream=clientSocket.getInputStream();
            clientOutputStream = clientSocket.getOutputStream();
            try {


                bytesRead=clientInputStream.read(clientBytes);
                clientRequest=bytesToSTring(clientBytes,bytesRead);
                //System.out.println(clientRequest);
                parseResponse = parseInput(clientRequest);
                dnsResponse = dns(requestedHost);
              //  System.out.println("DNS complete");
                if (dnsResponse == 0) {
                  //  clientOutputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                    int bytesTransferred = http(clientSocket,clientRequest);
                    System.out.println(bytesTransferred);
                    System.out.println(hostIP);
                } else {
                    clientOutputStream.write("Lookup failed".getBytes());
                }

            }
            catch(Exception e)
            {
                try {
                    e.printStackTrace();
                    clientOutputStream.write((INTERNAL_SERVER_ERROR).getBytes());
                } catch (IOException ioException)
                {
                }
            }
            finally
            {
                try{
                    clientInputStream.close();
                    clientOutputStream.close();
                    clientSocket.close();

                }catch (Exception e)
                {
                }

            }
        }

    }


}
