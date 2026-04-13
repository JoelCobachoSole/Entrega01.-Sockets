import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: java Cliente <host> <puerto> <palabraClave>");
            return;
        }

        String host = args[0];
        int puerto = Integer.parseInt(args[1]);
        String palabraClave = args[2];

        Socket socket = null;
        BufferedReader entrada = null;
        PrintWriter salida = null;
        Scanner teclado = null;

        try {
            System.out.print("Iniciando cliente...");
            socket = new Socket(host, puerto);
            System.out.println("OK");

            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            teclado = new Scanner(System.in);

            boolean conversacionActiva = true;

            while (conversacionActiva) {
                // El cliente envía el primer mensaje
                System.out.print("Introduce un mensaje: ");
                String mensajeEnviar = teclado.nextLine();

                System.out.print("Enviando mensaje...");
                salida.println(mensajeEnviar);
                System.out.println("OK");

                // Comprobar si el cliente usó su palabra clave
                if (mensajeEnviar.equalsIgnoreCase(palabraClave)) {
                    System.out.println("Has cerrado la conversación.");
                    conversacionActiva = false;
                    break;
                }

                // Recibir respuesta del servidor
                System.out.print("Recibiendo mensaje del servidor...");
                String mensajeRecibido = entrada.readLine();
                System.out.println("OK");
                System.out.println("Servidor: " + mensajeRecibido);

                // Comprobar si el servidor usó su palabra clave
                if (mensajeRecibido == null || mensajeRecibido.equalsIgnoreCase(palabraClave)) {
                    System.out.println("El servidor ha cerrado la conversación.");
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
                    System.out.print("Cerrando cliente...");
                    socket.close();
                    System.out.println("OK");
                }
            } catch (IOException e) {
                System.out.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
}
