import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {

    private static final Map<Integer, GestorCliente> clientes = new LinkedHashMap<>();
    private static final Queue<Integer> colaRespuesta = new LinkedList<>();
    static final Object lock = new Object();

    static String palabraClaveServidor;
    static boolean servidorActivo = true;
    static boolean tuvoAlgunCliente = false;
    static int maxClientes;
    static int contadorIds = 0;

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Uso: java Servidor <puerto> <maxClientes> <palabraClave>");
            return;
        }

        int puerto = Integer.parseInt(args[0]);
        maxClientes = Integer.parseInt(args[1]);
        palabraClaveServidor = args[2];

        ServerSocket serverSocket = null;

        try {
            System.out.print("Iniciando servidor...");
            serverSocket = new ServerSocket(puerto);
            System.out.println("OK");
            System.out.println("Servidor de chat escuchando en el puerto " + puerto);
            System.out.println("Palabra clave del servidor: \"" + palabraClaveServidor + "\"");

            final ServerSocket ss = serverSocket;
            Thread hiloTeclado = new Thread(() -> {
                Scanner teclado = new Scanner(System.in);
                while (servidorActivo) {

                    // Esperamos a que haya algún cliente en la cola antes de pedir input
                    synchronized (lock) {
                        if (colaRespuesta.isEmpty()) {
                            // No hay nadie esperando, no mostramos prompt todavía
                        } else {
                            System.out.print("Introduce un mensaje: ");
                        }
                    }

                    if (!teclado.hasNextLine()) break;
                    String mensajeEnviar = teclado.nextLine();

                    synchronized (lock) {
                        // Si el servidor usa su propia palabra clave -> cerrar todo
                        if (mensajeEnviar.equalsIgnoreCase(palabraClaveServidor)) {
                            System.out.println("Has cerrado el servidor.");
                            cerrarTodosLosClientes();
                            servidorActivo = false;
                            try { ss.close(); } catch (IOException ignored) {}
                            System.out.print("Cerrando servidor...");
                            System.out.println("OK");
                            teclado.close();
                            return;
                        }

                        Integer idDestino = colaRespuesta.poll();
                        if (idDestino == null) {
                            System.out.println("[Sin clientes esperando respuesta]");
                            continue;
                        }

                        GestorCliente gestor = clientes.get(idDestino);
                        if (gestor == null || !gestor.estaActivo()) {
                            System.out.println("[Cliente " + idDestino + " ya no esta conectado]");
                            continue;
                        }

                        if (mensajeEnviar.equalsIgnoreCase(gestor.getPalabraClaveCliente())) {
                            System.out.print("Enviando mensaje al cliente " + idDestino + "...");
                            gestor.enviarMensaje(mensajeEnviar);
                            System.out.println("OK");
                            System.out.println("Has cerrado la conversacion con el cliente " + idDestino + ".");
                            gestor.cerrarCliente();
                        } else {
                            System.out.print("Enviando mensaje al cliente " + idDestino + "...");
                            gestor.enviarMensaje(mensajeEnviar);
                            System.out.println("OK");
                            System.out.println("Servidor -> Cliente " + idDestino + ": " + mensajeEnviar);
                        }
                    }
                }
                teclado.close();
            });
            hiloTeclado.setDaemon(true);
            hiloTeclado.start();

            serverSocket.setSoTimeout(500);
            while (servidorActivo) {
                Socket socketCliente;
                try {
                    socketCliente = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    synchronized (lock) {
                        if (tuvoAlgunCliente && clientes.isEmpty() && servidorActivo) {
                            System.out.println("No quedan clientes conectados. Cerrando servidor.");
                            servidorActivo = false;
                        }
                    }
                    continue;
                }

                synchronized (lock) {
                    if (clientes.size() >= maxClientes) {
                        System.out.println("Maximo de clientes alcanzado. Rechazando conexion.");
                        try { socketCliente.close(); } catch (IOException ignored) {}
                        continue;
                    }

                    contadorIds++;
                    int idCliente = contadorIds;
                    System.out.print("Aceptando conexion del cliente " + idCliente + "...");
                    tuvoAlgunCliente = true;

                    GestorCliente gestor = new GestorCliente(socketCliente, idCliente);
                    clientes.put(idCliente, gestor);
                    Thread hiloCliente = new Thread(gestor);
                    hiloCliente.start();
                    System.out.println("OK");
                }
            }

        } catch (IOException e) {
            if (servidorActivo) {
                System.out.println("Error: " + e.getMessage());
            }
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    System.out.print("Cerrando servidor...");
                    serverSocket.close();
                    System.out.println("OK");
                }
            } catch (IOException e) {
                System.out.println("Error al cerrar el servidor: " + e.getMessage());
            }
        }
    }

    static void mensajeRecibidoDeCliente(int idCliente, String mensaje) {
        synchronized (lock) {
            System.out.println("Cliente " + idCliente + ": " + mensaje);
            colaRespuesta.add(idCliente);
            System.out.print("Introduce un mensaje: ");
        }
    }

    static void eliminarCliente(int idCliente) {
        synchronized (lock) {
            clientes.remove(idCliente);
            colaRespuesta.removeIf(id -> id == idCliente);
            if (tuvoAlgunCliente && clientes.isEmpty() && servidorActivo) {
                System.out.println("No quedan clientes conectados. Cerrando servidor.");
                servidorActivo = false;
            }
        }
    }

    private static void cerrarTodosLosClientes() {
        List<GestorCliente> copia = new ArrayList<>(clientes.values());
        clientes.clear();
        colaRespuesta.clear();
        for (GestorCliente gestor : copia) {
            gestor.cerrarCliente();
        }
    }
}