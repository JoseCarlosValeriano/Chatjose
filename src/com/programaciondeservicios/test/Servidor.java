package com.programaciondeservicios.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class Servidor extends Thread {

    private static Vector<PrintStream> CLIENTES;
    private Socket conexion;
    private String nombreCliente;
    private static List<String> lista_nombresClientes = new ArrayList<>();
    private static ArrayList<String> mensajes = new ArrayList<>();

    public Servidor(Socket socket) {
        this.conexion = socket;
    }

    //Método que comprueba si el usuario existe o no
    public boolean lista_nombre(String newName) {
        for (int i = 0; i < lista_nombresClientes.size(); i++) {
            //Comprueba si el usuario es igual al introducido
            if (lista_nombresClientes.get(i).equals(newName))
                return true;
        }
        //Si no existe, se añade a la arraylist
        lista_nombresClientes.add(newName);
        return false;
    }

    //Clase para remover el usuario cuando se desconecte
    public void remove(String usuarioDesconectado) {
        for (int i = 0; i < lista_nombresClientes.size(); i++) {
            if (lista_nombresClientes.get(i).equals(usuarioDesconectado))
                lista_nombresClientes.remove(usuarioDesconectado);
        }
    }

    public static void main(String args[]) {

        //Clase para almacenar objetos
        CLIENTES = new Vector<PrintStream>();
        try {
            // Arrancamos el servidor
            ServerSocket server = new ServerSocket(8080);
            System.out.println("ServidorSocket funcionando en el puerto 8080");
            while (true) {
                //Por cada persona que se conecte, comezaremos un nuevo hilo
                Socket conexion = server.accept();
                Thread t = new Servidor(conexion);
                t.start();

            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    public void run() {
        try {
            //Inicializamos un Buffer para entrada de datos
            BufferedReader entrada =
                    new BufferedReader(new InputStreamReader(this.conexion.getInputStream()));
            // Inicializamos el PrinStream para salida de datos a los clientes.
            PrintStream salida = new PrintStream(this.conexion.getOutputStream());
            this.nombreCliente = entrada.readLine();
            //Comprueba el usuario, en aso de contrario, se conecta
            if (lista_nombre(this.nombreCliente)) {
                salida.println("Este nombre ya existe! Conectate con otro nombre.");
                CLIENTES.add(salida);
                this.conexion.close();
                return;
            } else {
                System.out.println(this.nombreCliente + " : Conectado al servidor!");
            }
            if (this.nombreCliente == null) {
                return;
            }
            // Aquí añadimos el nombre del usuario a la arraylist
            CLIENTES.add(salida);

            // Envia el historial de mensajes, si los hay
            if (!mensajes.isEmpty()) {
                salida.println("Estos son los mensajes anteriores del chat:");

                for (String message : mensajes) {
                    salida.println(message);
                }
            }

            // Tratamos de leer los mensajes de los clientes
            String msg = entrada.readLine();
            // Si el mensaje que se envió no es en blanco, enviará a todos mediante un método el mensaje a todos
            //los clientes y añadira al historial de mensajes lo que hayamos escrito.
            while (msg != null && !(msg.trim().equals(""))) {
                sendToAll(salida, " escribió: ", msg);
                msg = entrada.readLine();
                mensajes.add(this.nombreCliente + ":" + msg);
            }
            //En caso de acabar el bucle, el usuario abandonará la sala del chat
            //Removiendo el cliente en el array y cerrará la conexión...
            System.out.println(this.nombreCliente + " ha salido del chat!");
            sendToAll(salida, " salió", " del chat del servidor!!");
            remove(this.nombreCliente);
            CLIENTES.remove(salida);
            this.conexion.close();
        } catch (IOException e) {
            System.out.println("Fallo en la conexión0... .. ." + " IOException: " + e);
        }
    }

    // Método que envia el mensaje a todos los usuarios
    public void sendToAll(PrintStream salidaPrint, String salida, String msg) throws IOException {
        //Enumera a todos los elementos que tenemos de clientes conectados
        Enumeration<PrintStream> e = CLIENTES.elements();
        while (e.hasMoreElements()) {
            // Con el printstream y el mensaje, enviaremos el mensaje a todos los usuarios mientras encuentre elementos
            // en el enumeration
            PrintStream chat = e.nextElement();
            if (chat != salidaPrint) {
                // Mientras el chat no esté vacio enviará por el PrintSream el mensaje.
                chat.println(this.nombreCliente + salida + msg);
            }
        }
    }
}