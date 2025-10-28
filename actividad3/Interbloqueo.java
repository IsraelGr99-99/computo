import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.Random;

/**
 * Clase EscenarioInterbloqueo
 * Simula dos recursos compartidos que pueden ser adquiridos por múltiples hilos.
 */
class EscenarioInterbloqueo {
    private ReentrantLock recurso1 = new ReentrantLock();
    private ReentrantLock recurso2 = new ReentrantLock();
    private Random rand = new Random();

    /**
     * Método que simula un hilo intentando adquirir ambos recursos en un orden fijo.
     * Si se usan locks normales sin prevención, puede ocurrir interbloqueo.
     * @param hiloId Identificador del hilo
     */
    public void tareaConInterbloqueo(int hiloId) {
        try {
            if (hiloId % 2 == 0) {
                // Hilo par intenta recurso1 primero, luego recurso2
                recurso1.lock();
                System.out.println("Hilo " + hiloId + " adquirió recurso1");
                Thread.sleep(rand.nextInt(100));
                recurso2.lock();
                System.out.println("Hilo " + hiloId + " adquirió recurso2");
            } else {
                // Hilo impar intenta recurso2 primero, luego recurso1
                recurso2.lock();
                System.out.println("Hilo " + hiloId + " adquirió recurso2");
                Thread.sleep(rand.nextInt(100));
                recurso1.lock();
                System.out.println("Hilo " + hiloId + " adquirió recurso1");
            }
            // Sección crítica simulada
            System.out.println("Hilo " + hiloId + " está ejecutando la sección crítica");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Liberar locks si los tiene
            if (recurso1.isHeldByCurrentThread()) recurso1.unlock();
            if (recurso2.isHeldByCurrentThread()) recurso2.unlock();
        }
    }

    /**
     * Método que previene interbloqueos usando tryLock con timeout
     * @param hiloId Identificador del hilo
     */
    public void tareaPrevencionInterbloqueo(int hiloId) {
        boolean adquirido = false;
        while (!adquirido) {
            try {
                if (hiloId % 2 == 0) {
                    // Hilo par intenta adquirir recurso1 con timeout
                    if (recurso1.tryLock(100, TimeUnit.MILLISECONDS)) {
                        try {
                            System.out.println("Hilo " + hiloId + " adquirió recurso1");
                            if (recurso2.tryLock(100, TimeUnit.MILLISECONDS)) {
                                try {
                                    System.out.println("Hilo " + hiloId + " adquirió recurso2");
                                    // Sección crítica
                                    System.out.println("Hilo " + hiloId + " está ejecutando sección crítica");
                                    adquirido = true;
                                } finally {
                                    recurso2.unlock();
                                }
                            } else {
                                System.out.println("Hilo " + hiloId + " no pudo adquirir recurso2, liberando recurso1");
                            }
                        } finally {
                            recurso1.unlock();
                        }
                    }
                } else {
                    // Hilo impar intenta adquirir recurso2 primero
                    if (recurso2.tryLock(100, TimeUnit.MILLISECONDS)) {
                        try {
                            System.out.println("Hilo " + hiloId + " adquirió recurso2");
                            if (recurso1.tryLock(100, TimeUnit.MILLISECONDS)) {
                                try {
                                    System.out.println("Hilo " + hiloId + " adquirió recurso1");
                                    System.out.println("Hilo " + hiloId + " está ejecutando sección crítica");
                                    adquirido = true;
                                } finally {
                                    recurso1.unlock();
                                }
                            } else {
                                System.out.println("Hilo " + hiloId + " no pudo adquirir recurso1, liberando recurso2");
                            }
                        } finally {
                            recurso2.unlock();
                        }
                    }
                }
                Thread.sleep(rand.nextInt(50)); // Espera antes de reintentar
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * Clase HiloDeadlock
 * Ejecuta la tarea con o sin prevención según el modo.
 */
class HiloDeadlock extends Thread {
    private EscenarioInterbloqueo escenario;
    private int id;
    private boolean prevenirDeadlock;

    public HiloDeadlock(EscenarioInterbloqueo escenario, int id, boolean prevenirDeadlock) {
        this.escenario = escenario;
        this.id = id;
        this.prevenirDeadlock = prevenirDeadlock;
    }

    @Override
    public void run() {
        if (prevenirDeadlock) {
            escenario.tareaPrevencionInterbloqueo(id);
        } else {
            escenario.tareaConInterbloqueo(id);
        }
    }
}

/**
 * Clase principal
 * Ejecuta varios hilos para demostrar interbloqueo y su prevención.
 */
public class Interbloqueo {
    public static void main(String[] args) throws InterruptedException {
        EscenarioInterbloqueo escenario = new EscenarioInterbloqueo();

        System.out.println("=== Escenario con posible interbloqueo ===");
        HiloDeadlock h1 = new HiloDeadlock(escenario, 1, false);
        HiloDeadlock h2 = new HiloDeadlock(escenario, 2, false);
        h1.start();
        h2.start();
        h1.join();
        h2.join();

        System.out.println("\n=== Escenario con prevención de interbloqueo ===");
        HiloDeadlock h3 = new HiloDeadlock(escenario, 1, true);
        HiloDeadlock h4 = new HiloDeadlock(escenario, 2, true);
        h3.start();
        h4.start();
        h3.join();
        h4.join();

        System.out.println("\nPrograma finalizado correctamente.");
    }
}
