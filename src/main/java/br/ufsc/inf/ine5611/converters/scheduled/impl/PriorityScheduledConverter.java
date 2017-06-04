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
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        ScheduledConverterTask task = new ScheduledConverterTask(inputStream, outputStream, mediaType, this.cancelCallback, inputBytes, priority, inputBytes);
        this.queue.add(task);
        
        if (this.current == null) {
            current = task;
        } else {
            if (comparator.compare(current, task) == 1) {
                
            }
        }
        return task;
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
   
    
    public class MyComparator implements Comparator<ScheduledConverterTask> {

    @Override
    public int compare(ScheduledConverterTask task1, ScheduledConverterTask task2) {
        if (task1.getPriority() == Priority.HIGH && task2.getPriority() == Priority.HIGH) {
            return 1; //taskAtual precisar terminar sua execução
        } 
        return 0;
    }
    
}

}
