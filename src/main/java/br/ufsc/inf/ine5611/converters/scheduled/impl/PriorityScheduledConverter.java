package br.ufsc.inf.ine5611.converters.scheduled.impl;

import br.ufsc.inf.ine5611.converters.Converter;
import br.ufsc.inf.ine5611.converters.scheduled.ConverterTask;
import br.ufsc.inf.ine5611.converters.scheduled.Priority;
import br.ufsc.inf.ine5611.converters.scheduled.ScheduledConverter;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import java.util.PriorityQueue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.function.Consumer;

public class PriorityScheduledConverter implements ScheduledConverter {
    public static final int DEFAULT_QUANTUM_LOW = 50;
    public static final int DEFAULT_QUANTUM_NORMAL = 100;
    public static final int DEFAULT_QUANTUM_HIGH = 200;
    
    private Converter converter;
    private HashMap<Priority, Integer> quantas;
    private Consumer<ConverterTask> cancelCallback;
    private PriorityQueue<ScheduledConverterTask> queue;
    private List<ConverterTask> allTasks;
    private ScheduledConverterTask current = null;
    
    private MyComparator comparator;
    

    public PriorityScheduledConverter(Converter converter) {
        //TODO implementar
        /* - Salve converter como um field, para uso posterior
           - Registre um listener em converter.addCompletionListener() para que você saiba
         *   quando uma tarefa terminou */
        this.converter = converter;
        this.converter.addCompletionListener(new Consumer<ConverterTask>() {
            @Override
            public void accept(ConverterTask t) {
                
            }
        });
        this.comparator = new MyComparator();
        this.queue = new PriorityQueue<>(comparator); 
            
    }

    @Override
    public void setQuantum(Priority priority, int milliseconds) {
        /* Dica: use um HasMap<Priority, Integer> para manter os quanta configurados para
         * cada prioridade */
        //TODO implementar
        quantas.put(priority, milliseconds);
        
        
    }

    @Override
    public int getQuantum(Priority priority) {
        /* Veja setQuantum */
        //TODO implementar
        return quantas.get(priority);
    }

    @Override
    public Collection<ConverterTask> getAllTasks() {
        /* Junte todas as tarefas não completas em um Collection */
        //TODO implementar
        return this.allTasks;
    }

    @Override
    public synchronized ConverterTask convert(InputStream inputStream, OutputStream outputStream,
                                String mediaType, long inputBytes, Priority priority) {
        /* - Crie um objeto ScheduledConverterTask utilizando os parâmetros dessa chamada
         * - Adicione o objeto em alguma fila (é possível implementar com uma ou várias filas)
         * - Se a nova tarefa for mais prioritária que a atualmente executando, interrompa */
        //TODO implementar
        long epoch = 0; //????????? mudar
        ScheduledConverterTask newTask = new ScheduledConverterTask(inputStream, outputStream, mediaType, this.cancelCallback, 
                inputBytes, priority, epoch);
        if (this.current == null) {
            current = newTask;
        } else {
            if (comparator.compare(current, newTask) <= 2) {
                this.queue.add(newTask);
            } if (comparator.compare(current, newTask) == 3 || comparator.compare(current, newTask) == 5 ) {
                //newTask deve interromper current, adicionar current no queue e tomar o seu lugar
            } if (comparator.compare(current, newTask) == 4) {
                //regra de prioridade para duas tarefas normais (round-robin)
            } if (comparator.compare(current, newTask) == 6) {
                //regra de prioridade para duas tarefas lows (não sei)
            }
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
        //TODO implementar
        
        long maxMs = TimeUnit.MILLISECONDS.convert(interval, timeUnit);
        long taskMs = Math.min(current.getCycles(), maxMs);
        Stopwatch w = Stopwatch.createStarted();
        
        /*try {
            while (current != null && current.getEpoch() > 0
                    && w.elapsed(TimeUnit.MILLISECONDS) < maxMs) {
                Stopwatch w2 = Stopwatch.createStarted();
                wait(taskMs);
                taskData.processingLeft -= w2.elapsed(TimeUnit.MILLISECONDS);
            }
            if (taskData.processingLeft <= 0) {
                completionListeners.forEach(l -> l.accept(task));
                activeTasks.remove(task);
                current = null;
            }
        } finally {
            current = null;
        } */
    }

    @Override
    public synchronized void close() throws Exception {
        /* - Libere quaisquer recursos alocados
         * - Cancele as tarefas não concluídas
         */
        //TODO implementar
        try {
            
        } catch (Exception e) {
            
        }
    }
    
    public boolean cancel() {
        
        return true;
    }
    
    public synchronized boolean interrupt() {
        if (current == null) return false;
        //interruptListeners.forEach(l -> l.accept(current.task));
        //fila de processos interrompidos??
        current = null;
        notifyAll();
        return true;
    }
   
    
    public class MyComparator implements Comparator<ScheduledConverterTask> {

    @Override
    public int compare(ScheduledConverterTask current, ScheduledConverterTask newTask) {
        if (current.getPriority() == Priority.HIGH && (newTask.getPriority() == Priority.HIGH || 
                newTask.getPriority() == Priority.NORMAL || newTask.getPriority() == Priority.LOW)) {
            return 1; //current precisar terminar sua execução antes da newTask, logo, adicionar newTask no queue 
            // verificar se HIGH para HIGH se aplica na regra do retorno.
        } else if (current.getPriority() == Priority.NORMAL && newTask.getPriority() == Priority.LOW) {
            return 2; //current precisar terminar sua execução antes da newTask, logo, adicionar newTask no queue
        } else if ((current.getPriority() == Priority.NORMAL || current.getPriority() == Priority.LOW) && newTask.getPriority() == Priority.HIGH) {
            return 3; //newTask deve interromper current, adicionar current no queue e tomar o seu lugar
        } else if (current.getPriority() == Priority.NORMAL && newTask.getPriority() == Priority.NORMAL) {
            return 4; //fazer o round-robin das tarefas normais
        } else if (current.getPriority() == Priority.LOW && newTask.getPriority() == Priority.NORMAL) {
            return 5; //newTask deve interromper current, adicionar current no queue e tomar o seu lugar
        } else if (current.getPriority() == Priority.LOW && newTask.getPriority() == Priority.LOW) {
            return 6; //entender regra de prioridade das tarefas LOW
        }
        return 0;
    }
    
}

}
