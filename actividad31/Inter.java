import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.Random;

/**
 * ============================================================
 *   ACTIVIDAD 3: Detección y Prevención de Interbloqueos
 * ============================================================
 * 
 * Descripción:
 * Este programa demuestra primero un escenario donde se produce 
 * un interbloqueo entre dos hilos, y luego aplica una prevención 
 * utilizando ReentrantLock y tryLock() con tiempo de espera.
 * 
 * Concepto:
 * - Interbloqueo: situación donde dos o más hilos quedan esperando 
 *   indefinidamente por recursos bloqueados entre sí.
 * - Prevención: consiste en usar mecanismos que eviten la espera circular,
 *   por ejemplo, el uso de tryLock() con timeout o adquirir recursos 
 *   siempre en un mismo orden.
 * 
 * Lo que se espera:
 * - En el primer caso (“con interbloqueo”) el programa se queda detenido.
 * - En el segundo caso (“prevención”), los hilos liberan recursos 
 *   y reintentan hasta poder ejecutarse correctamente.
 */
class EscenarioInterbloqueo {
    private ReentrantLock recurso1 = new ReentrantLock(); // Primer recurso compartido
    private ReentrantLock recurso2 = new ReentrantLock(); // Segundo recurso compartido
    private Random rand = new Random();

    /**
     * Método que simula el escenario con interbloqueo.
     * Los hilos adquieren los recursos en orden diferente, lo que causa el bloqueo circular.
     */
    public void tareaConInterbloqueo(int hiloId) {
        try {
            if (hiloId % 2 == 0) {
                // Hilo par: intenta primero recurso1, luego recurso2
                recurso1.lock();
                System.out.println("Hilo " + hiloId + " adquirió recurso1");
                Thread.sleep(rand.nextInt(100)); // Simula trabajo
                recurso2.lock();
                System.out.println("Hilo " + hiloId + " adquirió recurso2");
            } else {
                // Hilo impar: intenta primero recurso2, luego recurso1
                recurso2.lock();
                System.out.println("Hilo " + hiloId + " adquirió recurso2");
                Thread.sleep(rand.nextInt(100));
                recurso1.lock();
                System.out.println("Hilo " + hiloId + " adquirió recurso1");
            }

            // Sección crítica
            System.out.println("Hilo " + hiloId + " está ejecutando la sección crítica");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Liberar los recursos que tenga el hilo
            if (recurso1.isHeldByCurrentThread()) recurso1.unlock();
            if (recurso2.isHeldByCurrentThread()) recurso2.unlock();
        }
    }

    /**
     * Método que previene interbloqueos usando tryLock con timeout.
     * Si no logra adquirir ambos recursos, libera lo que tiene y reintenta.
     */
    public void tareaPrevencionInterbloqueo(int hiloId) {
        boolean completado = false;

        while (!completado) {
            try {
                if (hiloId % 2 == 0) {
                    // Hilo par: intenta adquirir recurso1 y luego recurso2
                    if (recurso1.tryLock(100, TimeUnit.MILLISECONDS)) {
                        try {
                            System.out.println("Hilo " + hiloId + " adquirió recurso1");
                            if (recurso2.tryLock(100, TimeUnit.MILLISECONDS)) {
                                try {
                                    System.out.println("Hilo " + hiloId + " adquirió recurso2");
                                    // Sección crítica protegida
                                    System.out.println("Hilo " + hiloId + " ejecuta sección crítica sin interbloqueo");
                                    completado = true; // Trabajo completado
                                } finally {
                                    recurso2.unlock();
                                }
                            } else {
                                // No pudo obtener recurso2, libera recurso1 y reintenta
                                System.out.println("Hilo " + hiloId + " no pudo adquirir recurso2, liberando recurso1");
                            }
                        } finally {
                            recurso1.unlock();
                        }
                    }
                } else {
                    // Hilo impar: intenta adquirir recurso2 y luego recurso1
                    if (recurso2.tryLock(100, TimeUnit.MILLISECONDS)) {
                        try {
                            System.out.println("Hilo " + hiloId + " adquirió recurso2");
                            if (recurso1.tryLock(100, TimeUnit.MILLISECONDS)) {
                                try {
                                    System.out.println("Hilo " + hiloId + " adquirió recurso1");
                                    System.out.println("Hilo " + hiloId + " ejecuta sección crítica sin interbloqueo");
                                    completado = true;
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

                // Espera aleatoria antes de reintentar
                Thread.sleep(rand.nextInt(100));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * Clase que representa un hilo del sistema.
 * Ejecuta la versión con o sin prevención de interbloqueo.
 */
class HiloDeadlock extends Thread {
    private EscenarioInterbloqueo escenario;
    private int id;
    private boolean prevenir;

    public HiloDeadlock(EscenarioInterbloqueo escenario, int id, boolean prevenir) {
        this.escenario = escenario;
        this.id = id;
        this.prevenir = prevenir;
    }

    @Override
    public void run() {
        if (prevenir)
            escenario.tareaPrevencionInterbloqueo(id);
        else
            escenario.tareaConInterbloqueo(id);
    }
}

/**
 * Clase principal donde se ejecutan ambas versiones:
 * 1. Con interbloqueo (sin prevención)
 * 2. Con prevención usando tryLock()
 */
public class Inter {
    public static void main(String[] args) throws InterruptedException {
        EscenarioInterbloqueo escenario = new EscenarioInterbloqueo();

        System.out.println("=== Escenario con posible interbloqueo ===");
        HiloDeadlock h1 = new HiloDeadlock(escenario, 1, false);
        HiloDeadlock h2 = new HiloDeadlock(escenario, 2, false);
        h1.start();
        h2.start();

        // Esperar un poco para observar el bloqueo
        h1.join(1000);
        h2.join(1000);
        System.out.println("\n(Si el programa no avanza, hay interbloqueo)\n");

        System.out.println("=== Escenario con prevención de interbloqueo ===");
        HiloDeadlock h3 = new HiloDeadlock(escenario, 1, true);
        HiloDeadlock h4 = new HiloDeadlock(escenario, 2, true);
        h3.start();
        h4.start();
        h3.join();
        h4.join();

        System.out.println("\nPrograma finalizado correctamente sin interbloqueos.");
    }
}
