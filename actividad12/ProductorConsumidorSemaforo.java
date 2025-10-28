import java.util.concurrent.Semaphore;

// Clase que representa el búfer compartido entre productores y consumidores
class BufferSem {
    private int[] items;           // Arreglo para almacenar los elementos
    private int tamano;            // Tamaño máximo del búfer
    private int indexInsert = 0;   // Posición de inserción (cuántos elementos ya insertados)

    // Semáforos
    private Semaphore mutex;       // Controla acceso exclusivo al búfer (1 permiso)
    private Semaphore espacios;    // Cuenta espacios vacíos en el búfer
    private Semaphore elementos;   // Cuenta cuántos elementos hay para consumir

    // Constructor
    public BufferSem(int tamano) {
        this.tamano = tamano;
        items = new int[tamano];
        indexInsert = 0;

        mutex = new Semaphore(1);         // Solo un hilo puede estar en la sección crítica
        espacios = new Semaphore(tamano);// Al inicio, todos los espacios están vacíos
        elementos = new Semaphore(0);     // Al inicio no hay elementos para consumir
    }

    // Método para que un productor inserte un valor
    public void insertar(int item) throws InterruptedException {
        espacios.acquire();  // Espera hasta que haya espacio libre
        mutex.acquire();     // Entra en la sección crítica (ningún otro hilo puede modificar aún)

        // Sección crítica: insertar el elemento
        items[indexInsert++] = item;
        System.out.println("Productor insertó: " + item);

        mutex.release();     // Sale de la sección crítica
        elementos.release(); // Señala que ahora hay un nuevo elemento para consumir
    }

    // Método para que un consumidor extraiga un valor
    public int extraer() throws InterruptedException {
        elementos.acquire(); // Espera hasta que haya al menos un elemento
        mutex.acquire();     // Entra en sección crítica

        // Sección crítica: extraer el elemento
        int valor = items[--indexInsert];
        System.out.println("Consumidor extrajo: " + valor);

        mutex.release();     // Sale de sección crítica
        espacios.release();  // Señala que ahora hay un espacio libre en el búfer

        return valor;
    }
}

// Clase que representa un productor (hilo)
class ProductorSem extends Thread {
    private BufferSem buffer;

    public ProductorSem(BufferSem buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                buffer.insertar(i);
                Thread.sleep((int)(Math.random() * 100));
            }
        } catch (InterruptedException e) {
            System.err.println("Productor interrumpido");
        }
    }
}

// Clase que representa un consumidor (hilo)
class ConsumidorSem extends Thread {
    private BufferSem buffer;

    public ConsumidorSem(BufferSem buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                buffer.extraer();
                Thread.sleep((int)(Math.random() * 100));
            }
        } catch (InterruptedException e) {
            System.err.println("Consumidor interrumpido");
        }
    }
}

// Clase principal que arranca el programa
public class ProductorConsumidorConSemaforos {
    public static void main(String[] args) {
        BufferSem buffer = new BufferSem(5);           // Crea un búfer con capacidad para 5 elementos
        ProductorSem p1 = new ProductorSem(buffer);    // Crea un productor
        ProductorSem p2 = new ProductorSem(buffer);    // Crea otro productor
        ConsumidorSem c1 = new ConsumidorSem(buffer);  // Crea un consumidor
        ConsumidorSem c2 = new ConsumidorSem(buffer);  // Crea otro consumidor

        p1.start();   // Inicia el primer productor
        p2.start();   // Inicia el segundo productor
        c1.start();   // Inicia el primer consumidor
        c2.start();   // Inicia el segundo consumidor
    }
}
