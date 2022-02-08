package com.programaciondeservicios.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.time.LocalDateTime;

public class ClienteSocket extends Thread {

    LocalDateTime locaDate = LocalDateTime.now();
    int hours  = locaDate.getHour();
    int minutes = locaDate.getMinute();
    int seconds = locaDate.getSecond();

    private final Socket conexion;

    public ClienteSocket(Socket socket) {
        this.conexion = socket;
    }
    public static void main(String[] args)
    {
        try {
            // Creamos la conexión
            Socket socket = new Socket("localhost", 8080);
            // Abrimos la conexión del print y buffer para leer y escribir respectivamente
            PrintStream salidaDatos = new PrintStream(socket.getOutputStream());
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
            // Pedimos nombre de usuario
            System.out.print("Escribe tu numero de usuario: ");
            String usuario = teclado.readLine();

            //Enviamos el usuario al servidor
            salidaDatos.println(usuario);

            // Arrancamos el hilo donde se ejecutará el hilo
            Thread thread = new ClienteSocket(socket);
            thread.start();

            String msg;
            while (true)
            {
                // Pedimos el mensaje, en caso de ser "Bye", se cerrará la conexión.
                System.out.print("Mensaje > ");
                msg = teclado.readLine();

                if(msg.isEmpty()){
                    System.out.println("Introduzca de nuevo el mensaje...");
                    System.out.print("Mensaje > ");
                    msg = teclado.readLine();
                }

                if (msg.equals("bye")) {
                    System.out.println("GoodBye!");
                    System.exit(0);
                }
                salidaDatos.println(msg);
            }
        } catch (IOException e) {
            System.out.println("Ha fallado la conexión... " + " IOException: " + e);
        }
    }

    // Este método lo usaremos explícitamente repetido al anterior, pero para poder enviar un mensaje de respuesta
    // al usuario que nos haya enviado un mensaje, puede ser cualquiera.
    public void run()
    {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(this.conexion.getInputStream()));
            String msg;
            while (true)
            {
                msg = entrada.readLine();
                System.out.println("-----------");
                System.out.println("[" + hours  + ":"+ minutes +":"+seconds + "] " + msg);
            }
        } catch (IOException e) {
            System.out.println("Ha ocurrido algún error... " +
                    " IOException: " + e);
        }
    }
}