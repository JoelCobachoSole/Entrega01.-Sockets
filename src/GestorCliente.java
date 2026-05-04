import java.io.*;
import java.net.*;

public class GestorCliente implements Runnable {

    private final Socket socket;
    private final int idCliente;
    private BufferedReader entrada;
    private PrintWriter salida;
    private String palabraClaveCliente;
    private boolean activo = true;

    public GestorCliente(Socket socket, int idCliente) {
        this.socket = socket;
        this.idCliente = idCliente;
    }

    @Override
    public void run() {
        try {
            // Configuramos los flujos de entrada/salida sobre el socket del cliente
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            // El primer mensaje del cliente es su palabra clave de cierre
            palabraClaveCliente = entrada.readLine();
            System.out.println("Cliente " + idCliente + " conectado con palabra clave: \"" + palabraClaveCliente + "\"");
            System.out.println("Iniciando chat con cliente " + idCliente + "...");

            String mensajeRecibido;

            // Bucle principal: recibir mensajes del cliente
            while (activo && (mensajeRecibido = entrada.readLine()) != null) {

                // Si el cliente envía su propia palabra clave → cierra su chat
                if (mensajeRecibido.equalsIgnoreCase(palabraClaveCliente)) {
                    System.out.println("El cliente " + idCliente + " ha usado su palabra clave y cierra el chat.");
                    // Notificamos igualmente para mantener el log del chat
                    Servidor.mensajeRecibidoDeCliente(idCliente, mensajeRecibido);
                    cerrarCliente();
                    return;
                }

                // Notificamos al servidor del nuevo mensaje (lo añade a la cola FIFO)
                Servidor.mensajeRecibidoDeCliente(idCliente, mensajeRecibido);
            }

        } catch (IOException e) {
            if (activo) {
                System.out.println("Error con el cliente " + idCliente + ": " + e.getMessage());
            }
        } finally {
            cerrarCliente();
        }
    }

    // Enviar un mensaje al cliente
    public void enviarMensaje(String mensaje) {
        if (salida != null && activo) {
            salida.println(mensaje);
        }
    }

    // Cerrar la conexión con este cliente y liberar recursos
    public void cerrarCliente() {
        if (!activo) return;
        activo = false;

        try {
            if (salida != null) {
                salida.close();
            }
            if (entrada != null) {
                System.out.print("Cerrando flujo de entrada del cliente " + idCliente + "...");
                entrada.close();
                System.out.println("OK");
            }
            if (socket != null && !socket.isClosed()) {
                System.out.print("Cerrando socket del cliente " + idCliente + "...");
                socket.close();
                System.out.println("OK");
            }
        } catch (IOException e) {
            System.out.println("Error al cerrar recursos del cliente " + idCliente + ": " + e.getMessage());
        }

        // Eliminar del mapa global del servidor
        Servidor.eliminarCliente(idCliente);
    }

    public boolean estaActivo() {
        return activo;
    }

    public String getPalabraClaveCliente() {
        return palabraClaveCliente;
    }

    public int getIdCliente() {
        return idCliente;
    }
}
