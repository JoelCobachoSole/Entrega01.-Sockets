import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Servidor {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java Servidor <puerto> <palabraClave>");
            return;
        }

        int puerto = Integer.parseInt(args[0]);
        String palabraClave = args[1];

        ServerSocket serverSocket = null;
        Socket socket = null;
        BufferedReader entrada = null;
        PrintWriter salida = null;
        Scanner teclado = null;

        try {
            System.out.print("Iniciando servidor...");
            serverSocket = new ServerSocket(puerto);
            System.out.println("OK");

            System.out.print("Esperando conexión del cliente...");
            socket = serverSocket.accept();
            System.out.println("OK");

            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            teclado = new Scanner(System.in);

            boolean conversacionActiva = true;

            while (conversacionActiva) {
                // Recibir mensaje del cliente
                System.out.print("Recibiendo mensaje del cliente...");
                String mensajeRecibido = entrada.readLine();
                System.out.println("OK");
                System.out.println("Cliente: " + mensajeRecibido);

                // Comprobar si el cliente usó su palabra clave
                if (mensajeRecibido == null || mensajeRecibido.equalsIgnoreCase(palabraClave)) {
                    System.out.println("El cliente ha cerrado la conversación.");
                    conversacionActiva = false;
                    break;
                }

                // El servidor escribe su respuesta
                System.out.print("Introduce un mensaje: ");
                String mensajeEnviar = teclado.nextLine();

                System.out.print("Enviando mensaje...");
                salida.println(mensajeEnviar);
                System.out.println("OK");

                // Comprobar si el servidor usó su palabra clave
                if (mensajeEnviar.equalsIgnoreCase(palabraClave)) {
                    System.out.println("Has cerrado la conversación.");
                    conversacionActiva = false;
                }
            }

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (teclado != null) {
                    System.out.print("Cerrando scanner...");
                    teclado.close();
                    System.out.println("OK");
                }
                if (salida != null) {
                    salida.close();
                }
                if (entrada != null) {
                    System.out.print("Cerrando flujo de entrada...");
                    entrada.close();
                    System.out.println("OK");
                }
                if (socket != null) {
                    System.out.print("Cerrando socket cliente...");
                    socket.close();
                    System.out.println("OK");
                }
                if (serverSocket != null) {
                    System.out.print("Cerrando servidor...");
                    serverSocket.close();
                    System.out.println("OK");
                }
            } catch (IOException e) {
                System.out.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
}