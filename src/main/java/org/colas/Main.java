package org.colas;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Main {
    static File registroFile = new File("registros.json");
    static Cola<Reporte> cola = new Cola<>();
    static int contador;

    public static void main(String[] args) {
        contador = ajustarContador();

        if (!registroFile.exists())
            try {
                registroFile.createNewFile();
            } catch (IOException e) {
                System.out.println("¡No se pudo crear el archivo de registro! Saliendo...");
                return;
            }

        System.out.println("Proyecto: Cola de turnos para atender reportes.");

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("Operaciones: ");
                System.out.println("1) Agregar un reporte.");
                System.out.println("2) Atender el reporte más próximo.");
                System.out.println("3) Ver reportes ya atendidos.");
                System.out.println("Cualquier otro número) Salir.");

                System.out.print("> ");
                switch (ensureInt(sc)) {
                    case 1:
                        agregar(sc);
                        break;
                    case 2:
                        atender();
                        break;
                    case 3:
                        mostrarReportes();
                        break;
                    default:
                        return;
                }
            }
        }
    }

    static void agregar(Scanner sc) {
        if (cola.estaLleno()) {
            System.out.println("¡La cola está llena! No se pueden atender más reportes por el momento.");
            return;
        }

        Reporte reporte = new Reporte();

        reporte.setId(contador++);

        System.out.println("¿Cuál es tu nombre?");
        reporte.setNombre(sc.nextLine());

        System.out.println("¿A qué departamento irás?");
        for (int i = 0; i < Reporte.departamentos.length; i++)
            System.out.println((i + 1) + ") " + Reporte.departamentos[i]);

        System.out.print("> ");
        int entrada;
        do {
            entrada = ensureInt(sc) - 1;
            if (!(1 <= entrada && entrada < Reporte.departamentos.length))
                System.out.println("¡Valor inválido! Intenta de nuevo.");

        } while (!(1 <= entrada && entrada < Reporte.departamentos.length));

        reporte.setDepartamento(Reporte.departamentos[entrada]);

        System.out.println("¿Cuál es el motivo por el que viene?");
        reporte.setReporte(sc.nextLine());

        System.out.println("Escriba la referencia del ticket de su producto:");
        reporte.setTicket(sc.nextLine());

        reporte.setFecha(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));

        cola.agregar(reporte);
    }

    static void atender() {
        if (cola.estaVacio()) {
            System.out.println("¡La cola está vacía! No hay nada qué atender.");
            return;
        }

        Reporte reporte = cola.eliminar();
        System.out.println(
                "Se ha atendido el siguiente reporte con id:" + reporte.getId());
        guardarRegistro(reporte);
    }

    static void mostrarReportes() {
        ArrayList<Reporte> reportes = obtenerReportes();
        for (Reporte reporte : reportes) {
            System.out.println(reporte);
        }
    }

    static void guardarRegistro(Reporte reporte) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);

        ArrayList<Reporte> reportes = obtenerReportes();

        try (PrintWriter writer = new PrintWriter(registroFile)) {
            if (reportes == null)
                reportes = new ArrayList<>();

            reportes.add(reporte);
            mapper.writeValue(writer, reportes);
        } catch (IOException e) {
            System.out.println("No se pudo guardar: ¡No se encontró el archivo de registro!");
            return;
        }
    }

    static ArrayList<Reporte> obtenerReportes() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(registroFile, new TypeReference<ArrayList<Reporte>>() {
            });
        } catch (IOException e) {
            return null;
        }
    }

    static int ajustarContador() {
        ArrayList<Reporte> reportes = obtenerReportes();
        return reportes == null ? 0 : reportes.size();
    }

    static int ensureInt(Scanner sc) {
        int valor;

        try {
            valor = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("¡Valor dado inválido! Intenta de nuevo.");
            return ensureInt(sc);
        }

        return valor;
    }
}

class Cola<T> {
    public T[] arreglo;
    private int cabeza = 0;
    private int cola = 0;

    public Cola() {
        @SuppressWarnings("unchecked")
        T[] data = (T[]) new Object[3];
        this.arreglo = data;
    }

    public void agregar(T dato) {
        if (estaLleno()) {
            System.out.println("¡No se pudo añadir, la cola está llena!");
            return;
        }

        System.out.println("Se añadió a: " + dato);
        arreglo[cola % arreglo.length] = dato;
        cola++;
    }

    public T eliminar() {
        if (estaVacio()) {
            System.out.println("¡No se pudo eliminar, la cola está vacía!");
            return null;
        }

        T regreso = arreglo[cabeza % arreglo.length];
        cabeza++;

        System.out.println("Se quitó a: " + regreso);
        return regreso;
    }

    public boolean estaLleno() {
        return (cola % arreglo.length) == (cabeza % arreglo.length) && !estaVacio();
    }

    public boolean estaVacio() {
        return cabeza == cola;
    }
}

class Reporte {
    public static final String[] departamentos = {
            "Servicio al cliente",
            "Sala de conferencias",
            "Ventas",
            "Servicio técnico",
            "Secretaría",
            "Mercadotecnia",
            "Redes"
    };

    private int id;
    private String departamento;
    private String nombre;
    private String fecha;
    private String reporte;
    private String ticket;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getReporte() {
        return reporte;
    }

    public void setReporte(String reporte) {
        this.reporte = reporte;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    @Override
    public String toString() {
        return String.format(
                "Reporte: [\n" +
                        "\tID: %d\n" +
                        "\tNombre: %s\n" +
                        "\tDepartamento: %s\n" +
                        "\tReporte: {\n\t\t%s\n\t}\n" +
                        "\tFecha: %s\n" +
                        "\tTicket: %s\n]",
                id, nombre, departamento, reporte, fecha, ticket);
    }

}