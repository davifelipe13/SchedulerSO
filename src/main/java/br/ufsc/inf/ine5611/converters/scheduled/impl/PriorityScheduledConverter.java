package br.ufsc.inf.ine5611.converters.scheduled.impl;

import br.ufsc.inf.ine5611.converters.Converter;
import br.ufsc.inf.ine5611.converters.scheduled.ConverterTask;
import br.ufsc.inf.ine5611.converters.scheduled.Priority;
import br.ufsc.inf.ine5611.converters.scheduled.ScheduledConverter;
import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PriorityScheduledConverter implements ScheduledConverter {
    public static final int DEFAULT_QUANTUM_LOW = 50;
    public static final int DEFAULT_QUANTUM_NORMAL = 100;
    public static final int DEFAULT_QUANTUM_HIGH = 200;
    
    private Converter converter;
    private HashMap<Priority, Integer> quantas;
    private PriorityBlockingQueue<ScheduledConverterTask> queue;
    private Collection<ConverterTask> allTasks;
    private ScheduledConverterTask current = null;
    
    private MyComparator comparator;
    private long epoch;


    public PriorityScheduledConverter(Converter converter) {
        //TODO implementar
        /* - Salve converter como um field, para uso posterior
           - Registre um listener em converter.addCompletionListener() para que você saiba
         *   quando uma tarefa terminou */
        this.converter = converter;
        this.converter.addCompletionListener(this::completarTarefa);
        this.comparator = new MyComparator();
        this.queue = new PriorityBlockingQueue<>(1024, comparator);
        this.quantas = new HashMap<>();
        this.allTasks = new ArrayList<ConverterTask>();
        this.quantas.put(Priority.LOW, DEFAULT_QUANTUM_LOW);
        this.quantas.put(Priority.NORMAL, DEFAULT_QUANTUM_NORMAL);
        this.quantas.put(Priority.HIGH, DEFAULT_QUANTUM_HIGH);
            
    }

    private void completarTarefa(ConverterTask task) {
        ScheduledConverterTask sct = (ScheduledConverterTask) task;
        sct.complete(null);
        queue.remove(sct);
    }

    @Override
    public void setQuantum(Priority priority, int milliseconds) {
        /* Dica: use um HasMap<Priority, Integer> para manter os quanta configurados para
         * cada prioridade */
        //TODO implementar
        Integer quantum = milliseconds;
        quantas.put(priority, quantum);
        
    }

    @Override
    public int getQuantum(Priority priority) {
        /* Veja setQuantum */
        //TODO implementar
        return quantas.get(priority);
    }

    @Override
    public Collection<ConverterTask> getAllTasks() {
        /* Junte todas as tarefas não completas em um Collecti  on */
        //TODO implementar
        for (ConverterTask task: queue) {
                this.allTasks.add(task);
        }
        return this.allTasks;
    }

    @Override
    public synchronized ConverterTask convert(InputStream inputStream, OutputStream outputStream,
                                String mediaType, long inputBytes, Priority priority) {
        /* - Crie um objeto ScheduledConverterTask utilizando os parâmetros dessa chamada
         * - Adicione o objeto em alguma fila (é possível implementar com uma ou várias filas)
         * - Se a nova tarefa for mais prioritária que a atualmente executando, interrompa */
        //TODO implementar
        ScheduledConverterTask newTask = new ScheduledConverterTask(inputStream, outputStream, mediaType, this::cancel,
                inputBytes, priority, epoch++);
        queue.add(newTask);
        if(current != null) {
            if (priority.compareTo(current.getPriority()) == 1)
                interrupt();
        } 
        return newTask;
        
    }

    @Override
    public void processFor(long interval, TimeUnit timeUnit) throws InterruptedException {
        /* Pseudocódigo:
         * while (!tempo_estourado) {
         *   t = escolha_tarefa();
         *   t.incCycles();
         *   this.converter.processFor(getQuantum(t.getPriority(), MILLISECONDS);
         * }
         */
        //TODO implementarasdasd
        int a = 0;
        long maxMs = TimeUnit.MILLISECONDS.convert(interval, timeUnit);
        Stopwatch w = Stopwatch.createStarted();
         while (w.elapsed(TimeUnit.MILLISECONDS) < maxMs) {
             ScheduledConverterTask task = queue.take();
             current = task;
             task.incCycles();
             queue.add(task);
            try {
                if (!task.isDone())
                    this.converter.processFor(task, getQuantum(task.getPriority()), MILLISECONDS);
            } catch (IOException ex) {
                task.completeExceptionally(ex);
                queue.remove(task);
            }
         }
    }

    @Override
      public synchronized void close() throws Exception {
        /* - Libere quaisquer recursos alocados
         * - Cancele as tarefas não concluídas
         */
        //TODO implementar
        for(ScheduledConverterTask task : queue){
            task.close();
            cancel(task);
        }
    }
    
    public boolean cancel(ConverterTask task) {
        converter.cancel(task); //marca como cancelado
        if (task == current) converter.interrupt();
        
        return queue.remove(task);
    }
    
    public synchronized boolean interrupt() {
        if (current == null) return false;
        current = null;
        this.converter.interrupt();
        return true;
    }
   
    
    public class MyComparator implements Comparator<ScheduledConverterTask> {

    @Override
    public int compare(ScheduledConverterTask l, ScheduledConverterTask r) {
        int cmp = -1 * l.getPriority().compareTo(r.getPriority());//ALGORITMO PRIORIDADE
        if (cmp != 0)  return cmp; 
        if (l.getPriority() == Priority.LOW) {
            //ALGORITMO SJF
            cmp = Long.compare(l.getInputBytes(), r.getInputBytes());
            if (cmp == 0) {
                cmp = Long.compare(l.getCycles(), r.getCycles());
            } else {
                return cmp;
            }
        } else if (l.getPriority() == Priority.NORMAL) {
            //ALGORITMO ROUND ROBIN
            cmp = Long.compare(l.getCycles(), r.getCycles());
            if(cmp == 0){
                return Long.compare(l.getEpoch(), r.getEpoch());
            }
            return cmp;
                
        } else {
            //ALGORITMO FIFO
            cmp = Long.compare(l.getEpoch(), r.getEpoch());
            return cmp;
            
        }
        return 0; 
    }
    
}

}
