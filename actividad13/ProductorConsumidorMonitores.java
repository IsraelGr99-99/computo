/**
 * BufferMonitores
 * Representa un buffer compartido protegido mediante monitores.
 * Se sincroniza usando synchronized, wait() y notifyAll().
 */
class BufferMonitores {
    private int[] items;   // Arreglo donde se guardan los elementos
    private int tamano;    // Capacidad máxima del buffer
    private int index;     // Posición del próximo elemento a insertar/sacar

    /**
     * Constructor
     * @param tamano Capacidad máxima del buffer
     */
    public BufferMonitores(int tamano) {
        this.tamano = tamano;
        this.items = new int[tamano];
        this.index = 0;
    }

    /**
     * Inserta un elemento en el buffer.
     * Si el buffer está lleno, el hilo espera hasta que haya espacio.
     * @param item Elemento a insertar
     */
    public synchronized void insertar(int item) {
        // Espera si el buffer está lleno
        while (index == tamano) {
            try {
                wait(); // Libera el monitor y espera a ser notificado
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Inserción del elemento
        items[index++] = item;
        System.out.println("Insertado: " + item);

        notifyAll(); // Despierta a hilos que puedan estar esperando
    }

    /**
     * Extrae un elemento del buffer.
     * Si el buffer está vacío, el hilo espera hasta que haya un elemento.
     * @return Elemento extraído
     */
    public synchronized int extraer() {
        // Espera si el buffer está vacío
        while (index == 0) {
            try {
                wait(); // Libera el monitor y espera a ser notificado
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Extracción del elemento
        int item = items[--index];
        System.out.println("Extraído: " + item);

        notifyAll(); // Despierta a hilos que puedan estar esperando
        return item;
    }
}

/**
 * ProductorMonitores
 * Hilo que genera elementos y los inserta en el buffer.
 */
class ProductorMonitores extends Thread {
    private BufferMonitores buffer;

    public ProductorMonitores(BufferMonitores buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            buffer.insertar(i); // Inserta elemento
            try {
                Thread.sleep((int)(Math.random() * 100)); // Simula tiempo de producción
            } catch (InterruptedException e) {}
        }
    }
}

/**
 * ConsumidorMonitores
 * Hilo que toma elementos del buffer y los procesa.
 */
class ConsumidorMonitores extends Thread {
    private BufferMonitores buffer;

    public ConsumidorMonitores(BufferMonitores buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            buffer.extraer(); // Extrae elemento
            try {
                Thread.sleep((int)(Math.random() * 100)); // Simula tiempo de consumo
            } catch (InterruptedException e) {}
        }
    }
}

/**
 * Clase principal para ejecutar la versión con monitores.
 */
public class ProductorConsumidorMonitores {
    public static void main(String[] args) {
        // Creamos un buffer con capacidad de 5 elementos
        BufferMonitores buffer = new BufferMonitores(5);

        // Creamos hilos productores y consumidores
        ProductorMonitores p1 = new ProductorMonitores(buffer);
        ProductorMonitores p2 = new ProductorMonitores(buffer);
        ConsumidorMonitores c1 = new ConsumidorMonitores(buffer);
        ConsumidorMonitores c2 = new ConsumidorMonitores(buffer);

        // Iniciamos todos los hilos
        p1.start();
        p2.start();
        c1.start();
        c2.start();
    }
}
