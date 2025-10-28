import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.Random;

/**
 * Clase RecursoCompartido
 * Representa un recurso compartido que puede ser leído por múltiples lectores
 * simultáneamente o escrito por un único escritor.
 */
class RecursoCompartido {
    private int dato; // Dato simulado que se leerá/escribirá

    // Lock de lectura/escritura
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Lock lectura = rwLock.readLock();   // Bloquea para lectura
    private Lock escritura = rwLock.writeLock(); // Bloquea para escritura

    /**
     * Método para leer el dato del recurso.
     * Puede ser llamado por múltiples lectores simultáneamente.
     * @param lectorId Identificador del lector
     */
    public void leer(int lectorId) {
        lectura.lock(); // Adquiere el lock de lectura
        try {
            System.out.println("Lector " + lectorId + " está leyendo: " + dato);
            Thread.sleep(new Random().nextInt(100)); // Simula tiempo de lectura
            System.out.println("Lector " + lectorId + " terminó de leer");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lectura.unlock(); // Libera el lock de lectura
        }
    }

    /**
     * Método para escribir un nuevo valor en el recurso.
     * Solo un escritor puede escribir a la vez.
     * @param escritorId Identificador del escritor
     * @param valor Valor a escribir
     */
    public void escribir(int escritorId, int valor) {
        escritura.lock(); // Adquiere el lock de escritura
        try {
            System.out.println("Escritor " + escritorId + " está escribiendo: " + valor);
            Thread.sleep(new Random().nextInt(100)); // Simula tiempo de escritura
            dato = valor; // Actualiza el dato
            System.out.println("Escritor " + escritorId + " terminó de escribir");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            escritura.unlock(); // Libera el lock de escritura
        }
    }
}

/**
 * Clase Lector
 * Hilo que representa un lector que lee el recurso compartido.
 */
class Lector extends Thread {
    private RecursoCompartido recurso;
    private int id;

    public Lector(RecursoCompartido recurso, int id) {
        this.recurso = recurso;
        this.id = id;
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            recurso.leer(id);
            try {
                Thread.sleep(new Random().nextInt(200)); // Espera antes de la siguiente lectura
            } catch (InterruptedException e) {}
        }
    }
}

/**
 * Clase Escritor
 * Hilo que representa un escritor que escribe en el recurso compartido.
 */
class Escritor extends Thread {
    private RecursoCompartido recurso;
    private int id;
    private Random rand = new Random();

    public Escritor(RecursoCompartido recurso, int id) {
        this.recurso = recurso;
        this.id = id;
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            int valor = rand.nextInt(100); // Valor aleatorio a escribir
            recurso.escribir(id, valor);
            try {
                Thread.sleep(new Random().nextInt(300)); // Espera antes de la siguiente escritura
            } catch (InterruptedException e) {}
        }
    }
}

/**
 * Clase principal
 * Crea múltiples lectores y escritores y los ejecuta simultáneamente.
 */
public class LectoresEscritores {
    public static void main(String[] args) {
        RecursoCompartido recurso = new RecursoCompartido();

        // Crear lectores y escritores
        Lector lector1 = new Lector(recurso, 1);
        Lector lector2 = new Lector(recurso, 2);
        Escritor escritor1 = new Escritor(recurso, 1);
        Escritor escritor2 = new Escritor(recurso, 2);

        // Iniciar hilos
        lector1.start();
        lector2.start();
        escritor1.start();
        escritor2.start();
    }
}
