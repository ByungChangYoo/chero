package org.simpleframework.module.service;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.simpleframework.module.build.Argument;
import org.simpleframework.module.build.Function;
import org.simpleframework.module.common.Task;
import org.simpleframework.module.core.ComponentListener;
import org.simpleframework.module.core.ComponentManager;
import org.simpleframework.module.core.Context;
import org.simpleframework.module.extract.Extractor;
import org.simpleframework.module.graph.DependencyIdentifier;
import org.simpleframework.module.graph.DependencyProvider;
import org.simpleframework.module.graph.DependencyScanner;
import org.simpleframework.module.index.ModuleFilter;
import org.simpleframework.module.path.ClassPath;

public class ServiceAssembler {
   
   private final DependencyIdentifier identifier;
   private final DependencyScanner calculator;
   private final DependencyProvider provider;
   private final ComponentManager manager;
   
   public ServiceAssembler(ComponentManager manager, List<Extractor> extractors, Predicate<Argument> filter) {
      this.provider = new DependencyProvider(manager, extractors, filter);
      this.identifier = new DependencyIdentifier();
      this.calculator = new DependencyScanner();
      this.manager = manager;
   }

   public Task assemble(ModuleFilter filter, ClassPath path, Context context) {
      calculator.create(filter, path).traverse(type -> {
         AtomicReference<Object> result = new AtomicReference<Object>();
         
         try {
            List<Function> builders = provider.createProviders(filter, path, type);
            Iterator<Function> iterator = builders.iterator();
            
            while(iterator.hasNext()) {
               try {
                  Function builder = iterator.next();
                  Object instance = builder.getValue(context); 
                  
                  if(instance != null) {
                     String name = identifier.identify(builder);
                     
                     manager.register(instance, name);
                     result.set(instance);
                     
                     if(builder.isConstructor()) {
                        return result.get();
                     }
                  }
               } catch(Exception e) {}
            }
         }catch(Exception e) {
            throw new IllegalStateException("Could not start application", e);
         }
         return result.get();
      });
      return new ServiceTask(manager);

   }
   
   private static class ServiceTask implements Task {
      
      private final ComponentManager manager;
      
      public ServiceTask(ComponentManager manager) {
         this.manager = manager;
      }

      @Override
      public void start() {
         manager.resolveAll(ComponentListener.class)
            .forEach(listener -> {
               try {
                  listener.onReady();
               } catch(Exception e) {}
            });
      }

      @Override
      public void stop() {
         manager.resolveAll(ComponentListener.class)
            .forEach(listener -> {
               try {
                  listener.onDestroy();
               } catch(Exception e) {}
            });
      }
      
   }
}
