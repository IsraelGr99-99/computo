// Clase que representa un "almacén" o espacio compartido donde los productores
// colocan elementos y los consumidores los retiran.
class Buffer {
    private int[] items;   // Arreglo donde se guardan los elementos (el "almacén")
    private int tamano;    // Tamaño máximo del buffer (cuántos elementos puede guardar)
    private int index;     // Indica cuántos elementos hay actualmente (y la posición libre)

    // Constructor: se ejecuta al crear un nuevo Buffer
    public Buffer(int tamano) {
        this.tamano = tamano;     // Asigna el tamaño máximo
        items = new int[tamano];  // Crea un arreglo con esa cantidad de espacios
        index = 0;                // Al inicio está vacío (por eso index = 0)
    }

    // Método para insertar (agregar) un nuevo elemento al buffer
    public void insertar(int item) {
        // Si aún hay espacio disponible...
        if (index < tamano) {
            // Guarda el nuevo elemento y avanza el índice
            items[index++] = item;
            System.out.println("Insertado: " + item);
        }
        // Si ya está lleno, simplemente no hace nada (no hay control de espera)
    }

    // Método para extraer (sacar) un elemento del buffer
    public int extraer() {
        // Si hay al menos un elemento almacenado...
        if (index > 0) {
            // Retrocede el índice y obtiene el último elemento insertado
            int item = items[--index];
            System.out.println("Extraído: " + item);
            return item;
        }
        // Si el buffer está vacío, devuelve -1 (indicando que no había nada)
        return -1;
    }
}

// ------------------------------------------------------------
// Clase que representa un "productor", es decir, un hilo (thread)
// que se encarga de crear o generar elementos y colocarlos en el buffer
class Productor extends Thread {
    private Buffer buffer; // Cada productor tiene acceso al mismo buffer

    // El constructor recibe el buffer compartido
    public Productor(Buffer buffer) {
        this.buffer = buffer;
    }

    // Este método se ejecuta automáticamente cuando se llama .start()
    public void run() {
        // El productor intentará insertar 10 elementos en el buffer
        for (int i = 0; i < 10; i++) {
            buffer.insertar(i);  // Inserta un número (por ejemplo 0, 1, 2, …)
            try {
                // Pausa el hilo un tiempo aleatorio para simular que está "produciendo"
                Thread.sleep((int)(Math.random() * 100));
            } catch (InterruptedException e) {
                // Si ocurre un error en la pausa, simplemente lo ignora
            }
        }
    }
}

// ------------------------------------------------------------
// Clase que representa un "consumidor", o sea un hilo que toma
// los elementos del buffer y los "usa" o procesa
class Consumidor extends Thread {
    private Buffer buffer; // También comparte el mismo buffer

    // El constructor recibe el buffer compartido
    public Consumidor(Buffer buffer) {
        this.buffer = buffer;
    }

    // Método que se ejecuta al iniciar el hilo
    public void run() {
        // El consumidor intentará extraer 10 elementos del buffer
        for (int i = 0; i < 10; i++) {
            buffer.extraer(); // Saca un elemento
            try {
                // Pausa un momento aleatorio para simular que "consume"
                Thread.sleep((int)(Math.random() * 100));
            } catch (InterruptedException e) {
                // Ignora la excepción si ocurre
            }
        }
    }
}

// ------------------------------------------------------------
// Clase principal donde se ejecuta todo el programa
public class ProductorConsumidorSinSync {
    public static void main(String[] args) {
        // Se crea un buffer con capacidad para 5 elementos
        Buffer buffer = new Buffer(5);

        // Se crean dos productores y dos consumidores que usarán el mismo buffer
        Productor p1 = new Productor(buffer);
        Productor p2 = new Productor(buffer);
        Consumidor c1 = new Consumidor(buffer);
        Consumidor c2 = new Consumidor(buffer);

        // Se inician todos los hilos (empiezan a trabajar en paralelo)
        p1.start();
        p2.start();
        c1.start();
        c2.start();
    }
}
