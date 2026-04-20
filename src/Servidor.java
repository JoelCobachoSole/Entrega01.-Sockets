import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Servidor {
    public static void main(String[] args) {

        // Verificamos que se pasen exactamente 2 argumentos: puerto y palabra clave
        if (args.length < 2) {
            System.out.println("Uso: java Servidor <puerto> <palabraClave>");
            return;
        }

        // Parseamos el puerto (primer argumento) y guardamos la palabra clave (segundo argumento)
        int puerto = Integer.parseInt(args[0]);
        String palabraClave = args[1];

        // Declaramos los recursos fuera del try para poder cerrarlos en el finally
        ServerSocket serverSocket = null; // Socket del servidor (escucha conexiones entrantes)
        Socket socket = null;             // Socket del cliente (una vez conectado)
        BufferedReader entrada = null;    // Flujo de lectura de mensajes del cliente
        PrintWriter salida = null;        // Flujo de escritura de mensajes hacia el cliente
        Scanner teclado = null;           // Lector de teclado para que el servidor escriba respuestas

        try {
            // Creamos el ServerSocket en el puerto indicado y esperamos conexiones
            System.out.print("Iniciando servidor...");
            serverSocket = new ServerSocket(puerto);
            System.out.println("OK");

            // accept() bloquea la ejecución hasta que un cliente se conecte
            System.out.print("Esperando conexión del cliente...");
            socket = serverSocket.accept();
            System.out.println("OK");

            // Configuramos los flujos de entrada/salida sobre el socket del cliente
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true); // true = autoFlush
            teclado = new Scanner(System.in);

            boolean conversacionActiva = true;

            // Bucle principal de la conversación: continúa hasta que alguien use la palabra clave
            while (conversacionActiva) {

                // --- RECEPCIÓN ---
                // readLine() bloquea hasta que el cliente envíe una línea de texto
                System.out.print("Recibiendo mensaje del cliente...");
                String mensajeRecibido = entrada.readLine();
                System.out.println("OK");
                System.out.println("Cliente: " + mensajeRecibido);

                // Si el cliente envía null (desconexión inesperada) o la palabra clave → cerramos
                if (mensajeRecibido == null || mensajeRecibido.equalsIgnoreCase(palabraClave)) {
                    System.out.println("El cliente ha cerrado la conversación.");
                    conversacionActiva = false;
                    break;
                }

                // --- RESPUESTA ---
                // El servidor escribe su respuesta por teclado
                System.out.print("Introduce un mensaje: ");
                String mensajeEnviar = teclado.nextLine();

                // Enviamos la respuesta al cliente a través del PrintWriter
                System.out.print("Enviando mensaje...");
                salida.println(mensajeEnviar);
                System.out.println("OK");

                // Si el servidor escribe la palabra clave, también se cierra la conversación
                if (mensajeEnviar.equalsIgnoreCase(palabraClave)) {
                    System.out.println("Has cerrado la conversación.");
                    conversacionActiva = false;
                }
            }

        } catch (IOException e) {
            // Capturamos cualquier error de red o de E/S
            System.out.println("Error: " + e.getMessage());

        } finally {
            // El bloque finally garantiza que SIEMPRE se liberan los recursos,
            // tanto si hubo error como si la conversación terminó normalmente.
            // El orden de cierre es inverso al de apertura.
            try {
                if (teclado != null) {
                    System.out.print("Cerrando scanner...");
                    teclado.close();
                    System.out.println("OK");
                }
                if (salida != null) {
                    salida.close(); // Cierra el flujo de salida hacia el cliente
                }
                if (entrada != null) {
                    System.out.print("Cerrando flujo de entrada...");
                    entrada.close();
                    System.out.println("OK");
                }
                if (socket != null) {
                    System.out.print("Cerrando socket cliente...");
                    socket.close(); // Cierra la conexión con el cliente
                    System.out.println("OK");
                }
                if (serverSocket != null) {
                    System.out.print("Cerrando servidor...");
                    serverSocket.close(); // Cierra el servidor; ya no acepta conexiones
                    System.out.println("OK");
                }
            } catch (IOException e) {
                System.out.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
}