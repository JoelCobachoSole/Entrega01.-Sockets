import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {

        // Verificamos que se pasen exactamente 3 argumentos: host, puerto y palabraClave
        if (args.length < 3) {
            System.out.println("Uso: java Cliente <host> <puerto> <palabraClave>");
            return;
        }

        String host = args[0];
        int puerto = Integer.parseInt(args[1]);
        String palabraClave = args[2];

        // Declaramos los recursos fuera del try para poder cerrarlos en el finally
        Socket socket = null;          // Socket de conexión con el servidor
        BufferedReader entrada = null; // Flujo de lectura de mensajes del servidor
        PrintWriter salida = null;     // Flujo de escritura de mensajes hacia el servidor
        Scanner teclado = null;        // Lector de teclado para que el cliente escriba mensajes

        try {
            // Creamos el socket y nos conectamos al servidor (host:puerto)
            System.out.print("Iniciando cliente...");
            socket = new Socket(host, puerto);
            System.out.println("OK");

            // Configuramos los flujos de entrada/salida sobre el socket
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true); // true = autoFlush
            teclado = new Scanner(System.in);

            // El primer mensaje que enviamos es nuestra palabra clave (para que el servidor la conozca)
            salida.println(palabraClave);
            System.out.println("Iniciando chat...");

            boolean conversacionActiva = true;

            // Bucle principal: el cliente SIEMPRE escribe primero, luego espera respuesta
            while (conversacionActiva) {

                // --- ENVÍO ---
                System.out.print("Introduce un mensaje: ");
                String mensajeEnviar = teclado.nextLine();

                System.out.print("Enviando mensaje...");
                salida.println(mensajeEnviar);
                System.out.println("OK");

                // Si el cliente escribe su palabra clave → cierra sin esperar respuesta
                if (mensajeEnviar.equalsIgnoreCase(palabraClave)) {
                    System.out.println("Has cerrado la conversación.");
                    conversacionActiva = false;
                    break;
                }

                // --- RECEPCIÓN ---
                System.out.print("Recibiendo mensaje del servidor...");
                String mensajeRecibido = entrada.readLine();
                System.out.println("OK");

                // Si el servidor cierra la conexión inesperadamente
                if (mensajeRecibido == null) {
                    System.out.println("El servidor ha cerrado la conexión.");
                    conversacionActiva = false;
                    break;
                }

                System.out.println("Servidor: " + mensajeRecibido);

                // Si el servidor envía nuestra palabra clave → él está cerrando este chat
                if (mensajeRecibido.equalsIgnoreCase(palabraClave)) {
                    System.out.println("El servidor ha cerrado la conversación.");
                    conversacionActiva = false;
                }
            }

        } catch (IOException e) {
            // Capturamos errores de red: servidor no disponible, conexión cortada, etc.
            System.out.println("Error: " + e.getMessage());

        } finally {
            // El bloque finally garantiza que SIEMPRE se liberan los recursos.
            // El orden de cierre es inverso al de apertura.
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